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
package com.huiyang.PBFTnet.bftsmart.statemanagement.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import com.huiyang.PBFTnet.bftsmart.consensus.executionmanager.ExecutionManager;
import com.huiyang.PBFTnet.bftsmart.consensus.messages.PaxosMessage;
import com.huiyang.PBFTnet.bftsmart.reconfiguration.views.View;
import com.huiyang.PBFTnet.bftsmart.statemanagement.ApplicationState;
import com.huiyang.PBFTnet.bftsmart.statemanagement.SMMessage;
import com.huiyang.PBFTnet.bftsmart.tom.core.DeliveryThread;
import com.huiyang.PBFTnet.bftsmart.tom.core.TOMLayer;
import com.huiyang.PBFTnet.bftsmart.tom.leaderchange.LCManager;
import com.huiyang.PBFTnet.bftsmart.tom.util.Logger;
import com.huiyang.PBFTnet.bftsmart.tom.util.TOMUtil;

/**
 * 
 * @author Marcel Santos
 *
 */
public class StandardStateManager extends BaseStateManager {
	
    private int replica;
    private ReentrantLock lockTimer = new ReentrantLock();
    private Timer stateTimer = null;
    private final static long INIT_TIMEOUT = 40000;
    private long timeout = INIT_TIMEOUT;
    
    private LCManager lcManager;
    private ExecutionManager execManager;


    @Override
    public void init(TOMLayer tomLayer, DeliveryThread dt) {
    	SVController = tomLayer.controller;
    	
        this.tomLayer = tomLayer;
        this.dt = dt;
        this.lcManager = tomLayer.getLCManager();
        this.execManager = tomLayer.execManager;

        this.replica = 0;

        if (SVController.getCurrentViewN() > 1 && replica == SVController.getStaticConf().getProcessId())
        	changeReplica();

        state = null;
        lastEid = -1;
        waitingEid = -1;

        appStateOnly = false;
    }
	
    private void changeReplica() {
        int pos = -1;
        do {
            pos = this.SVController.getCurrentViewPos(replica);
            replica = this.SVController.getCurrentViewProcesses()[(pos + 1) % SVController.getCurrentViewN()];
        } while (replica == SVController.getStaticConf().getProcessId());
    }
    
    @Override
    protected void requestState() {
        if (tomLayer.requestsTimer != null)
        	tomLayer.requestsTimer.clearAll();

        SMMessage smsg = new StandardSMMessage(SVController.getStaticConf().getProcessId(),
                waitingEid, TOMUtil.SM_REQUEST, replica, null, null, -1, -1);
        tomLayer.getCommunication().send(SVController.getCurrentViewOtherAcceptors(), smsg);

        System.out.println("(StandardStateManager.requestState) I just sent a request to the other replicas for the state up to EID " + waitingEid);

        TimerTask stateTask =  new TimerTask() {
            public void run() {
            	System.out.println("Timeout to retrieve state");
                int[] myself = new int[1];
                myself[0] = SVController.getStaticConf().getProcessId();
                tomLayer.getCommunication().send(myself, new StandardSMMessage(-1, waitingEid, TOMUtil.TRIGGER_SM_LOCALLY, -1, null, null, -1, -1));
            }
        };

        stateTimer = new Timer("state timer");
        timeout = timeout * 2;
        stateTimer.schedule(stateTask,timeout);
    }

    @Override
    public void stateTimeout() {
        lockTimer.lock();
        Logger.println("(StateManager.stateTimeout) Timeout for the replica that was supposed to send the complete state. Changing desired replica.");
        System.out.println("Timeout no timer do estado!");
        if (stateTimer != null)
        	stateTimer.cancel();
        changeReplica();
        reset();
        requestState();
        lockTimer.unlock();
    }
    
	@Override
    public void SMRequestDeliver(SMMessage msg, boolean isBFT) {
        if (SVController.getStaticConf().isStateTransferEnabled() && dt.getRecoverer() != null) {
        	StandardSMMessage stdMsg = (StandardSMMessage)msg;
            boolean sendState = stdMsg.getReplica() == SVController.getStaticConf().getProcessId();
            
            System.out.println("-- Should I send the state? " + sendState);
            
            ApplicationState thisState = dt.getRecoverer().getState(msg.getEid(), sendState);
            if (thisState == null) {
                
                System.out.println("-- For some reason, I am sending a void state");
              thisState = dt.getRecoverer().getState(-1, sendState);
            }
            else {
                System.out.println("-- Will I send the state? " + thisState.getSerializedState() != null);
            }
            int[] targets = { msg.getSender() };
            SMMessage smsg = new StandardSMMessage(SVController.getStaticConf().getProcessId(),
                    msg.getEid(), TOMUtil.SM_REPLY, -1, thisState, SVController.getCurrentView(), lcManager.getLastReg(), tomLayer.lm.getCurrentLeader());
            System.out.println("Sending state");
            tomLayer.getCommunication().send(targets, smsg);
            System.out.println("Sent");
        }
    }

	@Override
    public void SMReplyDeliver(SMMessage msg, boolean isBFT) {
        lockTimer.lock();
        if (SVController.getStaticConf().isStateTransferEnabled()) {
            if (waitingEid != -1 && msg.getEid() == waitingEid) {
                int currentRegency = -1;
                int currentLeader = -1;
                View currentView = null;
                
                if (!appStateOnly) {
                	senderRegencies.put(msg.getSender(), msg.getRegency());
                	senderLeaders.put(msg.getSender(), msg.getLeader());
                	senderViews.put(msg.getSender(), msg.getView());
                    if (moreThan2F_Regencies(msg.getRegency())) currentRegency = msg.getRegency();
                    if (moreThan2F_Leaders(msg.getLeader())) currentLeader = msg.getLeader();
                    if (moreThan2F_Views(msg.getView())) {
                        currentView = msg.getView();
                    }
                } else {
                    currentLeader = tomLayer.lm.getCurrentLeader();
                    currentRegency = lcManager.getLastReg();
                    currentView = SVController.getCurrentView();
                }
                
                if (msg.getSender() == replica && msg.getState().getSerializedState() != null) {
                	System.out.println("Expected replica sent state. Setting it to state");
                    state = msg.getState();
                    if (stateTimer != null) stateTimer.cancel();
                }

                senderStates.put(msg.getSender(), msg.getState());

                System.out.println("Verifying more than F replies");
                if (moreThanF_Replies()) {
                    System.out.println("More than F confirmed");
                    ApplicationState otherReplicaState = getOtherReplicaState();
                    System.out.println("State != null: " + (state != null) + ", recvState != null: " + (otherReplicaState != null));
                    int haveState = 0;
                        if(state != null) {
                            byte[] hash = null;
                            hash = tomLayer.computeHash(state.getSerializedState());
                            if (otherReplicaState != null) {
                                if (Arrays.equals(hash, otherReplicaState.getStateHash())) haveState = 1;
                                else if (getNumEqualStates() > SVController.getCurrentViewF())
                                    haveState = -1;
                            }
                        }
                    
                    System.out.println("haveState: " + haveState);

                    if (otherReplicaState != null && haveState == 1 && currentRegency > -1 &&
                            currentLeader > -1 && currentView != null) {

                    	System.out.println("Received state. Will install it");
                    	
                        lcManager.setLastReg(currentRegency);
                        lcManager.setNextReg(currentRegency);
                        lcManager.setNewLeader(currentLeader);
                        tomLayer.lm.setNewLeader(currentLeader);
                        //if (currentRegency > 0)
                        //    tomLayer.requestsTimer.setTimeout(tomLayer.requestsTimer.getTimeout() * (currentRegency * 2));
                        
                        dt.deliverLock();
                        waitingEid = -1;
                        dt.update(state);
                        
                        if (!appStateOnly && execManager.stopped()) {
                            Queue<PaxosMessage> stoppedMsgs = execManager.getStoppedMsgs();
                            for (PaxosMessage stopped : stoppedMsgs) {
                                if (stopped.getNumber() > state.getLastEid() /*msg.getEid()*/)
                                    execManager.addOutOfContextMessage(stopped);
                            }
                            execManager.clearStopped();
                            execManager.restart();
                        }
                        
                        tomLayer.processOutOfContext();
                        
                        if (SVController.getCurrentViewId() != currentView.getId()) {
                            System.out.println("Installing current view!");
                            SVController.reconfigureTo(currentView);
                        }
                        
						isInitializing = false;
						
                        dt.canDeliver();
                        dt.deliverUnlock();

                        reset();

                        System.out.println("I updated the state!");

                        tomLayer.requestsTimer.Enabled(true);
                        tomLayer.requestsTimer.startTimer();
                        if (stateTimer != null) stateTimer.cancel();
                        
                        if (appStateOnly) {
                        	appStateOnly = false;
                            tomLayer.resumeLC();
                        }
                    } else if (otherReplicaState == null && (SVController.getCurrentViewN() / 2) < getReplies()) {
                    	System.out.println("otherReplicaState == null && (SVController.getCurrentViewN() / 2) < getReplies()");
                        waitingEid = -1;
                        reset();
 
                        if (stateTimer != null) stateTimer.cancel();
                        
                        if (appStateOnly) {
                            requestState();
                        }
                    } else if (haveState == -1) {
                        System.out.println("haveState == -1");
                        Logger.println("(TOMLayer.SMReplyDeliver) The replica from which I expected the state, sent one which doesn't match the hash of the others, or it never sent it at all");

                        changeReplica();
                        reset();
                        requestState();

                        if (stateTimer != null) stateTimer.cancel();
                    } else if (haveState == 0 && (SVController.getCurrentViewN() - SVController.getCurrentViewF()) <= getReplies()) {

                        Logger.println("(TOMLayer.SMReplyDeliver) Could not obtain the state, retrying");
                        reset();
                        if (stateTimer != null) stateTimer.cancel();
                        waitingEid = -1;
                        //requestState();
                    } else {
                        System.out.println(" -- State transfer not yet finished");

                    }
                }
            }
        }
        lockTimer.unlock();
    }
	
    /**
     * Search in the received states table for a state that was not sent by the expected
     * replica. This is used to compare both states after received the state from expected
     * and other replicas.
     * @return The state sent from other replica
     */
    private ApplicationState getOtherReplicaState() {
    	int[] processes = SVController.getCurrentViewProcesses();
    	for(int process : processes) {
    		if(process == replica)
    			continue;
    		else {
    			ApplicationState otherState = senderStates.get(process);
    			if(otherState != null)
    				return otherState;
    		}
    	}
    	return null;
    }

    private int getNumEqualStates() {
    	List<ApplicationState> states = new ArrayList<ApplicationState>(receivedStates()); 
    	int match = 0;
        for (ApplicationState st1 : states) {
        	int count = 0;
            for (ApplicationState st2 : states) {
            	if(st1 != null && st1.equals(st2))
            		count++;
            }
            if(count > match)
            	match = count;
        }
        return match;
    }

	@Override
	public void currentConsensusIdAsked(int sender) {
		int me = SVController.getStaticConf().getProcessId();
		SMMessage currentEid = new StandardSMMessage(me, lastEid, TOMUtil.SM_REPLY_INITIAL, 0, null, null, 0, 0);
		tomLayer.getCommunication().send(new int[]{sender}, currentEid);
	}
	
}
