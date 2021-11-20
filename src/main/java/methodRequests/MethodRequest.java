package methodRequests;

public interface MethodRequest {

    boolean guard();
    void execute();
    String getType();
}
