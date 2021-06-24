/**
Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.huiyang.PBFTnet.bftsmart.communication;

import com.huiyang.PBFTnet.bftsmart.communication.server.ServerConnection;
import com.huiyang.PBFTnet.bftsmart.consensus.messages.MessageFactory;
import com.huiyang.PBFTnet.bftsmart.consensus.messages.PaxosMessage;
import com.huiyang.PBFTnet.bftsmart.consensus.roles.Acceptor;
import com.huiyang.PBFTnet.bftsmart.statemanagement.SMMessage;
import com.huiyang.PBFTnet.bftsmart.tom.core.TOMLayer;
import com.huiyang.PBFTnet.bftsmart.tom.core.messages.TOMMessage;
import com.huiyang.PBFTnet.bftsmart.tom.core.timer.ForwardedMessage;
import com.huiyang.PBFTnet.bftsmart.tom.leaderchange.LCMessage;
import com.huiyang.PBFTnet.bftsmart.tom.util.Logger;
import com.huiyang.PBFTnet.bftsmart.tom.util.TOMUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

/**
 *
 * @author edualchieri
 */
public class MessageHandler {

    private Acceptor acceptor;
    private TOMLayer tomLayer;
    //private Cipher cipher;
    private Mac mac;
    
    public MessageHandler() {
        try {
            //this.cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            this.mac = Mac.getInstance(ServerConnection.MAC_ALGORITHM);
        } catch (NoSuchAlgorithmException /*| NoSuchPaddingException*/ ex) {
            ex.printStackTrace();
        }
    }
    public void setAcceptor(Acceptor acceptor) {
        this.acceptor = acceptor;
    }

    public void setTOMLayer(TOMLayer tomLayer) {
        this.tomLayer = tomLayer;
    }

    @SuppressWarnings("unchecked")
    protected void processData(SystemMessage sm) {
        if (sm instanceof PaxosMessage) {
            
            int myId = tomLayer.controller.getStaticConf().getProcessId();
            
            PaxosMessage paxosMsg = (PaxosMessage) sm;

            if (paxosMsg.authenticated || paxosMsg.getSender() == myId) acceptor.deliver(paxosMsg);
            else if (paxosMsg.getPaxosType() == MessageFactory.ACCEPT && paxosMsg.getProof() != null) {
                                        
                //We are going to verify the MAC vector at the algorithm level
                HashMap<Integer, byte[]> macVector = (HashMap<Integer, byte[]>) paxosMsg.getProof();
                               
                byte[] recvMAC = macVector.get(myId);
                
                PaxosMessage pm = new PaxosMessage(MessageFactory.ACCEPT,paxosMsg.getNumber(),
                        paxosMsg.getRound(), paxosMsg.getSender(), paxosMsg.getValue());
                
                ByteArrayOutputStream bOut = new ByteArrayOutputStream(248);
                try {
                    new ObjectOutputStream(bOut).writeObject(pm);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                byte[] data = bOut.toByteArray();
        
                //byte[] hash = tomLayer.computeHash(data); 
                
                byte[] myMAC = null;
                
                /*byte[] k = tomLayer.getCommunication().getServersConn().getSecretKey(paxosMsg.getSender()).getEncoded();
                SecretKeySpec key = new SecretKeySpec(new String(k).substring(0, 8).getBytes(), "DES");*/
                
                SecretKey key = tomLayer.getCommunication().getServersConn().getSecretKey(paxosMsg.getSender());
                try {
                    this.mac.init(key);                   
                    myMAC = this.mac.doFinal(data);
                } catch (/*IllegalBlockSizeException | BadPaddingException |*/ InvalidKeyException ex) {
                    ex.printStackTrace();
                }
                
                if (recvMAC != null && myMAC != null && Arrays.equals(recvMAC, myMAC))
                    acceptor.deliver(paxosMsg);
                else {
                    Logger.println("(MessageHandler.processData) WARNING: invalid MAC from " + sm.getSender());
                    System.out.println("(MessageHandler.processData) WARNING: invalid MAC from " + sm.getSender());
                }
            } else {
                System.out.println("(MessageHandler.processData) Discarding unauthenticated message from " + sm.getSender());
                Logger.println("(MessageHandler.processData) Discarding unauthenticated message from " + sm.getSender());
            }

        } else {
        	if (sm.authenticated) {
	            /*** This is Joao's code, related to leader change */
	            if (sm instanceof LCMessage) {
	                LCMessage lcMsg = (LCMessage) sm;
	
	                String type = null;
	                switch(lcMsg.getType()) {
	
	                    case TOMUtil.STOP:
	                        type = "STOP";
	                        break;
	                    case TOMUtil.STOPDATA:
	                        type = "STOPDATA";
	                        break;
	                    case TOMUtil.SYNC:
	                        type = "SYNC";
	                        break;
	                    default:
	                        type = "LOCAL";
	                        break;
	                }
	
	                System.out.println("(MessageHandler.processData) LC_MSG received: type " + type + ", regency " + lcMsg.getReg() + ", (replica " + lcMsg.getSender() + ")");
	                if (lcMsg.TRIGGER_LC_LOCALLY) tomLayer.requestsTimer.run_lc_protocol();
	                else tomLayer.deliverTimeoutRequest(lcMsg);
	            /**************************************************************/
	
	            } else if (sm instanceof ForwardedMessage) {
	                TOMMessage request = ((ForwardedMessage) sm).getRequest();
	                tomLayer.requestReceived(request);
	
	            /** This is Joao's code, to handle state transfer */
	            } else if (sm instanceof SMMessage) {
	                SMMessage smsg = (SMMessage) sm;
	                // System.out.println("(MessageHandler.processData) SM_MSG received: type " + smsg.getType() + ", regency " + smsg.getRegency() + ", (replica " + smsg.getSender() + ")");
	                switch(smsg.getType()) {
	                    case TOMUtil.SM_REQUEST:
		                    tomLayer.getStateManager().SMRequestDeliver(smsg, tomLayer.controller.getStaticConf().isBFT());
	                        break;
	                    case TOMUtil.SM_REPLY:
		                    tomLayer.getStateManager().SMReplyDeliver(smsg, tomLayer.controller.getStaticConf().isBFT());
	                        break;
	                    case TOMUtil.SM_ASK_INITIAL:
	                    	tomLayer.getStateManager().currentConsensusIdAsked(smsg.getSender());
	                    	break;
	                    case TOMUtil.SM_REPLY_INITIAL:
	                    	tomLayer.getStateManager().currentConsensusIdReceived(smsg);
	                    	break;
	                    default:
		                    tomLayer.getStateManager().stateTimeout();
	                        break;
	                }
	            /******************************************************************/
	            } else {
	            	System.out.println("UNKNOWN MESSAGE TYPE: " + sm);
	            }
	        } else {
	            System.out.println("(MessageHandler.processData) Discarding unauthenticated message from " + sm.getSender());
	        }
        }
    }
    
    protected void verifyPending() {
        tomLayer.processOutOfContext();
    }
}
