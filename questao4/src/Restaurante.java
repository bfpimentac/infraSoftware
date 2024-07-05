import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Define a classe Restaurante
public class Restaurante {
    // Constante que define o número de lugares no restaurante
    private static final int NUM_LUGARES = 5;
    // Variável que rastreia o número de lugares ocupados
    private int lugaresOcupados = 0;
    // Variável que rastreia o número de lugares que precisam ser liberados
    private int lugaresParaLiberar = 0;
    // Fila de espera para os personagens
    private Queue<Personagem> filaEspera = new LinkedList<>();
    // Lock para garantir exclusão mútua
    private Lock lock = new ReentrantLock();
    // Condition para gerenciar a disponibilidade de lugares
    private Condition temLugar = lock.newCondition();
    // Total de personagens que irão visitar o restaurante
    private int totalPersonagens;

    // Construtor que inicializa o restaurante com o número total de personagens
    public Restaurante(int totalPersonagens) {
        this.totalPersonagens = totalPersonagens;
    }

    // Método main que cria e inicia as threads para cada personagem
    public static void main(String[] args) {
        // Número total de personagens
        int totalPersonagens = 100;

        // Cria uma instância do restaurante com 100 personagens
        Restaurante restaurante = new Restaurante(totalPersonagens);

        // Cria e inicia uma thread para cada personagem
        for (int i = 1; i <= totalPersonagens; i++) {
            Personagem personagem = new Personagem("Pessoa " + i, restaurante);
            personagem.start();
        }
    }

    // Método que trata a ocupação de um lugar no restaurante
    public void ocuparLugar(Personagem personagem) throws InterruptedException {
        // Adquire o lock para garantir exclusão mútua
        lock.lock();
        try {
            // Espera até que todos os lugares estejam disponíveis
            while (lugaresOcupados == NUM_LUGARES) {
                // Adiciona o personagem à fila de espera se ainda não estiver nela
                if (!filaEspera.contains(personagem)) {
                    filaEspera.offer(personagem);
                    System.out.println(personagem.getNome() + " está esperando na fila.");
                }
                // Aguarda até que um lugar esteja disponível
                temLugar.await();
            }

            // Quando um lugar está disponível, permite que o próximo cliente sente
            lugaresOcupados++;
            System.out.println(personagem.getNome() + " está sentado em um dos lugares.");
            // Notifica todas as threads esperando para verificar a condição novamente
            temLugar.signalAll();

            // Espera até que todos os lugares estejam ocupados
            while (lugaresOcupados < NUM_LUGARES) {
                temLugar.await();
            }

            // Simula o tempo de jantar
            Thread.sleep(2000);
        } finally {
            // Libera o lock
            lock.unlock();
        }
    }

    // Método que trata a liberação de um lugar no restaurante
    public void liberarLugar() {
        // Adquire o lock para garantir exclusão mútua
        lock.lock();
        try {
            // Incrementa o contador de lugares para liberar
            lugaresParaLiberar++;
            // Se todos os lugares estiverem para serem liberados
            if (lugaresParaLiberar == NUM_LUGARES) {
                // Libera todos os lugares
                lugaresOcupados -= NUM_LUGARES;
                // Reseta o contador de lugares para liberar
                lugaresParaLiberar = 0;
                System.out.println("Todos os lugares estão livres. Próximo grupo pode entrar.");
                // Notifica todas as threads esperando para verificar a condição novamente
                temLugar.signalAll();
            }
        } finally {
            // Libera o lock
            lock.unlock();
        }
    }

    // Método que reduz o total de personagens de forma segura com lock
    public void reduzirTotalPersonagens() {
        lock.lock();
        try {
            totalPersonagens--;
        } finally {
            lock.unlock();
        }
    }

    // Método que retorna o total de personagens restantes de forma segura com lock
    public int getTotalPersonagens() {
        lock.lock();
        try {
            return totalPersonagens;
        } finally {
            lock.unlock();
        }
    }
}

// Define a classe Personagem que estende Thread
class Personagem extends Thread {
    // Nome do personagem
    private String nome;
    // Referência ao restaurante
    private Restaurante restaurante;
    // Flag para indicar se o personagem já jantou
    private boolean jaJantou = false;

    // Construtor que inicializa o personagem com um nome e uma referência ao restaurante
    public Personagem(String nome, Restaurante restaurante) {
        this.nome = nome;
        this.restaurante = restaurante;
    }

    // Método para obter o nome do personagem
    public String getNome() {
        return nome;
    }

    // Método run que define o comportamento da thread
    @Override
    public void run() {
        try {
            System.out.println(nome + " chegou ao restaurante.");
            while (!jaJantou) {
                // Tenta ocupar um lugar no restaurante
                restaurante.ocuparLugar(this);
                // Marca que o personagem já jantou
                jaJantou = true;
                System.out.println(nome + " terminou de jantar e saiu do restaurante.");
                // Libera o lugar ocupado pelo personagem
                restaurante.liberarLugar();
            }

            // Reduz o total de personagens e verifica se todos já jantaram
            restaurante.reduzirTotalPersonagens();
            if (restaurante.getTotalPersonagens() == 0) {
                System.out.println("Todos os personagens jantaram uma vez. Encerrando o restaurante.");
                // Encerra o programa
                System.exit(0);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
