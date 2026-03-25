import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class StarvationFreePhilosophers {

    static final int N = 5;
    static final int ROUNDS = 5;

    static final ReentrantLock lock = new ReentrantLock(true);
    static final Condition[] cond = new Condition[N];
    static final boolean[] eating = new boolean[N];

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < N; i++)
            cond[i] = lock.newCondition();

        Thread[] phils = new Thread[N];
        for (int i = 0; i < N; i++) {
            final int id = i;
            phils[i] = new Thread(() -> {
                try {
                    for (int r = 1; r <= ROUNDS; r++) {
                        System.out.println("Phil " + id + " thinking");
                        Thread.sleep((long)(Math.random() * 40));

                        pickUp(id);

                        System.out.println("Phil " + id + " eating (round " + r + ")");
                        Thread.sleep((long)(Math.random() * 40));

                        putDown(id);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        System.out.println("--- Starvation-Free Dining Philosophers (n=5) ---");
        for (Thread t : phils) t.start();
        for (Thread t : phils) t.join();
        System.out.println("Everyone finished eating.");
    }

    static int leftOf(int id)  { return (id + N - 1) % N; }
    static int rightOf(int id) { return (id + 1) % N; }

    static void pickUp(int id) throws InterruptedException {
        lock.lock();
        try {
            while (eating[leftOf(id)] || eating[rightOf(id)])
                cond[id].await();
            eating[id] = true;
        } finally {
            lock.unlock();
        }
    }

    static void putDown(int id) {
        lock.lock();
        try {
            eating[id] = false;
            cond[leftOf(id)].signal();
            cond[rightOf(id)].signal();
        } finally {
            lock.unlock();
        }
    }
}
