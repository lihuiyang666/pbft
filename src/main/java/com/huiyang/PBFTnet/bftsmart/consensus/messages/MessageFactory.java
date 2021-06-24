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
package com.huiyang.PBFTnet.bftsmart.consensus.messages;

/**
 * This class work as a factory of messages used in the paxos protocol.
 */
public class MessageFactory{

    // constants for messages types
    public static final int PROPOSE = 44781;
    public static final int WRITE    = 44782;
    public static final int ACCEPT  = 44783;
    public static final int COLLECT = 44786;

    private int from; // Replica ID of the process which sent this message

    /**
     * Creates a message factory
     * @param from Replica ID of the process which sent this message
     */
    public MessageFactory(int from) {

        this.from = from;

    }

    /**
     * Creates a PROPOSE message to be sent by this process
     * @param id Consensus's execution ID
     * @param round Round number
     * @param value Proposed value
     * @param proof Proofs from other replicas
     * @return A paxos message of the PROPOSE type, with the specified id, round, value, and proof
     */
    public PaxosMessage createPropose(int id, int round, byte[] value) {

        return new PaxosMessage(PROPOSE, id, round, from, value);

    }

    /**
     * Creates a WRITE message to be sent by this process
     * @param id Consensus's execution ID
     * @param round Round number
     * @param value Write value
     * @return A consensus message of the WRITE type, with the specified id, round, and value
     */
    public PaxosMessage createWrite(int id, int round, byte[] value) {

        return new PaxosMessage(WRITE,id,round, from, value);

    }

    /**
     * Creates a WRITE message to be sent by this process
     * @param id Consensus's execution ID
     * @param round Round number
     * @param value Accepted value
     * @return A consensus message of the ACCEPT type, with the specified id, round, and value
     */
    public PaxosMessage createAccept(int id, int round, byte[] value) {

        return new PaxosMessage(ACCEPT,id,round, from, value);

    }


}

