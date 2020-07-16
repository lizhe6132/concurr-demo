package com.lizhe.concurr.aqs;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchDemo {
    private static final CountDownLatch latch = new CountDownLatch(3);

    public static void main(String[] args) {
        for (int i =0; i< 3; i++) {
            new Thread(new Worker()).start();
        }
        new Thread(new Waiter()).start();
    }
    static class Worker implements Runnable {

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + "已完成工作");
            latch.countDown();
        }
    }
    static class Waiter implements Runnable {

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + "前面的工作未完成,我在等待");
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "前面的工作已完成，我来统一提交");
        }
    }
}
