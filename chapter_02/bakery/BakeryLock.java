import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

interface Timestamp extends Comparable<Timestamp> {
    boolean compare(Timestamp other);
}

interface TimestampSystem {
    Timestamp[] scan();
    void label(Timestamp timestamp, int i);
}

class UnboundedTimestamp implements Timestamp {
    private volatile long value;

    UnboundedTimestamp(long value) {
        this.value = value;
    }

    long getValue() { return value; }
    void setValue(long v) { this.value = v; }

    @Override
    public boolean compare(Timestamp other) {
        return this.value < ((UnboundedTimestamp) other).value;
    }

    @Override
    public int compareTo(Timestamp other) {
        return Long.compare(this.value, ((UnboundedTimestamp) other).value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}

class UnboundedTimestampSystem implements TimestampSystem {
    private final UnboundedTimestamp[] timestamps;

    UnboundedTimestampSystem(int n) {
        timestamps = new UnboundedTimestamp[n];
        for (int i = 0; i < n; i++)
            timestamps[i] = new UnboundedTimestamp(0);
    }

    @Override
    public Timestamp[] scan() {
        UnboundedTimestamp[] copy = new UnboundedTimestamp[timestamps.length];
        for (int i = 0; i < timestamps.length; i++)
            copy[i] = new UnboundedTimestamp(timestamps[i].getValue());
        return copy;
    }

    @Override
    public void label(Timestamp timestamp, int i) {
        Timestamp[] current = scan();
        long max = 0;
        for (Timestamp t : current) {
            long v = ((UnboundedTimestamp) t).getValue();
            if (v > max) max = v;
        }
        ((UnboundedTimestamp) timestamp).setValue(max + 1);
        timestamps[i] = (UnboundedTimestamp) timestamp;
    }
}

public class BakeryLock {
    private static final VarHandle FLAG_HANDLE =
            MethodHandles.arrayElementVarHandle(boolean[].class);

    private final int n;
    private final boolean[] flag;
    private final UnboundedTimestamp[] label;
    private final UnboundedTimestampSystem ts;

    public BakeryLock(int n) {
        this.n = n;
        flag = new boolean[n];
        label = new UnboundedTimestamp[n];
        ts = new UnboundedTimestampSystem(n);
        for (int i = 0; i < n; i++) {
            flag[i] = false;
            label[i] = new UnboundedTimestamp(0);
        }
    }

    public void lock(int i) {
        FLAG_HANDLE.setVolatile(flag, i, true);
        ts.label(label[i], i);

        for (int k = 0; k < n; k++) {
            if (k == i) continue;
            while ((boolean) FLAG_HANDLE.getVolatile(flag, k) && earlier(k, i)) {
                Thread.yield();
            }
        }
    }

    // true if (label[k], k) << (label[i], i)
    private boolean earlier(int k, int i) {
        long lk = label[k].getValue();
        long li = label[i].getValue();
        return (lk < li) || (lk == li && k < i);
    }

    public void unlock(int i) {
        FLAG_HANDLE.setVolatile(flag, i, false);
    }
}
