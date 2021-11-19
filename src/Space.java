
import rest.*;
import timeMeasure.OtherWorkersCalculations;
import timeMeasure.TimeMeter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
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
            float start, end;

            while (!pseudoCond.end) {
                sleep(workersDelay);
                if (!pseudoCond.stop) {

                    if (role.equals("producer")) {
                        data = genRandData();
                        start = System.nanoTime();
                        Future<int[]> future = proxy.putData(data);
                        future.waitForResult();
                        end = System.nanoTime();
                        timeMeter.logProducerTime(index, end - start);
//                        System.out.println("producer " + index + " :: done");

                    } else if (role.equals("consumer")) {
                        size = getRandSize();
                        start = System.nanoTime();
                        Future<int[]> future = proxy.takeData(size);

                        OtherWorkersCalculations.cNotRequiringFuture(1000);
                        OtherWorkersCalculations.cRequiringFuture(100, future);

                        end = System.nanoTime();
                        timeMeter.logConsumerTime(index, end - start);
//                        System.out.println("consumer " + index + " :: " + Arrays.toString(data));

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

    static private TimeMeter timeMeter;

    public static void main(String[] args) {

        /*
         * set of parameters
         * */

        producersNumb = 20;
        consumersNumb = 15;
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

        timeMeter = new TimeMeter(producersNumb, consumersNumb, producersThreads, consumersThreads);

        startThreads(producersThreads);
        startThreads(consumersThreads);


        try {
            runCLI();
        } catch (IOException ignore ){}

        System.out.println("out of loop");
        pseudoCond.end = true;
        sleep(1000);

        joinThreads(consumersThreads);
        System.out.println("Consumers joined");
        joinThreads(producersThreads);
        System.out.println("Producers joined");
    }

    private static void runCLI() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        BufferedWriter writer = new BufferedWriter(new FileWriter("res.txt", true));

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
                case "state":
                    System.out.println(timeMeter.toStringTimes());
                    break;
                case "queues":
                    System.out.println(activationQueue);
                    break;
                case "buffer":
                    System.out.println(servant);
                    break;
                case "save":
                    writer.write(timeMeter.toStringTimes().replaceAll("\u001B\\[[;\\d]*m", ""));
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
