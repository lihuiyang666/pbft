package com.huiyang.PBFTtrans;

import com.huiyang.consensusEngine.Transaction;
import com.huiyang.consensusEngine.TransactionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

@Service
public class PBFTPool implements TransactionPool {

    public CopyOnWriteArrayList<BftTransaction> pool;

    private final Semaphore semaphore;

    PBFTPool(){
        pool=new CopyOnWriteArrayList<>();
        semaphore=new Semaphore(1);

    }

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    String protocolurl;

    @Override
    public Constructor[] getConstrucors() {
        try {
            Class clazz=Class.forName("com.huiyang.rafttrans.RTransaction");
            Constructor[] c=clazz.getConstructors();
            return c;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean addToPool(Transaction transaction) {
        try {
            BftTransaction temp= (BftTransaction) transaction;

            semaphore.acquire();

            pool.add(temp);
            semaphore.release();
            this.check();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void check() {
        if (pool.size()>999){
            boolean res=sendToProtocol();
            System.out.println(res);
        }


    }

    @Override
    public boolean sendToProtocol() {

        try {
            semaphore.acquire();
            ArrayList<BftTransaction> toProTrans=new ArrayList<>(pool);
            restTemplate.postForObject(protocolurl,toProTrans,boolean.class);

            clearPool();
            semaphore.release();
            return true;


        } catch (RestClientException | InterruptedException e) {
            e.printStackTrace();
            semaphore.release();
            return false;
        }
    }

    @Override
    public void clearPool() {
        pool.clear();

    }
}
