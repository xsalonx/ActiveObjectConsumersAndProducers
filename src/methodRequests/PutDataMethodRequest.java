package methodRequests;

import rest.Future;
import rest.Proxy;
import rest.Servant;

public class PutDataMethodRequest implements MethodRequest {

    private final int[] data;
    private final Servant servant;
    private final Future<int[]> future;

    public PutDataMethodRequest(Future<int[]> future, Servant servant, int[] data) {
        this.future = future;
        this.servant = servant;
        this.data = data;
    }

    @Override
    public boolean guard() {
        return data.length <= servant.getLeftSpace();
    }

    @Override
    public void execute() {
        servant.putData(data);
        future.setResult(null);
    }

    @Override
    public String getType() {
        return Proxy.reqTypes.PUT.name();
    }
}
