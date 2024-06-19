import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Classe que representa a ponte com sincronização
class Ponte {
    // Cria um lock para controlar o acesso à ponte
    private Lock lock = new ReentrantLock();

    // Método que simula a travessia da ponte por um carro
    public void atravessar(Carro carro) {
        lock.lock(); // Adquire o lock
        try {
            System.out.println(carro.getNome() + " está atravessando a ponte na direção " + carro.getDirecao() + ".");
            // Simula o tempo necessário para atravessar a ponte (1 a 3 segundos)
            Thread.sleep(new Random().nextInt(2000) + 1000);
            System.out.println(carro.getNome() + " saiu da ponte.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock(); // Libera o lock
        }
    }
}

// Classe que representa um carro como uma thread
class Carro extends Thread {
    private String nome;
    private String direcao;
    private Ponte ponte;

    // Construtor da classe Carro
    public Carro(String nome, String direcao, Ponte ponte) {
        this.nome = nome;
        this.direcao = direcao;
        this.ponte = ponte;
    }

    // Métodos getters para obter o nome e a direção do carro
    public String getNome() {
        return nome;
    }

    public String getDirecao() {
        return direcao;
    }

    // Método executado quando a thread é iniciada
    @Override
    public void run() {
        System.out.println(nome + " está esperando para entrar na ponte.");
        // Chama o método atravessar da ponte
        ponte.atravessar(this);
    }
}

public class PonteComSincronizacao {
    public static void main(String[] args) {
        // Instancia a ponte
        Ponte ponte = new Ponte();
        // Cria um array de carros (threads)
        Carro[] carros = new Carro[10];
        for (int i = 0; i < carros.length; i++) {
            // Inicializa cada carro com um nome, uma direção aleatória e a ponte
            carros[i] = new Carro("Carro " + i, new Random().nextBoolean() ? "Esquerda" : "Direita", ponte);
            // Inicia a thread do carro
            carros[i].start();
        }
        for (Carro carro : carros) {
            try {
                // Espera a conclusão de cada thread de carro
                carro.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}