public class PseudoCond {

    public boolean end;
    public boolean stop;

    public PseudoCond() {
        end = false;
        stop = false;
    }
    public synchronized void wait_() {
        try {
            wait();
        } catch (InterruptedException ignore) {}
    }
    public synchronized void notifyAll_() {
        notifyAll();
    }
}