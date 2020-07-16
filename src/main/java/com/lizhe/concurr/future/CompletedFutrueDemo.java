package com.lizhe.concurr.future;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * T任务有n个子任务执行，当某一个任务失败时通知其它子任务结束执行/回滚
 */
public class CompletedFutrueDemo {
    private static List<Mytask> tasks = new ArrayList<>(3);
    private static volatile boolean canceled = false;
    private static Executor executor = Executors.newFixedThreadPool(6);
    public static void main(String[] args) throws IOException {
        Mytask task1 = new Mytask("task-1", 1, Result.SUCC);
        Mytask task2 = new Mytask("task-2", 2, Result.FAIL);
        Mytask task3 = new Mytask("task-3", 3, Result.SUCC);
        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);
        tasks.forEach(task -> {
            CompletableFuture future = CompletableFuture.supplyAsync(() -> task.run(), executor)
                    .thenAccept(result -> {
                        callBack(result, task);
                    });
        });
        System.in.read();
    }

    private static void callBack(Result result, Mytask task) {
        synchronized (tasks) {
            tasks.forEach(mytask -> {
                if (result == Result.FAIL && !task.taskName.equals(mytask.taskName)) {
                    mytask.cancel();
                }
            });
        }
    }


    //任务执行状态常量
    static enum Result{
        SUCC,FAIL,CANCELD
    }

    /**
     * 模拟n个子任务
     */
    private static class Mytask {
        private String taskName;
        // 任务消耗的时间
        private Integer usedTime;
        //任务执行状态
        private Result ret;
        public Mytask(String taskName, Integer usedTime, Result ret) {
            this.taskName = taskName;
            this.usedTime = usedTime;
            this.ret = ret;
        }
        public Result run() {
            int interval = 100;
            int total = 0;
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(interval);
                    total += interval;
                    if (total >= usedTime) {
                        break;
                    }
                    if (canceled) {
                        return Result.CANCELD;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(taskName + "end");
            return ret;
        }

        public void cancel() {
            boolean canceling = true;
            synchronized (this) {
                System.out.println(taskName + "canceling");
                try {
                    // 模拟任务处理
                    TimeUnit.SECONDS.sleep(usedTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(taskName + "canceled");
            }
            canceled = true;
        }
    }
}
