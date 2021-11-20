package timeMeasure;

import rest.Future;

import java.util.Arrays;

public class OtherWorkersCalculations {

    static public void cRequiringFuture(int n, Future<int[]> future) {
        int[] fGet_ = future.get();
        double[] fGet = new double[fGet_.length];
        Arrays.setAll(fGet, (i) -> (double) fGet_[i]);

        double[] tmp = new double[fGet.length];

        for (int i=0; i<n; i++) {
            for (int j=0; j<fGet.length; j++)
                tmp[j] = fGet[j] + Math.sin(tmp[j] + Math.exp(tmp[j]));
        }
    }
    static public void notRequiringFuture(int n) {
        double tmp = 0;
        for (int i=0; i<n; i++) {
            tmp = Math.sin(tmp + Math.exp(tmp));
        }
    }
}
