package com.huiyang.PBFTnet;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;

@RestController
@RequestMapping("/bft")
public class NetController {

    @Autowired
    PBFTNet pbftNet;

    @RequestMapping(value ="/accounts",method = RequestMethod.POST)
    public boolean register(@RequestBody BftAccount newAccount){

        boolean res=pbftNet.addMember(newAccount);

        System.out.println(pbftNet.gethostlist());
        return res;
    }

    @RequestMapping(value ="/accounts",method = RequestMethod.GET)
    public HashSet<BftAccount> getAccounts(){
        return pbftNet.getAccounts();


    }

    @RequestMapping(value ="/accounts/{host}/{port}",method = RequestMethod.POST)
    public boolean register(@PathVariable String host,@PathVariable Integer port){
        BftAccount t=new BftAccount(host,port);


        boolean res=pbftNet.addMember(t);

        System.out.println(pbftNet.gethostlist());
        return res;
    }

    @RequestMapping(value = "/accounts/getlist",method = RequestMethod.GET)
    public HashSet<String> gethostlist(){
        return pbftNet.gethostlist();
    }


}
