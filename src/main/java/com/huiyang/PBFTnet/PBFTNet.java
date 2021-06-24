package com.huiyang.PBFTnet;

import com.huiyang.consensusEngine.Account;
import com.huiyang.consensusEngine.Network;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class PBFTNet implements Network {

    private CopyOnWriteArrayList<BftAccount> accounts;

    private CopyOnWriteArrayList<String> hostlist;

    PBFTNet(){
        accounts=new CopyOnWriteArrayList<>();
        hostlist=new CopyOnWriteArrayList<>();
    }



    @Override
    public boolean addMember(Account a) {
        try {
            BftAccount temp=(BftAccount) a;
            if (!accounts.contains(temp)){
                accounts.add(temp);
                hostlist.add(temp.Ip+":"+temp.host);
                return true;
            }
            else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    public HashSet<String> gethostlist(){
        return new HashSet<>(hostlist);


    }

    public HashSet<BftAccount> getAccounts(){
        return new HashSet<>(accounts);
    }

    @Override
    public Account searchAccount(String name) {
        return null;
    }

    @Override
    public void communication(Account a) {

    }
}
