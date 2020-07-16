package com.lizhe.concurr.aqs;

import java.util.concurrent.locks.StampedLock;

/**
 * StampedLock 读写锁升级版,避免写锁饥饿
 * StampedLock是不可重入的，如果一个线程已经持有了写锁，再去获取写锁的话就会造成死锁。
 * StampedLock支持读锁和写锁的相互转换。我们知道ReentrantReadWriteLock中，当线程获取到写锁后，
 * 可以降级为读锁，但是读锁是不能直接升级为写锁的。而StampedLock提供了读锁和写锁相互转换的功能，
 * 使得该类支持更多的应用场景。
 *
 *
 * 读写锁在读线程非常多，写线程很少的情况下可能会导致写线程饥饿，
 * JDK1.8 新增的StampedLock通过乐观读锁来解决这一问题。
 * StampedLock有三种访问模式：
 * ①写锁writeLock：功能和读写锁的写锁类似
 * ②悲观读锁readLock：功能和读写锁的读锁类似
 * ③乐观读锁Optimistic reading：一种优化的读模式
 *
 * 所有获取锁的方法，都返回一个票据 Stamp，Stamp 为 0 表示获取失败，其余都表示成功；
 * 所有释放锁的方法，都需要一个票据 Stamp，这个 Stamp 必须是和成功获取锁时得到的 Stamp 一致。
 * 乐观读锁：乐观的认为在具体操作数据前其他线程不会对自己操作的数据进行修改，
 * 所以当前线程获取到乐观读锁的之后不会阻塞线程获取写锁。
 * 为了保证数据一致性，在具体操作数据前要检查一下自己操作的数据是否经过修改操作了，
 * 如果进行了修改操作，就重新读一次。因为乐观读锁不需要进行 CAS 设置锁的状态而只是简单的测试状态，
 * 所以在读多写少的情况下有更好的性能。
 */
public class StampedLockDemo {
    public static void main(String[] args) {
        Point point = new Point();
        for (int i =0; i < 9; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + point.getDistanceFromOrigin() + ",x:" + point.x + ",y:" + point.y);
                }
            }, "计算距离线程").start();
        }
        for (int i =0; i < 2; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    double x = Math.random();
                    double y = Math.random();
                    point.moveIfAtOrigin(x, y);
                    point.move(x, y);
                    System.out.println(Thread.currentThread().getName() + "移动至x:" + x + ",y:" + y);
                }
            }, "移动线程").start();
        }
    }

    static class Point{
        private double x, y;
        private final StampedLock stampedLock = new StampedLock();

        /**
         * 写锁示例
         * @param x
         * @param y
         */
        void move(double x, double y) {
            //先获取写锁
            long stamp = stampedLock.writeLock();
            // 更改数据
            try {
                this.x = this.x + x;
                this.y = this.y + y;
            } finally {
                // 释放锁
                stampedLock.unlockWrite(stamp);
            }

        }

        /**
         * 总结乐观读锁的使用步骤：
         * long stamp = lock.tryOptimisticRead();  // 非阻塞获取版本信息
         * copyVaraibale2ThreadMemory();           // 拷贝变量到线程本地堆栈
         * if(!lock.validate(stamp)){              // 校验
         *     long stamp = lock.readLock();       // 获取读锁
         *     try {
         *         copyVaraibale2ThreadMemory();   // 拷贝变量到线程本地堆栈
         *      } finally {
         *        lock.unlock(stamp);              // 释放悲观锁
         *     }
         *
         * }
         * useThreadMemoryVarables();              // 使用线程本地堆栈里面的数据进行操作
         * @return
         */
        double getDistanceFromOrigin() {
            // 获取乐观锁
            long stamp = stampedLock.tryOptimisticRead();
            // 先copy数据至方法体栈内
            double currentX = this.x;
            double currentY = this.y;
            double result = 0.0;
            // 检查票据是否可用,即写锁有没有被占用
            if (!stampedLock.validate(stamp)) {
                //如果写锁被占用，说明数据可能被修改过，则此时获取悲观读锁
                stamp = stampedLock.readLock(); // 获取悲观锁
                try {
                    //copy数据至方法体栈内
                    currentX = this.x;
                    currentY = this.y;
                } finally {
                    stampedLock.unlockRead(stamp);
                }
            }
            // 有可能不是最新的结果，因为这个时候也有可能有其它线程更改了x,y的值
            result = Math.sqrt(currentX * currentX + currentY * currentY);// 真正读取操作，返回计算结果(7)
            return result;
        }
        /**
         * 悲观读锁readLock
         * 如果当前坐标为原点则移动到指定的位置
         */
        void moveIfAtOrigin(double newX, double newY) {
            long stamp = stampedLock.readLock();// 获取悲观读锁(1)
            try {
                // 如果当前点在原点则移动(2)
                while (this.x == 0.0 && this.y == 0.0) {
                    // 尝试将获取的读锁升级为写锁(3)
                    long ws = stampedLock.tryConvertToWriteLock(stamp);
                    if (ws != 0L) {
                        // 升级成功，则更新票据，并设置坐标值，然后退出循环(4)
                        stamp = ws;
                        this.x = newX;
                        this.y = newY;
                        break;
                    } else {
                        // 读锁升级写锁失败，则释放读锁，显示获取独占写锁，然后循环重试(5)
                        stampedLock.unlockRead(stamp);
                        stamp = stampedLock.writeLock();
                    }
                }
            } finally {
                stampedLock.unlock(stamp);// 释放写锁(6)
            }
        }

    }
}
