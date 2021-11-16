package rest;

import methodRequests.MethodRequest;

public class Scheduler {
    private final ActivationQueue tasksQueue = new ActivationQueue();
    private final Thread schedulerThread;
    private boolean end = false;

    public Scheduler() {
        schedulerThread = new Thread(() -> {
            while (!end) {
                MethodRequest mr = tasksQueue.checkAndDequeue();
                if (mr.guard())
                    mr.execute();
                else
                    tasksQueue.enqueueBack(mr);
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
