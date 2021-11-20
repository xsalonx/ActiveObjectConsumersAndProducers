package rest;

import methodRequests.MethodRequest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rest.Proxy.reqTypes;

public class ActivationQueue {
    static private final int oneQueueSizeBound = 128;
    static private final reqTypes[] mrTypes = reqTypes.values();
    private final HashMap<String, LinkedList<MethodRequest>> tasksQueues;

    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private final HashMap<String, Condition> mrTypeToCond;

    private int currentToDequeueIndex = 0;


    public ActivationQueue() {
        tasksQueues = new HashMap<>();
        mrTypeToCond = new HashMap<>();

        for (reqTypes t : mrTypes) {
            tasksQueues.put(t.name(), new LinkedList<>());
            mrTypeToCond.put(t.name(), lock.newCondition());
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ActivationQueue< ");
        for (reqTypes t : mrTypes) {
            stringBuilder.append(t.name()).append("::").append(tasksQueues.get(t.name()).size()).append(", ");
        }
        return stringBuilder.toString();
    }

    private boolean isEmpty() {
        boolean isEmpty = true;
        for (LinkedList<MethodRequest> l : tasksQueues.values()) {
            isEmpty = isEmpty && l.isEmpty();
        }
        return isEmpty;
    }


    public MethodRequest dequeue() {
        lock.lock();
<<<<<<< HEAD:src/rest/ActivationQueue.java
        MethodRequest mr;

        while (true) {
            mr = checkEachQueue();
            if (mr != null) {
                lock.unlock();
                return mr;
            }
            waitEmpty();
        }
=======
        if (isEmpty())
            wait_();
        return popFromOneQueue();
>>>>>>> timeMeter:src/main/java/rest/ActivationQueue.java
    }

    private MethodRequest popFromOneQueue() {
        int i = 0;
        String mrType;
        MethodRequest mr = null;
        LinkedList<MethodRequest> currentQueue;

        while (i < mrTypes.length) {
            mrType = mrTypes[(currentToDequeueIndex + i) % mrTypes.length].name();
            currentQueue = tasksQueues.get(mrType);

            if (!currentQueue.isEmpty()) {
                mr = currentQueue.getFirst();
                mrTypeToCond.get(mr.getType()).signal();
                currentToDequeueIndex += (i + 1);
                currentToDequeueIndex %= mrTypes.length;
                mr = currentQueue.pop();
                break;
            }

            i++;
        }
        return mr;
    }

<<<<<<< HEAD:src/rest/ActivationQueue.java
    public void waitNoneExecutable() {
        lock.lock();
        try {
            System.out.println("cannot execute : none of requests meet requirements");
            cond.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waitEmpty() {
=======

    public void wait_() {
>>>>>>> timeMeter:src/main/java/rest/ActivationQueue.java
        try {
            cond.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enqueueBack(MethodRequest mr) {
        lock.lock();
        tasksQueues.get(mr.getType()).addFirst(mr);
        lock.unlock();
    }

    public void enqueue(MethodRequest mr) {
        lock.lock();
        while (tasksQueues.get(mr.getType()).size() >= oneQueueSizeBound) {
            try {
                mrTypeToCond.get(mr.getType()).await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        tasksQueues.get(mr.getType()).addLast(mr);
        cond.signal();
        lock.unlock();
    }



}
