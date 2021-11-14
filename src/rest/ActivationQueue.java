package rest;

import methodRequests.MethodRequest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rest.Proxy.reqTypes;

public class ActivationQueue {
    static private final int oneQueueSizeBound = 256;
    private final reqTypes[] types;
    private final HashMap<String, Integer> typeToIndex;
    private final HashMap<String, LinkedList<MethodRequest>> tasksQueues;

    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private final HashMap<String, Condition> typeToCond;

    private int currentToDequeueIndex = 0;
    private int lastIndexOfGuardFailure = -1;

    public ActivationQueue() {
        types = reqTypes.values();
        typeToIndex = new HashMap<>();
        tasksQueues = new HashMap<>();
        typeToCond = new HashMap<>();

        int i=0;
        for (reqTypes t : types) {
            tasksQueues.put(t.name(), new LinkedList<>());
            typeToIndex.put(t.name(), i);
            typeToCond.put(t.name(), lock.newCondition());
            i++;
        }
    }

    public String getState() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ActivationQueue< ");
        for (reqTypes t : types) {
            stringBuilder.append(t.name()).append("::").append(tasksQueues.get(t.name()).size()).append(", ");
        }
        stringBuilder.append(">\n");
        return stringBuilder.toString();
    }

    public MethodRequest dequeue() {
        lock.lock();
        int i = 0;
        MethodRequest mr;
        while (true) {
            while (i < types.length) {
                if (!tasksQueues.get(types[(currentToDequeueIndex + i) % types.length].name()).isEmpty()) {
                    mr = tasksQueues.get(types[(currentToDequeueIndex + i) % types.length].name()).pop();
                    typeToCond.get(mr.getType()).signal();
                    currentToDequeueIndex += (i + 1);
                    currentToDequeueIndex %= types.length;
                    lock.unlock();
                    return mr;
                }
                i++;
            }
            i = 0;
            try {
                System.out.println("empty");
                cond.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void enqueueBack(MethodRequest mr) {
        lock.lock();
        String type = mr.getType();
        tasksQueues.get(type).addFirst(mr);

        int currentIndexOfGuardLastFailure = typeToIndex.get(mr.getType());
        if (currentIndexOfGuardLastFailure == lastIndexOfGuardFailure) {
            try {
                System.out.println("cond enqueue back and wait ,,,," + getState());
                cond.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            lastIndexOfGuardFailure = currentIndexOfGuardLastFailure;
        }

        lock.unlock();
    }

    public void enqueue(MethodRequest mr) {
        lock.lock();
        while (tasksQueues.get(mr.getType()).size() >= oneQueueSizeBound)
            try {
                typeToCond.get(mr.getType()).await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        tasksQueues.get(mr.getType()).addLast(mr);
        cond.signal();
        lock.unlock();

    }


}
