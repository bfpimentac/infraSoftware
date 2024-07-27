import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.Random;

// Classe que representa um ônibus. É uma thread que simula o comportamento do ônibus na parada.
class Onibus extends Thread {
    private int assentosLivres; // Quantidade de assentos livres no ônibus
    private boolean disponivel; // Se o ônibus está disponível na parada
    private boolean naoEstaEmbarcando; // Se o ônibus está embarcando passageiros ou não

    private final ReentrantLock locker = new ReentrantLock(); // Lock para garantir acesso seguro às variáveis compartilhadas
    private final Condition podeEmbarcar = locker.newCondition(); // Condição que permite aos passageiros embarcarem
    private final PontoOnibus pontoOnibus; // Referência ao ponto de ônibus onde os passageiros esperam

    // Construtor da classe Onibus, inicializa as variáveis
    public Onibus(int assentosLivres, boolean disponivel, boolean naoEstaEmbarcando, PontoOnibus pontoOnibus) {
        this.assentosLivres = assentosLivres;
        this.disponivel = disponivel;
        this.naoEstaEmbarcando = naoEstaEmbarcando;
        this.pontoOnibus = pontoOnibus;
    }

    // Método chamado pelos passageiros para ocupar um assento no ônibus
    public void ocuparAssento(int numPassageiro) {
        locker.lock(); // Adquire o lock para acesso seguro às variáveis compartilhadas
        try {
            while (!naoEstaEmbarcando || assentosLivres <= 0) { // Se o ônibus não está embarcando ou não há assentos livres
                podeEmbarcar.await(); // Aguarda a condição para poder embarcar
            }
            assentosLivres--; // Ocupa um assento
            pontoOnibus.passageiroEmbarcou();
            System.out.println("O passageiro " + numPassageiro + " ocupou um assento com sucesso! Assentos livres restantes: " + assentosLivres);

            if (assentosLivres == 0) { // Se todos os assentos estão ocupados
                System.out.println("O ônibus está cheio!");
                naoEstaEmbarcando = false; // O ônibus para de embarcar passageiros
            }
        } catch (InterruptedException e) {
            System.out.println("Erro: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
        } finally {
            locker.unlock(); // Libera o lock
        }
    }

    // Método para verificar a disponibilidade do ônibus
    public boolean getDisponibilidade() {
        locker.lock();
        try {
            return disponivel;
        } finally {
            locker.unlock();
        }
    }

    @Override
    public void run() {
        while (true) {
            // Simula o tempo de espera até o próximo ônibus chegar
            Random random = new Random();
            int min = 1000;
            int max = 3000;
            int tempoEspera = random.nextInt(max - min + 1) + min;

            locker.lock(); // Adquire o lock para acesso seguro às variáveis compartilhadas
            try {
                // Checa se há passageiros esperando na parada. Se não, o ônibus vai embora
                if (pontoOnibus.getPassageirosEsperando() == 0) {
                    System.out.println("Sem passageiros na parada ou com passageiros que chegaram só quando o ônibus já estava embarcando. O ônibus está indo embora e o próximo chega em " + tempoEspera + " milissegundos");
                    disponivel = false;
                    naoEstaEmbarcando = false;
                    Thread.sleep(tempoEspera); // O ônibus "vai embora" por um tempo
                    assentosLivres = 50; // Restaura o número de assentos livres
                    disponivel = true;
                    naoEstaEmbarcando = true;
                    podeEmbarcar.signalAll(); // Notifica todos os passageiros que o ônibus está de volta e disponível
                }

                // Se o ônibus está cheio, ele vai embora
                if (assentosLivres == 0) {
                    System.out.println("O ônibus foi embora. O próximo chega em " + tempoEspera + " milissegundos");
                    disponivel = false;
                    naoEstaEmbarcando = false;
                    Thread.sleep(tempoEspera); // O ônibus "vai embora" por um tempo
                    assentosLivres = 50; // Restaura o número de assentos livres
                    disponivel = true;
                    naoEstaEmbarcando = true;
                    podeEmbarcar.signalAll(); // Notifica todos os passageiros que um novo ônibus está disponível
                }
            } catch (InterruptedException e) {
                System.out.println("Erro: " + e.getMessage());
                Thread.currentThread().interrupt(); // Restaura o status de interrupção
            } finally {
                locker.unlock(); // Libera o lock
            }
        }
    }
}

// Classe que representa um passageiro que chega na parada e tenta embarcar no ônibus
class Passageiro extends Thread {
    private final int id; // ID do passageiro
    private boolean embarcou; // Se o passageiro já embarcou no ônibus
    private final Onibus onibus; // Referência ao ônibus
    private final PontoOnibus pontoOnibus; // Referência ao ponto de ônibus
    private final ReentrantLock locker = new ReentrantLock(); // Lock para sincronização dentro da thread Passageiro

    public Passageiro(int id, boolean embarcou, Onibus onibus, PontoOnibus pontoOnibus) {
        this.id = id;
        this.embarcou = embarcou;
        this.onibus = onibus;
        this.pontoOnibus = pontoOnibus;
    }

    @Override
    public void run() {
        try {
            pontoOnibus.passageiroChegou(); // Registra a chegada do passageiro no ponto de ônibus
            System.out.println("O passageiro " + id + " acabou de chegar no ponto de ônibus");

            while (!embarcou) { // Enquanto o passageiro não embarcar
                locker.lock(); // Adquire o lock para sincronização
                try {
                    if (onibus.getDisponibilidade()) { // Se o ônibus está disponível
                        onibus.ocuparAssento(id); // O passageiro tenta ocupar um assento
                        embarcou = true; // Marca que o passageiro embarcou
                    }
                } finally {
                    locker.unlock(); // Libera o lock
                }
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}

// Classe que representa o ponto de ônibus onde os passageiros esperam
class PontoOnibus {
    private int passageirosEsperando; // Número de passageiros esperando na parada
    private final ReentrantLock locker = new ReentrantLock(); // Lock para garantir acesso seguro às variáveis compartilhadas

    public PontoOnibus(int passageirosEsperando) {
        this.passageirosEsperando = passageirosEsperando;
    }

    // Método para registrar a chegada de um passageiro
    public void passageiroChegou() {
        locker.lock(); // Adquire o lock para sincronização
        try {
            passageirosEsperando++; // Incrementa o número de passageiros esperando
        } finally {
            locker.unlock(); // Libera o lock
        }
    }

    // Método para obter o número de passageiros esperando
    public int getPassageirosEsperando() {
        locker.lock(); // Adquire o lock para sincronização
        try {
            return passageirosEsperando; // Retorna o número de passageiros esperando
        } finally {
            locker.unlock(); // Libera o lock
        }
    }

    // Método para decrementar o número de passageiros esperando na parada após o embarque
    public void passageiroEmbarcou() {
        locker.lock();
        try {
            passageirosEsperando--; // Retorna o número de passageiros esperando
        } finally {
            locker.unlock(); // Libera o lock
        }
    }
}

// Classe principal que inicializa o ponto de ônibus, o ônibus e os passageiros
public class Main {
    public static void main(String[] args) {
        PontoOnibus ponto = new PontoOnibus(0); // Cria um ponto de ônibus inicializando com 0 passageiros
        Onibus onibus = new Onibus(50, true, true, ponto); // Cria um ônibus com 50 assentos, disponível e pronto para embarcar
        onibus.start(); // Inicia a thread do ônibus

        // Cria 100 passageiros e inicia cada um em uma thread separada
        for (int i = 1; i <= 100; i++) {
            new Passageiro(i, false, onibus, ponto).start();
        }
    }
}
