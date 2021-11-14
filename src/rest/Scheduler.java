package rest;

import methodRequests.MethodRequest;

public class Scheduler {
    private final ActivationQueue tasksQueue = new ActivationQueue();
    private final Thread schedulerThread;
    private boolean end = false;

    public Scheduler() {
        schedulerThread = new Thread(() -> {
            while (!end) {
//                System.out.println("scheduler");
                MethodRequest mr = tasksQueue.checkAndDequeue();
//                System.out.println(tasksQueue.getState());
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
