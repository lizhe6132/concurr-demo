package com.lizhe.concurr.aqs;

import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class LockSurportDemo {

        public static void main(String[] args) {
            Thread thread = new Thread(() -> {
                LockSupport.park();
                System.out.println("thread线程被唤醒");
            });
            thread.start();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LockSupport.unpark(thread);
            /*java.util.concurrent.locks.Lock reentrantLock = new ReentrantLock(true);
            reentrantLock.lock();
            try {
                System.out.println("同步代码块");
            } finally {
                reentrantLock.unlock();
            }*/
        }


}
