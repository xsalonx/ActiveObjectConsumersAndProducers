package rest;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Future<T> {
    private boolean isDone = false;
    private T result = null;
    Lock lock = new ReentrantLock();
    Condition cond = lock.newCondition();

    public Future() {}
    
    public boolean isDone() {
        return isDone;
    }

    public void waitForResult() {
        lock.lock();
        if (!isDone) {
            try {
                cond.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lock.unlock();
    }

    public T get() {
        waitForResult();
        return result;
    }

    public void setResult(T result) {
        lock.lock();
        this.result = result;
        this.isDone = true;
        cond.signalAll();
        lock.unlock();
    }
}
