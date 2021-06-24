package com.huiyang.PBFTtrans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bft")
public class PoolController {

    @Autowired
    PBFTPool pbftPool;

    @RequestMapping(value = "/transactions",method = RequestMethod.POST)
    public boolean addTrans(@RequestBody BftTransaction trans){
        System.out.println("接收到交易");
        boolean res= pbftPool.addToPool(trans);
        for (BftTransaction transaction:pbftPool.pool){
            System.out.println(transaction);
        }
        return res;

    }
    @RequestMapping(value = "/transactions/{counter}",method = RequestMethod.POST)
    public boolean addTrans(@PathVariable Integer counter){
        BftTransaction t=new BftTransaction(counter);
        System.out.println("接收到交易");
        //        for (BftTransaction transaction:pbftPool.pool){
//            System.out.println(transaction);
//        }
        return pbftPool.addToPool(t);

    }

}
