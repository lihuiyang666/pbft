package com.huiyang.consensusEngine;

public interface ConsensusDriver {

    void setNetwork(Network network);

    Network getNetwork();

    void setTransactionPool(TransactionPool transactionPool);

    TransactionPool getTransactionPool();

    void setProtocol(Protocol protocol);

    Protocol getProtocol();

    void setOutcome(Outcome outcome);

    Outcome getOutcome();
}
