package methodRequests;

import rest.Future;
import rest.Proxy;
import rest.Servant;

public class TakeDataMethodRequest implements MethodRequest {
    private final int size;
    private final Servant servant;
    private final Future<int[]> future;

    public TakeDataMethodRequest(Future<int[]> future, Servant servant, int size) {
        this.future = future;
        this.servant = servant;
        this.size = size;
    }

    @Override
    public boolean guard() {
        return servant.getCurrentSize() >= size;
    }

    @Override
    public void execute() {
        future.setResult(servant.takeData(size));
    }

    @Override
    public String getType() {
        return Proxy.reqTypes.TAKE.name();
    }
}
