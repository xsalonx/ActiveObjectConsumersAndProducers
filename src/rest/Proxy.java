package rest;

import methodRequests.MethodRequest;
import methodRequests.PutDataMethodRequest;
import methodRequests.TakeDataMethodRequest;

public class Proxy {
    private final Scheduler scheduler;
    private final Servant servant;

    public enum reqTypes {
        PUT, TAKE
    };

    public Proxy(Servant servant) {
        scheduler = new Scheduler();
        this.servant = servant;
    }

    public Future<int[]> putData(int[] data) {
        Future<int[]> future = new Future<>();
        MethodRequest mr = new PutDataMethodRequest(future, servant, data);
        scheduler.enqueue(mr);
        return future;
    }

    public Future<int[]> takeData(int size) {
        Future<int[]> future = new Future<>();
        MethodRequest mr = new TakeDataMethodRequest(future, servant, size);
        scheduler.enqueue(mr);
        return future;
    }

}
