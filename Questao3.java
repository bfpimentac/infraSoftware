import java.util.concurrent.Semaphore;

class Barbearia {

    static final int MAX_CADEIRAS = 20; // Definindo o máximo de cadeiras na barbearia como 20

    static final Semaphore filaClientes = new Semaphore(0); // Semáforo que controla a fila de clientes
    static final Semaphore barbeiro = new Semaphore(0); // Semáforo que controla se o barbeiro está livre ou não
    static final Semaphore mutex = new Semaphore(1); // Semáforo para exclusão mútua

    static int clientesEsperando = 0; // Quantidade de clientes esperando nos assentos. É uma cópia do valor de filaClientes que precisa existir para comparações nas próximas partes do código
    static boolean barbeiroEstaDormindo = false;
    static boolean barbeiroEstaAtendendo = false;

    static class Barbeiro extends Thread {

        public static void EmAtendimento() {
            barbeiroEstaAtendendo = true;

            // Simulando a duração do atendimento (3s)
            try{
                System.out.println("Barbeiro está atendendo!");
                Thread.sleep(3000);
                System.out.println("Atendimento finalizado!");
            } catch (InterruptedException e){
                System.out.println("Erro");
            }
        }

        @Override
        public void run() {
            while (true) {
                System.out.println("Barbeiro esperando clientes para atender (e dormindo por enquanto)");


                try {
                    filaClientes.acquire(); // Checa a fila. O barbeiro dorme se não há clientes na fila

                    mutex.acquire();
                    if (clientesEsperando > 0) {
                        clientesEsperando -= clientesEsperando; // Decrementa o número de clientes esperando
                    }
                    barbeiro.release();
                    filaClientes.release(); // Libera o próximo cliente da fila
                    mutex.release();

                    // Simula o corte de cabelo
                    EmAtendimento();

                } catch (InterruptedException e) {
                    System.err.print(e);
                }


                if (clientesEsperando == 0 && barbeiroEstaAtendendo == false) {
                    System.out.println("O barbeiro está dormindo");
                    barbeiroEstaDormindo = true;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e){
                        System.out.println("Erro");
                    }
                }

            }
        }

    }

    static class Cliente extends Thread{

        final int idCliente; // Inicializando um identifcador para os clientes

        // Construtor para a classe Cliente
        public Cliente(int idCliente) {
            this.idCliente = idCliente;
        }


        public void run() {

            try {
                mutex.acquire();

                if (clientesEsperando < MAX_CADEIRAS){
//                    if (Barbearia.barbeiroEstaDormindo == true){ // Significa que o barbeiro não está atendendo ninguém
//                        // Acorda o barbeiro e é atendido
//                    } else if (Barbearia.clientesEsperando == 0 && Barbearia.barbeiroEstaAtendendo == false){
//                        // É atendido
//                    } else { // Nos casos em que o barbeiro já está atendendo, entra na fila
//                        Barbearia.clientesEsperando++; // Incrementa o número de clientes esperando
////                        Barbearia.barbeiro.release();
////                        Barbearia.mutex.release();
////                        Barbearia.filaClientes.acquire();
//                    }

                    clientesEsperando++;

                    filaClientes.release();
                    mutex.release();
                    barbeiro.acquire();

                    if (barbeiroEstaDormindo == true){
                        barbeiroEstaDormindo = false;

                    }

                    Barbeiro.EmAtendimento();

                } else { // A barbearia está com todos os assentos ocupados
                    System.out.println("A barbearia está com todos os assentos ocupados. O cliente " + idCliente + " foi embora. Assentos ocupados: " + Barbearia.clientesEsperando + "/" + Barbearia.MAX_CADEIRAS);
                    mutex.release();
                }

            } catch (InterruptedException e) {
                System.out.println("Erro");
            }

        }
    }
}


public class Main {
    public static void main(String[] args) {
        int totalClientes = 100;

        Barbearia.Barbeiro barbeiro = new Barbearia.Barbeiro(); // Inicializando a thread do barbeiro
        barbeiro.start();

        for (int i = 1; i <= totalClientes; i++) {
            Barbearia.Cliente cliente = new Barbearia.Cliente(i);
            cliente.start();
        }

    }
}
