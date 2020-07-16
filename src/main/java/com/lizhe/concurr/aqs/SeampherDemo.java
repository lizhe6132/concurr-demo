package com.lizhe.concurr.aqs;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SeampherDemo {
    private static final ConnectionManager manager = new ConnectionManager(3);//只有3个连接可用

    public static void main(String[] args) {
        for (int i =0 ; i < 10; i++) {
            new Thread(() -> {
                manager.getConnection();
            }).start();
        }
    }

    static class ConnectionManager {
        private Semaphore semaphore;
        public ConnectionManager(int count) {
            this.semaphore = new Semaphore(count);
        }
        public void getConnection() {
            try {
                semaphore.acquire();// 获取许可
                System.out.println(Thread.currentThread().getName() + "获取到数据库连接，做业务逻辑");
                TimeUnit.SECONDS.sleep(1);
                System.out.println(Thread.currentThread().getName() + "业务处理完，归还连接");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }
    }
}
