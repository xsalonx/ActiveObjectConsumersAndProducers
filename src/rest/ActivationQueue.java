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
    private final reqTypes[] types;
    private final HashMap<String, LinkedList<MethodRequest>> tasksQueues;

    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private final HashMap<String, Condition> typeToCond;

    private int currentToDequeueIndex = 0;


    public ActivationQueue() {
        types = reqTypes.values();
        tasksQueues = new HashMap<>();
        typeToCond = new HashMap<>();

        for (reqTypes t : types) {
            tasksQueues.put(t.name(), new LinkedList<>());
            typeToCond.put(t.name(), lock.newCondition());
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ActivationQueue< ");
        for (reqTypes t : types) {
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
        String type;
        MethodRequest mr;
        LinkedList<MethodRequest> currentQueue;

        while (i < types.length) {
            type = types[(currentToDequeueIndex + i) % types.length].name();
            currentQueue = tasksQueues.get(type);

            if (!currentQueue.isEmpty()) {
                mr = currentQueue.getFirst();
                typeToCond.get(mr.getType()).signal();
                currentToDequeueIndex += (i + 1);
                currentToDequeueIndex %= types.length;
                return currentQueue.pop();
            }

            i++;
        }
        return null;
    }

    public void waitIfNoneExecutable() {
        lock.lock();
        try {
            System.out.println("cannot execute : none of requests meet requirements");
            cond.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waitIfEmpty() {
        try {
            System.out.println("queues are empty");
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
                typeToCond.get(mr.getType()).await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        tasksQueues.get(mr.getType()).addLast(mr);
        cond.signal();
        lock.unlock();
    }



}
