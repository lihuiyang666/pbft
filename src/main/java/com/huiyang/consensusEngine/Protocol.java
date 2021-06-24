package com.huiyang.consensusEngine;

import java.util.ArrayList;

public interface Protocol {

    boolean getNet();

    void execute(ArrayList<Transaction> trans);

}
