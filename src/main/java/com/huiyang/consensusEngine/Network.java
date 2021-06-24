package com.huiyang.consensusEngine;

//import com.huiyang.Pojo.Account;



public interface Network {

    public boolean addMember(Account a);

    public Account searchAccount(String name);

    public void communication(Account a);

}
