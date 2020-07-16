package com.lizhe.concurr.aqs;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockDemo {
    public static void main(String[] args) {
        Data data = new Data();
        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        data.get();
                    }
                }
            }, "读锁线程-" + i).start();

        }

        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        data.put(new Random().nextInt(10000));
                    }
                }
            }, "写锁线程-" + i).start();

        }
    }

}
class Data {
    private Object data = 0;// 共享数据，只能有一个线程能写该数据，但可以有多个线程同时读该数据。
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    public void get() {
        // 上读锁,其它线程只能读，不能写
        readWriteLock.readLock().lock();
        System.out.println(Thread.currentThread().getName() + "开始读数据");
        try {
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 1000));
            System.out.println(Thread.currentThread().getName() + "读数据完成");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();//释放读锁
        }
    }
    public void put(Object data) {
        readWriteLock.writeLock().lock();
        System.out.println(Thread.currentThread().getName() + "开始写数据");
        try {
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * 1000));
            this.data = data;
            System.out.println(Thread.currentThread().getName() + "写数据完成");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();//释放读锁
        }
    }
}
