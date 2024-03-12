package com.xck.concurrent;

import org.checkerframework.checker.units.qual.C;

import java.util.concurrent.*;

public class CompleteFutureTest {

    public static void main(String[] args) throws Exception {
//        test1();
        test2();
    }

    /**
     * 1. 无法手动停止，只能通过中断的方式；
     * 2. 无法处理异常，需要手动添加try-catch
     * 3. 如果直接调用get会阻塞，如果调用isDone则会轮训，总是会阻塞而不会自动调用
     * 4. 当需要多个结果，需要轮训多个Future来判断isDone
     *
     * @throws Exception
     */
    public static void test1() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Integer> future = executorService.submit(new TestCallable());
        while (!future.isDone()) {
            System.out.println(Thread.currentThread().getName() + " wait future");
            future.cancel(true);
            Thread.sleep(500);
        }

        // 这里如果不判断cancell会出现java.util.concurrent.CancellationException异常
        if (!future.isCancelled() && future.get() == 0) {
            System.out.println(Thread.currentThread().getName() + " result is 0");
        } else {
            System.out.println(Thread.currentThread().getName() + " result not 0");
        }

        executorService.shutdown();
    }

    private static class TestCallable implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            Thread.sleep(500);
            System.out.println(Thread.currentThread().getName() + ", call end");
            return 0;
        }
    }

    public static void test2() {
//        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//
//        completableFuture.supplyAsync(() -> {
//                    // 执行相似度判断
//                    return 0.2;
//                }, executorService)
//                .exceptionally(ex -> {
//                    System.out.println(ex);
//
//                }).thenAccept((result) -> {
//                    System.out.println("thenAccept");
//                });
    }
}
