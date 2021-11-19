package timeMeasure;

import rest.Future;

public class OtherWorkersCalculations {

    public void cRequiringFuture(int n, Future<int[]> future) {
        int[] fGet = future.get();
        double[] tmp = new double[fGet.length];
        for (int i=0; i<n; i++) {
            tmp[i] = (double)fGet[i] + Math.sin(tmp[i] + Math.exp(tmp[i]));
        }
    }
    public void cNotRequiringFuture(int n) {
        double tmp = 0;
        for (int i=0; i<n; i++) {
            tmp = Math.sin(tmp + Math.exp(tmp));
        }
    }
}
