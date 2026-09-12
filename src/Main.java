public class Main {

    private static long semente = 1;
    private static int quantidade = 100000;

    private static void exemplo() {
        System.out.println("exemplo do material: G/G/1/4, chegadas 1..2, atendimento 2..3");
        double[] lista = {0.5, 0.8, 0.1, 0.6, 0.4, 0.2};
        Simulacao s = new Simulacao(new Aleatorio(lista));
        Fila q = new Fila("Q1", 1, 4, 1, 2, 2, 3);
        s.adiciona(q);
        s.primeiraChegada(q, 1.0);
        s.roda();
        s.imprime();
    }

    private static void m4() {
        System.out.println("M4 cenario 1: G/G/1/5, chegadas 3..5, atendimento 4..5");
        Simulacao s = new Simulacao(new Aleatorio(semente, quantidade));
        Fila q = new Fila("Q1", 1, 5, 3, 5, 4, 5);
        s.adiciona(q);
        s.primeiraChegada(q, 3.0);
        s.roda();
        s.imprime();

        System.out.println("M4 cenario 2: G/G/2/5, chegadas 3..5, atendimento 4..5");
        s = new Simulacao(new Aleatorio(semente, quantidade));
        q = new Fila("Q1", 2, 5, 3, 5, 4, 5);
        s.adiciona(q);
        s.primeiraChegada(q, 3.0);
        s.roda();
        s.imprime();
    }

    private static void m6() {
        System.out.println("M6: Fila1 G/G/2/3 (chegadas 1..5, atendimento 4..5) -> Fila2 G/G/1/5 (atendimento 1..3)");
        Simulacao s = new Simulacao(new Aleatorio(semente, quantidade));
        Fila f1 = new Fila("Fila1", 2, 3, 1, 5, 4, 5);
        Fila f2 = new Fila("Fila2", 1, 5, 1, 3);
        f1.liga(f2, 1.0);
        s.adiciona(f1);
        s.adiciona(f2);
        s.primeiraChegada(f1, 2.5);
        s.roda();
        s.imprime();
    }

    public static void main(String[] args) {
        String qual = args.length > 0 ? args[0] : "todos";
        if (qual.equals("exemplo") || qual.equals("todos")) {
            exemplo();
        }
        if (qual.equals("m4") || qual.equals("todos")) {
            m4();
        }
        if (qual.equals("m6") || qual.equals("todos")) {
            m6();
        }
    }
}
