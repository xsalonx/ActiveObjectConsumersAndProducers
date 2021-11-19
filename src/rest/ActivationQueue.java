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


    public MethodRequest dequeue() {
        lock.lock();
        MethodRequest mr;

        while (true) {
            mr = checkEachQueue();
            if (mr != null) {
                lock.unlock();
                return mr;
            }
            waitIfEmpty();
        }
    }

    private MethodRequest checkEachQueue() {
        int i = 0;
        String mrType;
        MethodRequest mr;
        LinkedList<MethodRequest> currentQueue;

        while (i < mrTypes.length) {
            mrType = mrTypes[(currentToDequeueIndex + i) % mrTypes.length].name();
            currentQueue = tasksQueues.get(mrType);

            if (!currentQueue.isEmpty()) {
                mr = currentQueue.getFirst();
                mrTypeToCond.get(mr.getType()).signal();
                currentToDequeueIndex += (i + 1);
                currentToDequeueIndex %= mrTypes.length;
                return currentQueue.pop();
            }

            i++;
        }
        return null;
    }

    public void waitIfNoneExecutable() {
        lock.lock();
        try {
//            System.out.println("cannot execute : none of requests meet requirements");
            cond.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waitIfEmpty() {
        try {
//            System.out.println("queues are empty");
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
