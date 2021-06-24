package com.huiyang.PBFTprotocol;

import com.huiyang.PBFTnet.bftsmart.tom.ServiceProxy;
import com.huiyang.PBFTtrans.BftTransaction;
import com.huiyang.consensusEngine.Protocol;
import com.huiyang.consensusEngine.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

@Service
public class PBFTProtocol implements Protocol {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    String getNeturl;

    private HashSet<String> hostlist;


    @Override
    public boolean getNet() {

        try {
            hostlist=restTemplate.getForObject(getNeturl,HashSet.class);
            if (hostlist==null||hostlist.size()==0)
                return false;
            else return true;
        } catch (RestClientException e) {
            e.printStackTrace();
            return false;
        }


    }

    @Override
    public synchronized void execute(ArrayList<Transaction> trans) {

        ServiceProxy counterProxy = new ServiceProxy(1005);
        try {

            for (int i=0;i<trans.size();i++){
                BftTransaction tmp=( BftTransaction) trans.get(i);
                ByteArrayOutputStream out = new ByteArrayOutputStream(4);
                new DataOutputStream(out).writeInt(1);

                System.out.println("发送第" + i+"个事务请求");
                byte[] reply;
                reply = counterProxy.invokeOrdered(out.toByteArray());
                if(reply != null) {
                    int newValue = new DataInputStream(new ByteArrayInputStream(reply)).readInt();
                    System.out.println("统计已处理的事务数: " + newValue);

                } else {

                    break;
                }
            }

            System.out.println(System.currentTimeMillis());
        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            counterProxy.close();


        }

    }
}
