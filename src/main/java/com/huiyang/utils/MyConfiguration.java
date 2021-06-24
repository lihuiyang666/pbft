package com.huiyang.utils;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MyConfiguration {



    @Bean
    @Scope(value = "prototype")
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        factory.setConnectTimeout(15000);
        // 设置代理
        //factory.setProxy(null);
        return factory;
    }

    @Bean
    public String protocolurl(){
        return "http://127.0.0.1:8080/bft/process";
    }

    @Bean
    public String getNeturl(){
        return "http://127.0.0.1:8080/bft/accounts/getlist";
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);//我这里设置的线程数是2,可以根据需求调整
        return taskScheduler;
    }

    @Bean
    public String getaddressurl(){
        return "http://127.0.0.1:8080/pow/accounts/getlist";
    }

    @Bean
    public String getip(){
        return "http://127.0.0.1:8080/pow/process/getip";
    }



}
