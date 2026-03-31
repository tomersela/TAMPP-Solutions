import java.util.concurrent.locks.ReentrantLock;

public class DeadlockFreePhilosophers {

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

            // always grab the lower-numbered chopstick first
            final int first  = Math.min(left, right);
            final int second = Math.max(left, right);

            phils[i] = new Thread(() -> {
                try {
                    for (int r = 1; r <= ROUNDS; r++) {
                        System.out.println("Phil " + id + " thinking");
                        Thread.sleep((long)(Math.random() * 40));

                        chopsticks[first].lock();
                        chopsticks[second].lock();

                        System.out.println("Phil " + id + " eating (round " + r + ")");
                        Thread.sleep((long)(Math.random() * 40));

                        chopsticks[second].unlock();
                        chopsticks[first].unlock();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        System.out.println("--- Deadlock-Free Dining Philosophers ---");
        for (Thread t : phils) t.start();
        for (Thread t : phils) t.join();
        System.out.println("Everyone finished eating.");
    }
}
