package com.huiyang.utils;

import org.springframework.context.annotation.Configuration;

@Configuration      //1.主要用于标记配置类，兼备Component的效果。
//@EnableScheduling   // 2.开启定时任务
public class SaticScheduleTask {
    //3.添加定时任务
//    @Scheduled(cron = "0/5 * * * * ?")
//    //或直接指定时间间隔，例如：5秒
//    //@Scheduled(fixedRate=5000)
//    private void configureTasks() {
//        System.err.println("执行静态定时任务时间: " + LocalDateTime.now());
//    }
//    private Logger logger = LoggerFactory.getLogger(SaticScheduleTask.class);
//
//    @Scheduled(cron="0/2 * * * * ? ")   //每2秒执行一次
//    public void testCron2() {
//        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        logger.info(sdf.format(new Date())+"*********每2秒执行一次");
//    }
//
//    @Scheduled(cron="0/3 * * * * ? ")   //每3秒执行一次
//    public void testCron3() {
//        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        logger.info(sdf.format(new Date())+"*********每3秒执行一次");
//    }
//
//
//    @Scheduled(cron="0/1 * * * * ? ")   //每1秒执行一次
//    public void testCron1() {
//        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        logger.info(sdf.format(new Date())+"*********每1秒执行一次");
//    }





}