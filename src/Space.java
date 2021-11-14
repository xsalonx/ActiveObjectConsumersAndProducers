
import rest.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Random;
import java.util.Scanner;

/**
 * implementation of producers and consumers problem with two and four conditions with tracing threads' work
 * @author Łukasz Dubiel
 * */


public class Space {


    static class Worker implements Runnable {

        private final String name;
        private final Proxy proxy;
        private final int index;
        private final String role;
        private final Random random = new Random();


        Worker(String name, String role, Proxy proxy, int index) {
            this.name = name;
            this.role = role;
            this.proxy = proxy;
            this.index = index;
        }

        private int getRandSize() {
            if (index > alterPoint)
                return random.nextInt(dataSizeUpperBound_2 - dataSizeLowerBound_2 + 1) + dataSizeLowerBound_2;
            else
                return random.nextInt(dataSizeUpperBound_1 - dataSizeLowerBound_1 + 1) + dataSizeLowerBound_1;
        }

        private int[] genRandData() {
            Random random = new Random();
            int[] data = new int[getRandSize()];
            for (int i = 0; i < data.length; i++) {
                data[i] = random.nextInt(dataBound);
            }
            return data;
        }

        @Override
        public void run() throws IllegalArgumentException {
            int[] data;
            int size;
            while (!pseudoCond.end) {
                sleep(workersDelay);
                if (!pseudoCond.stop) {

                    if (role.equals("producer")) {
                        data = genRandData();
                        System.out.println("producer " + index + " size: " + data.length + " " + Arrays.toString(data));
                        Future<int[]> future = proxy.putData(data);
//                        future.waitForResult();
//                        System.out.println("producer " + index + " :: done");

                    } else if (role.equals("consumer")) {
                        size = getRandSize();
                        System.out.println("consumer " + index + " size: " + size);
                        Future<int[]> future = proxy.takeData(size);
                        data = future.get();
                        System.out.println("consumer " + index + " :: " + Arrays.toString(data));

                    } else {
                        throw new IllegalArgumentException("Incorrect role for worker");
                    }

                } else {
                    pseudoCond.wait_();
                }
            }
        }
    }

    static private int producersNumb;
    static private int consumersNumb;
    static private int bufferSize;

    static private final PseudoCond pseudoCond = new PseudoCond();
    static private int dataSizeUpperBound_1;
    static private int dataSizeLowerBound_1;
    static private int dataSizeUpperBound_2;
    static private int dataSizeLowerBound_2;

    static private int dataBound;
    static private int workersDelay;
    static private int alterPoint = Integer.MAX_VALUE;

    static private Servant servant;
    static private Scheduler scheduler;
    static private Proxy proxy;
    static private ActivationQueue activationQueue;


    public static void main(String[] args) {

        /*
         * set of parameters
         * */

        producersNumb = 250;
        consumersNumb = 250;
        bufferSize = 100;
        dataSizeUpperBound_1 = 40;
        dataSizeLowerBound_1 = 1;

        alterPoint = 10;

        dataSizeUpperBound_2 = 45;
        dataSizeLowerBound_2 = 40;

        dataBound = 10;
        workersDelay = 1;
        String filePath = "log1.txt";

        /**
         * end of set of parameters
         * */

        servant = new Servant(bufferSize);
        scheduler = new Scheduler();
        proxy = new Proxy(servant, scheduler);
        activationQueue = scheduler.getTasksQueue();


        Worker[] producers = initWorkers(producersNumb, "producer", proxy);
        Worker[] consumers = initWorkers(consumersNumb, "consumer", proxy);

        Thread[] producersThreads = declareWorkersThreads(producers);
        Thread[] consumersThreads = declareWorkersThreads(consumers);

        startThreads(producersThreads);
        startThreads(consumersThreads);


        runCLI();

        System.out.println("out of loop");
        pseudoCond.end = true;
        sleep(1000);

        joinThreads(consumersThreads);
        System.out.println("Consumers joined");
        joinThreads(producersThreads);
        System.out.println("Producers joined");
    }

    private static void runCLI() {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equals("end")) {
            input = scanner.nextLine();
            System.out.println("command: <" + input + ">");
            pseudoCond.stop = true;
            String[] commandAndParams = input.split(" ");

            sleep(500);
            switch (commandAndParams[0]) {
                case "continue":
                    System.out.println("Continuing");
                case "end":
                    pseudoCond.stop = false;
                    pseudoCond.notifyAll_();
                    break;
                case "queues":
                    System.out.println(activationQueue.getState());
                    break;
                case "state":
                    System.out.println(servant.toStringBufferState());
                    break;
            }
        }
    }


    private static Worker[] initWorkers(int n, String role, Proxy proxy) {
        Worker[] workers = new Worker[n];
        for (int i = 0; i < n; i++) {
            workers[i] = new Worker(role + i, role, proxy, i);
        }
        return workers;
    }

    private static Thread[] declareWorkersThreads(Worker[] workers) {
        Thread[] workersThreads = new Thread[workers.length];
        for (int i = 0; i < workers.length; i++) {
            workersThreads[i] = new Thread(workers[i]);
        }
        return workersThreads;
    }

    private static void startThreads(Thread[] threads) {
        for (Thread th : threads)
            th.start();
    }

    private static void joinThreads(Thread[] threads) {
        for (Thread th : threads)
            try {
                th.join();
            } catch (InterruptedException ignore) {
            }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {
        }
    }

}
