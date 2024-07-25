import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BanheiroUnissex {

    // Capacidade máxima do banheiro
    private static final int MAX_CAPACITY = 3;
    // Capacidade atual e gênero atual no banheiro ('N' para nenhum, 'M' para masculino, 'F' para feminino)
    private int currentCapacity = 0;
    private char currentGender = 'N';

    // Semáforo para controlar a capacidade do banheiro
    private final Semaphore semaphore = new Semaphore(MAX_CAPACITY);
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    // Fila para gerenciar a ordem das pessoas esperando para entrar no banheiro
    private final Queue<Pessoa> fila = new LinkedList<>();

    // Método para uma pessoa entrar no banheiro
    public void entrarBanheiro(char gender, int id) throws InterruptedException {
        lock.lock();
        try {
            // Cria uma nova pessoa com o gênero e ID fornecidos
            Pessoa pessoa = new Pessoa(gender, id);
            // Adiciona a pessoa na fila
            fila.add(pessoa);
            System.out.println((gender == 'M' ? "Homem " : "Mulher ") + id + " está esperando para entrar no banheiro.");

            // Espera até que a pessoa na frente da fila seja ela e que o banheiro esteja disponível para seu gênero
            while (fila.peek() != pessoa || (currentCapacity > 0 && currentGender != gender) || currentCapacity >= MAX_CAPACITY) {
                condition.await(); // Espera até que a condição permita a entrada
            }

            // Remove a pessoa da fila quando ela entra
            fila.poll();
            semaphore.acquire(); // Adquire o semáforo para entrar no banheiro
            currentCapacity++; // Aumenta a capacidade atual do banheiro
            currentGender = gender; // Define o gênero atual no banheiro

            System.out.println((gender == 'M' ? "Homem " : "Mulher ") + id + " entrou no banheiro.");
            if (currentCapacity == MAX_CAPACITY) {
                System.out.println("O banheiro está cheio.");
            }
            if (!fila.isEmpty()) {
                Pessoa proximo = fila.peek();
                System.out.println("Próximo na fila: " + (proximo.getGender() == 'M' ? "Homem " : "Mulher ") + proximo.getId());
            }
        } finally {
            lock.unlock();
        }
    }

    // Método para uma pessoa sair do banheiro
    public void sairBanheiro(char gender, int id, long tempoNoBanheiro) {
        lock.lock();
        try {
            currentCapacity--; // Diminui a capacidade atual do banheiro

            // Reseta o gênero atual quando o banheiro fica vazio
            if (currentCapacity == 0) {
                currentGender = 'N'; // Nenhum gênero está no banheiro
            }

            semaphore.release(); // Libera o semáforo para permitir que outras pessoas entrem
            condition.signalAll(); // Notifica todos que estão esperando para entrar

            System.out.println((gender == 'M' ? "Homem " : "Mulher ") + id + " saiu do banheiro após " + tempoNoBanheiro + "ms.");
        } finally {
            lock.unlock();
        }
    }

    // Classe interna estática representando uma pessoa
    private static class Pessoa {
        private final char gender; // Gênero da pessoa ('M' ou 'F')
        private final int id; // Identificador único da pessoa

        // Construtor da classe Pessoa
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
        // Cria uma instância da classe BanheiroUnissex
        BanheiroUnissex banheiro = new BanheiroUnissex();
        // Instância da classe Random para embaralhamento das threads
        Random random = new Random();

        // Classe interna implementando Runnable para gerenciar a execução das threads
        class PessoaRunnable implements Runnable {
            private final char gender; // Gênero da pessoa ('M' ou 'F')
            private final int id; // Identificador único da pessoa

            // Construtor da classe PessoaRunnable
            public PessoaRunnable(char gender, int id) {
                this.gender = gender;
                this.id = id;
            }

            @Override
            public void run() {
                try {
                    // Chama o método para entrar no banheiro
                    banheiro.entrarBanheiro(gender, id);
                    long tempoInicio = System.currentTimeMillis();
                    // Simula o tempo que a pessoa fica no banheiro
                    Thread.sleep((long) (Math.random() * 1000));
                    long tempoFim = System.currentTimeMillis();
                    // Chama o método para sair do banheiro
                    banheiro.sairBanheiro(gender, id, tempoFim - tempoInicio);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Cria e inicia as threads para 50 mulheres e 50 homens
        Thread[] threads = new Thread[100];
        for (int i = 0; i < 50; i++) {
            threads[i] = new Thread(new PessoaRunnable('F', i)); // Thread para uma mulher
            threads[50 + i] = new Thread(new PessoaRunnable('M', i)); // Thread para um homem
        }

        // Embaralha as threads para a execução aleatória
        for (int i = 0; i < threads.length; i++) {
            int randomIndex = random.nextInt(threads.length);
            Thread temp = threads[i];
            threads[i] = threads[randomIndex];
            threads[randomIndex] = temp;
        }

        // Inicia todas as threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Aguarda a conclusão de todas as threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
