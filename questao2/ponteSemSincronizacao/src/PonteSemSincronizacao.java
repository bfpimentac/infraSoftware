import java.util.Random;

// Classe que representa um carro como uma thread
class Carro extends Thread {
    private String nome;
    private String direcao;

    // Construtor da classe Carro
    public Carro(String nome, String direcao) {
        this.nome = nome;
        this.direcao = direcao;
    }

    // Método executado quando a thread é iniciada
    @Override
    public void run() {
        System.out.println(nome + " está esperando para entrar na ponte.");
        System.out.println(nome + " está atravessando a ponte na direção " + direcao + ".");
        try {
            // Simula o tempo necessário para atravessar a ponte (1 a 3 segundos)
            Thread.sleep(new Random().nextInt(2000) + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(nome + " saiu da ponte.");
    }
}

public class PonteSemSincronizacao {
    public static void main(String[] args) {
        // Cria um array de carros (threads)
        Carro[] carros = new Carro[10];
        for (int i = 0; i < carros.length; i++) {
            // Inicializa cada carro com um nome e uma direção aleatória
            carros[i] = new Carro("Carro " + i, new Random().nextBoolean() ? "Esquerda" : "Direita");
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