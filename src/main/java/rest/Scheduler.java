package rest;

import methodRequests.MethodRequest;

public class Scheduler {
    private final ActivationQueue tasksQueue = new ActivationQueue();
    private final Thread schedulerThread;
    private boolean end = false;
    private static final int typesNumber = Proxy.reqTypes.values().length;

    public Scheduler() {
        schedulerThread = new Thread(() -> {
            while (!end) {
                passOverAllReqTypes();
            }
        });
        schedulerThread.start();
    }

    private void passOverAllReqTypes() {
        int i = 0;

        while (i < typesNumber) {
            MethodRequest mr = tasksQueue.dequeue();
            if (mr.guard()) {
                mr.execute();
                return;
            }
            else {
                tasksQueue.enqueueBack(mr);
                i++;
            }
        }
<<<<<<< HEAD:src/rest/Scheduler.java
        tasksQueue.waitNoneExecutable();
=======
        tasksQueue.wait_();
>>>>>>> timeMeter:src/main/java/rest/Scheduler.java
    }

    public void enqueue(MethodRequest mr) {
        tasksQueue.enqueue(mr);
    }


    public ActivationQueue getTasksQueue() {
        return tasksQueue;
    }
}
