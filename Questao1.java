import java.util.concurrent.locks.ReentrantLock;

// Criando uma classe para a conta
class Conta {
    private double saldo;
    private final ReentrantLock locker = new ReentrantLock();

    // Construtor da Conta, inicializando o valor do saldo na chamada da classe
    public Conta(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    // Definindo um método para depósitos
    public void Deposito(double valor) {
        locker.lock();
        try {
            saldo += valor;
            System.out.println("O valor R$" + valor + " foi depositado com sucesso! Saldo final: R$" + saldo);
        } finally {
            locker.unlock();
        }
    }

    // Definindo um método para saques
    public void Saque(double valor) {
        locker.lock();
        if (saldo >= valor) {
            try {
                saldo -= valor;
                System.out.println("O valor R$" + valor + " foi sacado com sucesso! Saldo final: R$" + saldo);
            } finally {
                locker.unlock();
            }
        } else {
            System.out.println("Seu saldo é de R$" + saldo + ", insuficiente para este saque!");
            locker.unlock();
        }
    }
}

// Criando uma classe para representar as pessoas que vão movimentar a conta (threads)
class Membro extends Thread {

    private final String nome; // Nome do membro da família
    private final String operacao; // Operação realizada (saque, depósito)
    private final double valor;
    private final Conta conta;

    // Construtor do Membro, que vai inicializar as variáveis declaradas na thread
    public Membro(String nome, String operacao, double valor, Conta conta) {
        this.nome = nome;
        this.operacao = operacao.toLowerCase();
        this.valor = valor;
        this.conta = conta;
    }

    @Override
    public void run(){
        System.out.println(nome + " está tentando fazer um " + operacao + " de R$" + valor);

        if (operacao.equals("deposito") || operacao.equals("depósito") || operacao.equals("depositar")) {
            conta.Deposito(valor);
        } else if (operacao.equals("saque") || operacao.equals("sacar")) {
            conta.Saque(valor);
        } else {
            System.out.println("Operação inválida. As operações disponíveis são apenas 'saque' ou 'deposito'.");
        }

    }
}

public class Questao1 {
    public static void main(String[] args) {

        Conta conta = new Conta(100000); // Inicializando a conta com R$100.000

        // Membros da família que vão movimentar a conta
        Thread mae = new Membro("Juliana", "saque", 30000, conta);
        Thread pai = new Membro("Felipe", "deposito", 20000, conta);
        Thread filho = new Membro("Angelo", "saque", 50000, conta);
        Thread filha = new Membro("Mariana", "deposito", 45000, conta);
        Thread tio = new Membro("Jorge", "saque", 42000, conta);
        Thread tia = new Membro("Carina", "deposito", 38000, conta);
        Thread avo = new Membro("Joana", "saque", 64000, conta);
        Thread primo = new Membro("Joao", "deposito", 33000, conta);
        Thread cachorro = new Membro("Mel", "saque", 15000, conta);

        // Inicializando as threads
        mae.start();
        pai.start();
        filho.start();
        filha.start();
        tio.start();
        tia.start();
        avo.start();
        primo.start();
        cachorro.start();

    }
}
