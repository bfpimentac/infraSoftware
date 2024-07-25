import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class BanheiroUnissex {

    private static final int MAX_CAPACITY = 3;
    private int currentCapacity = 0;
    private char currentGender = 'N';

    private final Semaphore semaphore = new Semaphore(MAX_CAPACITY);
    private final Object catracaLock = new Object();
    private final Queue<Pessoa> fila = new LinkedList<>();
    private final Random random = new Random();

    public void entrarBanheiro(char gender, int id) throws InterruptedException {
        synchronized (catracaLock) {
            Pessoa pessoa = new Pessoa(gender, id);
            fila.add(pessoa);
            System.out.println((gender == 'M' ? "Homem " : "Mulher ") + id + " está esperando para entrar no banheiro.");

            while (fila.peek() != pessoa || (currentCapacity > 0 && currentGender != gender) || currentCapacity >= MAX_CAPACITY) {
                catracaLock.wait();
            }

            fila.poll();
            semaphore.acquire();
            currentCapacity++;
            currentGender = gender;

            System.out.println((gender == 'M' ? "Homem " : "Mulher ") + id + " entrou no banheiro.");
            if (currentCapacity == MAX_CAPACITY) {
                System.out.println("O banheiro está cheio.");
            }
            if (!fila.isEmpty()) {
                Pessoa proximo = fila.peek();
                System.out.println("Próximo na fila: " + (proximo.getGender() == 'M' ? "Homem " : "Mulher ") + proximo.getId());
            }
        }
    }

    public void sairBanheiro(char gender, int id, long tempoNoBanheiro) {
        synchronized (catracaLock) {
            currentCapacity--;

            if (currentCapacity == 0) {
                currentGender = 'N';
            }

            semaphore.release();
            catracaLock.notifyAll();

            System.out.println((gender == 'M' ? "Homem " : "Mulher ") + id + " saiu do banheiro após " + tempoNoBanheiro + "ms.");
        }
    }

    private static class Pessoa {
        private final char gender;
        private final int id;

        public Pessoa(char gender, int id) {
            this.gender = gender;
            this.id = id;
        }

        public char getGender() {
            return gender;
        }

        public int getId() {
            return id;
        }
    }

    public static void main(String[] args) {
        BanheiroUnissex banheiro = new BanheiroUnissex();
        Random random = new Random();

        class PessoaRunnable implements Runnable {
            private final char gender;
            private final int id;

            public PessoaRunnable(char gender, int id) {
                this.gender = gender;
                this.id = id;
            }

            @Override
            public void run() {
                try {
                    banheiro.entrarBanheiro(gender, id);
                    long tempoInicio = System.currentTimeMillis();
                    Thread.sleep((long) (Math.random() * 1000));
                    long tempoFim = System.currentTimeMillis();
                    banheiro.sairBanheiro(gender, id, tempoFim - tempoInicio);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Thread[] threads = new Thread[100];
        for (int i = 0; i < 50; i++) {
            threads[i] = new Thread(new PessoaRunnable('F', i));
            threads[50 + i] = new Thread(new PessoaRunnable('M', i));
        }

        for (int i = 0; i < threads.length; i++) {
            int randomIndex = random.nextInt(threads.length);
            Thread temp = threads[i];
            threads[i] = threads[randomIndex];
            threads[randomIndex] = temp;
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
