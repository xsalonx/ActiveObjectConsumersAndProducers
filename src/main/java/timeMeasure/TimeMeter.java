package timeMeasure;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;


public class TimeMeter {

    private final int leftPadding = 25;
    private final int spaceBetweenCells = 2;
    private int linesLength = 250;

    static int cellWidth = 8;

    public final ArrayList<Float>[] producersCompletionTimes;
    public final ArrayList<Float>[] consumersCompletionTimes;

    private final Thread[] producersThreads;
    private final Thread[] consumersThreads;

    ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();


    public TimeMeter(int producersNumb, int consumersNumb, Thread[] producersThreads, Thread[] consumersThreads) {
        producersCompletionTimes = new ArrayList[producersNumb];
        for (int i=0; i<producersNumb; i++) {
            producersCompletionTimes[i] = new ArrayList<>();
        }
        consumersCompletionTimes = new ArrayList[consumersNumb];
        for (int i=0; i<consumersNumb; i++) {
            consumersCompletionTimes[i] = new ArrayList<>();
        }
        this.producersThreads = producersThreads;
        this.consumersThreads = consumersThreads;
    }

    public void logProducerTime(int index, float time) {
        producersCompletionTimes[index].add(time);
    }
    public void logConsumerTime(int index, float time) {
        consumersCompletionTimes[index].add(time);
    }

    private float[] avgTimePerWorker(ArrayList<Float>[] arr) {
        int length = arr.length;
        float[] avg = new float[length];
        for (int i=0; i<length; i++) {
            for (Float t : arr[i]) {
                avg[i] += t;
            }
            avg[i] /= arr[i].size();
            avg[i] /= 1000; // nano to micro
        }

        return avg;
    }



    public String toStringTimes() {
        StringBuilder stringBuilder = new StringBuilder();

        float[] producersTimes = avgTimePerWorker(producersCompletionTimes);
        float[] consumersTimes = avgTimePerWorker(consumersCompletionTimes);

        int[] producersMeasurements = new int[producersTimes.length];
        int[] consumersMeasurements = new int[consumersTimes.length];
        Arrays.setAll(producersMeasurements, i -> producersCompletionTimes[i].size());
        Arrays.setAll(consumersMeasurements, i -> consumersCompletionTimes[i].size());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        int n = Math.max(producersTimes.length, consumersTimes.length);
        stringBuilder
                .append(dtf.format(now)).append(' ')
                .append("-".repeat(linesLength)).append('\n')

                .append(getOnLineOfToString(IntStream.rangeClosed(0, n-1).toArray(), cellWidth, "worker index"))
                .append("\n\n")

                .append("\u001B[32m").append(getOnLineOfToString(producersTimes, cellWidth, "producers [micro]")).append("\u001B[0m")
                .append('\n')
                .append("\u001B[34m").append(getRowOfCPUTimes(producersThreads, "cpu t [mega clocks]")).append("\u001B[0m")
                .append('\n')
                .append(getOnLineOfToString(producersMeasurements, cellWidth, "number of meas."))
                .append("\n\n")

                .append("\u001B[32m").append(getOnLineOfToString(consumersTimes, cellWidth, "consumers [micro]")).append("\u001B[0m")
                .append('\n')
                .append("\u001B[34m").append(getRowOfCPUTimes(consumersThreads, "cpu t [mega clocks]")).append("\u001B[0m")
                .append('\n')
                .append(getOnLineOfToString(consumersMeasurements, cellWidth, "number of meas."))
                .append('\n')

                .append("-".repeat(linesLength));

        return stringBuilder.toString();
    }

    private String getRowOfCPUTimes(Thread[] arr, String rowTitle) {
        StringBuilder stringBuilder = new StringBuilder();

        long[] cpuT = new long[arr.length];
        Arrays.setAll(cpuT, i -> mxBean.getThreadCpuTime(arr[i].getId()));
        stringBuilder.append(rowTitle).append(" ".repeat(leftPadding - rowTitle.length()));
        float a;
        for (long t : cpuT) {
            a = ((float) t) / 1000000;
            String formattedFloat = String.format("%.01f", a);
            stringBuilder.append(" ".repeat(Math.max(cellWidth - formattedFloat.length() + spaceBetweenCells, 1)));
            stringBuilder.append(formattedFloat);
        }
        return stringBuilder.toString();

    }

    private String getOnLineOfToString(int[] arr, int cellWidth, String rowTitle) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rowTitle).append(" ".repeat(leftPadding - rowTitle.length()));
        for (int a : arr) {
            stringBuilder.append(" ".repeat(cellWidth - digitNumb(a) + spaceBetweenCells));
            stringBuilder.append(a);
        }
        return stringBuilder.toString();
    }
    private String getOnLineOfToString(float[] arr, int cellWidth, String rowTitle) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rowTitle).append(" ".repeat(leftPadding - rowTitle.length()));
        for (float a : arr) {
            String formattedFloat = String.format("%.02f", a);
            stringBuilder.append(" ".repeat(Math.max(cellWidth - formattedFloat.length() + spaceBetweenCells, 1)));
            stringBuilder.append(formattedFloat);
        }
        return stringBuilder.toString();
    }
    private int digitNumb(long n) {
        return (int) (Math.floor(Math.log10(Math.max(1, n))) + 1);
    }


}
