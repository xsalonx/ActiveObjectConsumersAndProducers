package rest;

public class Servant {

    private final int[] buffer;

    int currentSize = 0;
    int putIndex = 0;
    int takeIndex = 0;

    public Servant(int size) {
        this.buffer = new int[size];
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        if (putIndex < takeIndex || currentSize == buffer.length) {
            stringBuilder.append("+".repeat(putIndex));
            stringBuilder.append(".".repeat(takeIndex - putIndex));
            stringBuilder.append("+".repeat(buffer.length - takeIndex));
        } else {
            stringBuilder.append(".".repeat(takeIndex));
            stringBuilder.append("+".repeat(putIndex - takeIndex));
            stringBuilder.append(".".repeat(buffer.length - putIndex));
        }
        stringBuilder
                .append(']').append("  current size:").append(currentSize)
                .append(", putIndex: ").append(putIndex)
                .append(", take index: ").append(takeIndex);

        return stringBuilder.toString();
    }
    public void printBufferState() {
        System.out.println(toString());
    }

    public int getCurrentSize() {
        return currentSize;
    }
    public int getLeftSpace() {
        return buffer.length - currentSize;
    }

    public void putData(int[] data) {
        for (int i=0; i<data.length; i++)
            buffer[(putIndex + i) % buffer.length] = data[i];
        putIndex = (putIndex + data.length) % buffer.length;
        currentSize += data.length;
    }

    public int[] takeData(int size) {
        int[] data = new int[size];
        for (int i=0; i<size; i++)
            data[i] = buffer[(takeIndex + i) % buffer.length];
        takeIndex = (takeIndex + size) % buffer.length;
        currentSize -= size;
        return data;
    }

}
