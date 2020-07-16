package com.lizhe.concurr.aqs;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierDemo {
    private static CyclicBarrier cyclicBarrier = new CyclicBarrier(8, () -> {
        System.out.println("8位运动员就位, 发令枪响...");
    });

    public static void main(String[] args) {
        System.out.println("运动员入场....");
        for (int i =0; i < 8; i++) {
            new Thread(new Runner()).start();
        }
    }
    static class Runner implements Runnable {

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + "运动员准备好了");
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "动起来了");
        }
    }
}
