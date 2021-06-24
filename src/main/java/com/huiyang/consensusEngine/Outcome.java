package com.huiyang.consensusEngine;

public interface Outcome {

    //产生结果,由账户通过反射调用
    Block geneOutcome(Block block);

    //验证区块
    boolean verifyBlock(Block block);
}
