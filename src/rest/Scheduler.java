package rest;

import methodRequests.MethodRequest;

public class Scheduler {
    private final ActivationQueue tasksQueue = new ActivationQueue();
    private final Thread schedulerThread;
    private boolean end = false;

    public Scheduler() {
        schedulerThread = new Thread(() -> {
            /* guard invocation inside checkAndDequeue*/
            while (!end) {
                MethodRequest mr = tasksQueue.checkAndDequeue();
                mr.execute();
            }
        });
        schedulerThread.start();
    }

    public void enqueue(MethodRequest mr) {
        tasksQueue.enqueue(mr);
    }


    public ActivationQueue getTasksQueue() {
        return tasksQueue;
    }
}
