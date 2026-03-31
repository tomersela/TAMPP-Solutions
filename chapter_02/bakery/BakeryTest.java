public class BakeryTest {

    static final int NUM_THREADS = 5;
    static final int ROUNDS = 20_000;
    static volatile int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        BakeryLock lock = new BakeryLock(NUM_THREADS);

        Thread[] threads = new Thread[NUM_THREADS];
        for (int t = 0; t < NUM_THREADS; t++) {
            final int id = t;
            threads[t] = new Thread(() -> {
                for (int r = 0; r < ROUNDS; r++) {
                    lock.lock(id);
                    try {
                        counter++;
                    } finally {
                        lock.unlock(id);
                    }
                }
            });
        }

        System.out.println("Bakery Lock test: " + NUM_THREADS + " threads, "
                + ROUNDS + " increments each");
        System.out.println("Expected final count: " + (NUM_THREADS * ROUNDS));

        long start = System.currentTimeMillis();
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Actual final count:   " + counter);
        if (counter == NUM_THREADS * ROUNDS)
            System.out.println("PASS -- mutual exclusion held.");
        else
            System.out.println("FAIL -- mutual exclusion violated!");
        System.out.println("Time: " + elapsed + " ms");
    }
}
