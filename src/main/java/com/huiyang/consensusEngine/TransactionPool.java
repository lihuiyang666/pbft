package com.huiyang.consensusEngine;

import java.lang.reflect.Constructor;

public interface TransactionPool {

//    public abstract Transaction geneTran(Account a, Account b);

    public Constructor[] getConstrucors();
    //产生事务
//    public abstract Transaction genTran(String address,String transaction,byte[] signature);
    //添加到交易池
    public abstract boolean addToPool(Transaction transaction);
    //阈值检验
    public abstract void check();

    public abstract boolean sendToProtocol();
    //清空交易池
    public abstract void clearPool();

}
