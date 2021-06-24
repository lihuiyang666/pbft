package com.huiyang.PBFTtrans;

import com.huiyang.consensusEngine.Transaction;

public class BftTransaction extends Transaction {

    public int counter;

    public BftTransaction() {
    }

    public BftTransaction(int counter) {
        this.counter = counter;
    }

    @Override
    public String toString() {
        return "BftTransaction{" +
                "counter=" + counter +
                '}';
    }
}
