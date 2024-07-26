import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.Random;

// Criando uma classe para o ônibus
class Onibus extends Thread {
    private int assentosLivres;
    private boolean disponivel;
    private boolean embarcando;
    private final ReentrantLock locker = new ReentrantLock();
    private final Condition podeEmbarcar = locker.newCondition(); // Para sinalizar quando o ônibus está pronto para embarque
    private final PontoOnibus pontoOnibus;

    public Onibus(int assentosLivres, boolean disponivel, boolean embarcando, PontoOnibus pontoOnibus) {
        this.assentosLivres = assentosLivres;
        this.disponivel = disponivel;
        this.embarcando = embarcando;
        this.pontoOnibus = pontoOnibus;
    }

    public void ocuparAssento(int numPassageiro) {
        locker.lock();
        try {
            while (!embarcando || assentosLivres <= 0) {
                podeEmbarcar.await(); // Aguarda até que o ônibus esteja pronto para embarque
            }
            assentosLivres -= 1;
            System.out.println("O passageiro " + numPassageiro + " ocupou um assento com sucesso! Assentos livres restantes: " + assentosLivres);
            if (assentosLivres == 0) {
                System.out.println("O ônibus está cheio!");
                embarcando = false;
                podeEmbarcar.signalAll(); // Notifica todos os passageiros de que o ônibus não está mais disponível
            }
        } catch (InterruptedException e) {
            System.out.println("Erro: " + e.getMessage());
        } finally {
            locker.unlock();
        }
    }

    public boolean getDisponibilidade() {
        locker.lock();
        try {
            return disponivel;
        } finally {
            locker.unlock();
        }
    }

    public boolean getEmbarcando() {
        locker.lock();
        try {
            return embarcando;
        } finally {
            locker.unlock();
        }
    }

    public void run() {
        while (true) {
            Random random = new Random();
            int min = 1000;
            int max = 3000;
            int tempoEspera = random.nextInt(max - min + 1) + min;

            locker.lock();
            try {
                // Se o ônibus não está disponível ou não está embarcando, ele precisa esperar
                while (!disponivel) {
                    podeEmbarcar.await(); // Aguarda até que o ônibus esteja disponível
                }
                // Se o ônibus estiver cheio ou não houver passageiros esperando
                if (assentosLivres == 0 || pontoOnibus.getPassageirosEsperando() == 0) {
                    System.out.println("O ônibus foi embora. O próximo chega em " + tempoEspera + " milissegundos");
                    disponivel = false;
                    embarcando = false;
                    Thread.sleep(tempoEspera);
                    disponivel = true;
                    embarcando = true;
                    pontoOnibus.notifyAll(); // Notifica os passageiros que um novo ônibus está disponível
                }
            } catch (InterruptedException e) {
                System.out.println("Erro: " + e.getMessage());
            } finally {
                locker.unlock();
            }
        }
    }
}

// Criando uma classe para representar os passageiros (threads)
class Passageiro extends Thread {
    private final int id;
    private boolean embarcou;
    private final Onibus onibus;
    private final PontoOnibus pontoOnibus;

    public Passageiro(int id, boolean embarcou, Onibus onibus, PontoOnibus pontoOnibus) {
        this.id = id;
        this.embarcou = embarcou;
        this.onibus = onibus;
        this.pontoOnibus = pontoOnibus;
    }

    @Override
    public void run() {
        try {
            pontoOnibus.passageiroChegou();
            System.out.println("O passageiro " + id + " acabou de chegar no ponto de ônibus");
            while (!embarcou) {
                if (onibus.getDisponibilidade()) { // Se o ônibus estiver disponível
                    onibus.ocuparAssento(id);
                    embarcou = true;
                }
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}

class PontoOnibus {
    private int passageirosEsperando;
    private final ReentrantLock locker = new ReentrantLock();

    public PontoOnibus(int passageirosEsperando) {
        this.passageirosEsperando = passageirosEsperando;
    }

    public void passageiroChegou() {
        locker.lock();
        try {
            passageirosEsperando += 1;
        } finally {
            locker.unlock();
        }
    }

    public int getPassageirosEsperando() {
        locker.lock();
        try {
            return passageirosEsperando;
        } finally {
            locker.unlock();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        PontoOnibus ponto = new PontoOnibus(0);
        Onibus onibus = new Onibus(50, true, false, ponto);
        onibus.start();

        for (int i = 1; i <= 100; i++) {
            new Passageiro(i, false, onibus, ponto).start();
        }
    }
}
