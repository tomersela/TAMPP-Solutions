import java.util.concurrent.locks.ReentrantLock;

public class NaivePhilosophers {

    static final int N = 5;
    static final int ROUNDS = 5;

    public static void main(String[] args) throws InterruptedException {
        ReentrantLock[] chopsticks = new ReentrantLock[N];
        for (int i = 0; i < N; i++)
            chopsticks[i] = new ReentrantLock();

        Thread[] phils = new Thread[N];
        for (int i = 0; i < N; i++) {
            final int id = i;
            final int left = i;
            final int right = (i + 1) % N;

            phils[i] = new Thread(() -> {
                try {
                    for (int r = 1; r <= ROUNDS; r++) {
                        // think
                        System.out.println("Phil " + id + " thinking");
                        Thread.sleep((long)(Math.random() * 40));

                        chopsticks[left].lock();
                        chopsticks[right].lock();

                        System.out.println("Phil " + id + " eating (round " + r + ")");
                        Thread.sleep((long)(Math.random() * 40));

                        chopsticks[right].unlock();
                        chopsticks[left].unlock();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        System.out.println("--- Naive Dining Philosophers (may deadlock) ---");
        for (Thread t : phils) t.start();

        for (Thread t : phils) t.join(5000);

        boolean deadlocked = false;
        for (Thread t : phils) {
            if (t.isAlive()) {
                deadlocked = true;
                t.interrupt();
            }
        }
        if (deadlocked)
            System.out.println("DEADLOCK detected -- had to kill threads.");
        else
            System.out.println("Everyone finished eating.");
    }
}
