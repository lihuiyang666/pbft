package com.huiyang.PBFTprotocol;

import com.huiyang.PBFTtrans.BftTransaction;
import com.huiyang.consensusEngine.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/bft")
public class ProtocolController {
    @Autowired
    PBFTProtocol pbftProtocol;

    @RequestMapping(value = "/process",method = RequestMethod.POST)
    public boolean process(@RequestBody ArrayList<BftTransaction> toPoolTrans){
        boolean b=pbftProtocol.getNet();

        if (b){
            ArrayList<Transaction> now=new ArrayList<>(toPoolTrans);
            pbftProtocol.execute(now);
            System.out.println("成功发送事务给共识处理模块");
            return true;
        }
        else {
            System.out.println("发送事务失败,当前不存在replicas");
            return false;
        }


    }

}
