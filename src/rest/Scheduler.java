package rest;

import methodRequests.MethodRequest;

public class Scheduler {
    private final ActivationQueue tasksQueue = new ActivationQueue();
    private final Thread schedulerThread;
    private boolean end = false;

    public Scheduler() {
        schedulerThread = new Thread(() -> {
            while (!end) {
                MethodRequest mr = tasksQueue.dequeue();
                System.out.println(tasksQueue.getState());
                if (mr.guard()) {
                    System.out.println("scheduler: execute");
                    mr.execute();
                }
                else {
                    System.out.println("scheduler: enqueue back");
                    tasksQueue.enqueueBack(mr);
                }
            }
        });
        schedulerThread.start();
    }

    public void enqueue(MethodRequest mr) {
        tasksQueue.enqueue(mr);
    }

}
