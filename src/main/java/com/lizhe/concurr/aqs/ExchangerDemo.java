package com.lizhe.concurr.aqs;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

public class ExchangerDemo {
    private static Exchanger<String> exchanger = new Exchanger<>();
    static String goods = "电脑";
    static String money = "1000$";
    public static void main(String[] args) throws InterruptedException {
        System.out.println("准备交易，一手交钱一手交货");
        new Thread(new Sealner()).start();
        TimeUnit.SECONDS.sleep(2);
        new Thread(new Buyer()).start();
    }
    static class Sealner implements Runnable {

        @Override
        public void run() {
            System.out.println("卖家准备好货:" + goods);
            //等待换钱
            try {
                String money = exchanger.exchange(goods);
                System.out.println("卖家收到钱:" + money);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    static class Buyer implements Runnable {

        @Override
        public void run() {
            System.out.println("买家准备好钱:" + money);
            try {
                String googs = exchanger.exchange(money);
                System.out.println("买家收到货物:" + googs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
