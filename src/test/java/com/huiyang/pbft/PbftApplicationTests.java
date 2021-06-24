package com.huiyang.pbft;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class PbftApplicationTests {

    @Test
    void contextLoads() {
        long start=System.currentTimeMillis();
        System.out.println(start);
        for (int i=0;i<1000;i++){
            String s="http://127.0.0.1:8080/bft/transactions/"+i;
            new RestTemplate().postForObject(s,null,boolean.class);
        }
        System.out.println(System.currentTimeMillis()-start);
//        new RestTemplate().postForObject("http://127.0.0.1:8080/bft/transactions/1",null,boolean.class);
    }

    @Test
    void calu(){

        System.out.println(492-277);
    }

}
