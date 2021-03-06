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
package com.huiyang.PBFTnet.bftsmart.tom.server.defaultservices;

import com.huiyang.PBFTnet.bftsmart.tom.MessageContext;
import com.huiyang.PBFTnet.bftsmart.tom.ReplicaContext;
import com.huiyang.PBFTnet.bftsmart.tom.core.messages.TOMMessage;
import com.huiyang.PBFTnet.bftsmart.tom.server.Replier;

/**
 *
 * @author miguel
 */
public class DefaultReplier implements Replier{

    private ReplicaContext rc;
    
    @Override
    public void manageReply(TOMMessage request, MessageContext msgCtx) {
        rc.getServerCommunicationSystem().send(new int[]{request.getSender()}, request.reply);
    }

    @Override
    public void setReplicaContext(ReplicaContext rc) {
        this.rc = rc;
    }
    
}
