public class Main {

    static void rodarExemploValidacao() {
        System.out.println("Exemplo de validacao (cafeteria, G/G/1/4)");
        SimuladorFilas.FonteAleatoria rnd = new SimuladorFilas.ListaFixa(0.5, 0.8, 0.1, 0.6, 0.4, 0.2);
        SimuladorFilas sim = new SimuladorFilas(rnd);
        SimuladorFilas.Fila q = sim.addFila(new SimuladorFilas.Fila("Q1", 1, 4, 1.0, 2.0, 2.0, 3.0));
        sim.agendaChegadaInicial(q, 1.0);
        sim.run();
        sim.relatorio();
        System.out.println("esperado: tempo global 5.3, estados [1.0, 2.2, 2.1, 0, 0]");
        System.out.println();
    }

    static void rodarM4(long semente) {
        System.out.println("M4 cenario 1: G/G/1/5, chegadas 3..5, atendimento 4..5");
        SimuladorFilas sim1 = new SimuladorFilas(new SimuladorFilas.GeradorCongruencial(semente, 100_000));
        SimuladorFilas.Fila q1 = sim1.addFila(new SimuladorFilas.Fila("Q1", 1, 5, 3.0, 5.0, 4.0, 5.0));
        sim1.agendaChegadaInicial(q1, 3.0);
        sim1.run();
        sim1.relatorio();
        System.out.println();

        System.out.println("M4 cenario 2: G/G/2/5, chegadas 3..5, atendimento 4..5");
        SimuladorFilas sim2 = new SimuladorFilas(new SimuladorFilas.GeradorCongruencial(semente, 100_000));
        SimuladorFilas.Fila q2 = sim2.addFila(new SimuladorFilas.Fila("Q1", 2, 5, 3.0, 5.0, 4.0, 5.0));
        sim2.agendaChegadaInicial(q2, 3.0);
        sim2.run();
        sim2.relatorio();
        System.out.println();
    }

    static void rodarM6(long semente) {
        System.out.println("M6: Fila1 G/G/2/3 -> Fila2 G/G/1/5");

        SimuladorFilas sim = new SimuladorFilas(new SimuladorFilas.GeradorCongruencial(semente, 100_000));
        SimuladorFilas.Fila f1 = sim.addFila(new SimuladorFilas.Fila("Fila1", 2, 3, 1.0, 5.0, 4.0, 5.0));
        SimuladorFilas.Fila f2 = sim.addFila(new SimuladorFilas.Fila("Fila2", 1, 5, -1, -1, 1.0, 3.0));
        f1.addDestino(f2, 1.0);
        sim.agendaChegadaInicial(f1, 2.5);
        sim.run();
        sim.relatorio();
        System.out.println();
    }

    public static void main(String[] args) {
        long semente = 1L;
        String modo = args.length > 0 ? args[0] : "todos";

        if (modo.equals("exemplo") || modo.equals("todos")) rodarExemploValidacao();
        if (modo.equals("m4") || modo.equals("todos")) rodarM4(semente);
        if (modo.equals("m6") || modo.equals("todos")) rodarM6(semente);
    }
}
