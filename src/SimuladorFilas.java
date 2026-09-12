import java.math.BigInteger;
import java.util.*;

public class SimuladorFilas {

    interface FonteAleatoria {
        boolean hasNext();
        double next();
    }

    static class SemMaisAleatorios extends RuntimeException {}

    // mesmos parametros do java.util.Random, usados no simulator.jar da disciplina
    static class GeradorCongruencial implements FonteAleatoria {
        static final BigInteger A = BigInteger.valueOf(25214903917L);
        static final BigInteger C = BigInteger.valueOf(11L);
        static final BigInteger M = BigInteger.ONE.shiftLeft(48);

        private BigInteger x;
        private final long limite;
        private long count = 0;

        GeradorCongruencial(long semente, long limite) {
            this.x = BigInteger.valueOf(semente);
            this.limite = limite;
        }

        public boolean hasNext() { return count < limite; }

        public double next() {
            if (!hasNext()) throw new SemMaisAleatorios();
            x = A.multiply(x).add(C).mod(M);
            count++;
            return x.doubleValue() / M.doubleValue();
        }
    }

    static class ListaFixa implements FonteAleatoria {
        private final double[] valores;
        private int i = 0;
        ListaFixa(double... valores) { this.valores = valores; }
        public boolean hasNext() { return i < valores.length; }
        public double next() {
            if (!hasNext()) throw new SemMaisAleatorios();
            return valores[i++];
        }
    }

    static class Destino {
        final Fila alvo;
        final double probabilidade;
        Destino(Fila alvo, double probabilidade) { this.alvo = alvo; this.probabilidade = probabilidade; }
    }

    static class Fila {
        final String nome;
        final int servidores;
        final int capacidade; // -1 = infinita
        final double chegadaMin, chegadaMax; // -1 se nao houver chegada externa
        final double servMin, servMax;

        int populacao = 0;
        int perdidos = 0;
        final Map<Integer, Double> times = new TreeMap<>();
        final List<Destino> destinos = new ArrayList<>();

        Fila(String nome, int servidores, int capacidade,
             double chegadaMin, double chegadaMax, double servMin, double servMax) {
            this.nome = nome;
            this.servidores = servidores;
            this.capacidade = capacidade;
            this.chegadaMin = chegadaMin;
            this.chegadaMax = chegadaMax;
            this.servMin = servMin;
            this.servMax = servMax;
        }

        void addDestino(Fila alvo, double probabilidade) {
            destinos.add(new Destino(alvo, probabilidade));
            destinos.sort(Comparator.comparingDouble(d -> d.probabilidade));
        }

        boolean temCapacidade() { return capacidade < 0 || populacao < capacidade; }
    }

    enum TipoEvento { CHEGADA, SAIDA }

    static class Evento implements Comparable<Evento> {
        double tempo;
        TipoEvento tipo;
        Fila fila;
        Fila destinoNaSaida;
        public int compareTo(Evento o) { return Double.compare(tempo, o.tempo); }
    }

    static class Ambiente {
        final List<Fila> filas = new ArrayList<>();
        final List<Evento> escalonador = new ArrayList<>();
        final FonteAleatoria rnd;

        double TG = 0.0;
        double ultimoEvento = 0.0;

        Ambiente(FonteAleatoria rnd) { this.rnd = rnd; }

        Fila addFila(Fila f) { filas.add(f); return f; }

        void agendaChegadaInicial(Fila fila, double tempoAbsoluto) {
            Evento e = new Evento();
            e.tempo = tempoAbsoluto;
            e.tipo = TipoEvento.CHEGADA;
            e.fila = fila;
            escalonador.add(e);
            escalonador.sort(null);
        }

        private void agendar(double delay, TipoEvento tipo, Fila fila, Fila destinoNaSaida) {
            Evento e = new Evento();
            e.tempo = TG + delay;
            e.tipo = tipo;
            e.fila = fila;
            e.destinoNaSaida = destinoNaSaida;
            escalonador.add(e);
            escalonador.sort(null);
        }

        private void acumulaTempo() {
            double delta = TG - ultimoEvento;
            for (Fila f : filas) {
                f.times.merge(f.populacao, delta, Double::sum);
            }
            ultimoEvento = TG;
        }

        private Fila getDestino(Fila fila) {
            if (fila.destinos.isEmpty()) return null;
            if (fila.destinos.size() == 1 && fila.destinos.get(0).probabilidade >= 1.0) {
                return fila.destinos.get(0).alvo;
            }
            double sorteio = rnd.next();
            double acumulado = 0.0;
            for (Destino d : fila.destinos) {
                acumulado += d.probabilidade;
                if (sorteio <= acumulado) return d.alvo;
            }
            return null;
        }

        private void chegada(Fila fila, boolean externa) {
            acumulaTempo();
            if (fila.temCapacidade()) {
                fila.populacao++;
                if (fila.populacao <= fila.servidores) {
                    Fila destino = getDestino(fila);
                    double delay = rnd.next() * (fila.servMax - fila.servMin) + fila.servMin;
                    agendar(delay, TipoEvento.SAIDA, fila, destino);
                }
            } else {
                fila.perdidos++;
            }
            if (externa) {
                double delay = rnd.next() * (fila.chegadaMax - fila.chegadaMin) + fila.chegadaMin;
                agendar(delay, TipoEvento.CHEGADA, fila, null);
            }
        }

        private void saida(Fila fila, Fila destinoDoEvento) {
            acumulaTempo();
            fila.populacao--;
            if (fila.populacao >= fila.servidores) {
                Fila destino = getDestino(fila);
                double delay = rnd.next() * (fila.servMax - fila.servMin) + fila.servMin;
                agendar(delay, TipoEvento.SAIDA, fila, destino);
            }
            if (destinoDoEvento != null) {
                chegada(destinoDoEvento, false);
            }
        }

        void run() {
            try {
                while (!escalonador.isEmpty() && rnd.hasNext()) {
                    Evento ev = escalonador.remove(0);
                    TG = ev.tempo;
                    if (ev.tipo == TipoEvento.CHEGADA) {
                        chegada(ev.fila, true);
                    } else {
                        saida(ev.fila, ev.destinoNaSaida);
                    }
                }
            } catch (SemMaisAleatorios fim) {
                // acabaram os aleatorios: a simulacao para no estado em que estava
            }
        }

        void relatorio() {
            for (Fila f : filas) {
                System.out.println("=========================================================");
                String notacao = "G/G/" + f.servidores + (f.capacidade < 0 ? "" : "/" + f.capacidade);
                System.out.println("Fila " + f.nome + "  (" + notacao + ")");
                if (f.chegadaMin >= 0) {
                    System.out.printf("  chegada: %.1f..%.1f%n", f.chegadaMin, f.chegadaMax);
                }
                System.out.printf("  atendimento: %.1f..%.1f%n", f.servMin, f.servMax);
                System.out.println("---------------------------------------------------------");
                System.out.println("  estado        tempo acumulado      probabilidade");
                int max = f.capacidade < 0 ? Collections.max(f.times.keySet()) : f.capacidade;
                for (int i = 0; i <= max; i++) {
                    double t = f.times.getOrDefault(i, 0.0);
                    System.out.printf("  %6d %20.4f %18.4f%%%n", i, t, t / TG * 100.0);
                }
                System.out.println("  perdas: " + f.perdidos);
            }
            System.out.println("=========================================================");
            System.out.printf("Tempo Global (TG) da simulacao: %.4f%n", TG);
        }
    }

    static void rodarExemploValidacao() {
        System.out.println(">>> Validacao: exemplo da cafeteria (G/G/1/4, aleatorios fixos) <<<");
        FonteAleatoria rnd = new ListaFixa(0.5, 0.8, 0.1, 0.6, 0.4, 0.2);
        Ambiente amb = new Ambiente(rnd);
        Fila q = amb.addFila(new Fila("Q1", 1, 4, 1.0, 2.0, 2.0, 3.0));
        amb.agendaChegadaInicial(q, 1.0);
        amb.run();
        amb.relatorio();
        System.out.println("Esperado (guia): TG=5.3 ; times=[1.0,2.2,2.1,0,0] ; P(vazia)=18.87%");
        System.out.println();
    }

    static void rodarM4(long semente) {
        System.out.println(">>> M4 | Entrega Fila simples <<<");

        System.out.println("--- Cenario 1: G/G/1/5, chegadas 3..5, atendimento 4..5 ---");
        Ambiente amb1 = new Ambiente(new GeradorCongruencial(semente, 100_000));
        Fila q1 = amb1.addFila(new Fila("Q1", 1, 5, 3.0, 5.0, 4.0, 5.0));
        amb1.agendaChegadaInicial(q1, 3.0);
        amb1.run();
        amb1.relatorio();
        System.out.println();

        System.out.println("--- Cenario 2: G/G/2/5, chegadas 3..5, atendimento 4..5 ---");
        Ambiente amb2 = new Ambiente(new GeradorCongruencial(semente, 100_000));
        Fila q2 = amb2.addFila(new Fila("Q1", 2, 5, 3.0, 5.0, 4.0, 5.0));
        amb2.agendaChegadaInicial(q2, 3.0);
        amb2.run();
        amb2.relatorio();
        System.out.println();
    }

    static void rodarM6(long semente) {
        System.out.println(">>> M6 | Entrega Filas em tandem <<<");
        System.out.println("--- Fila 1: G/G/2/3, chegadas 1..5, atendimento 4..5  ->  Fila 2: G/G/1/5, atendimento 1..3 ---");

        Ambiente amb = new Ambiente(new GeradorCongruencial(semente, 100_000));
        Fila f1 = amb.addFila(new Fila("Fila1", 2, 3, 1.0, 5.0, 4.0, 5.0));
        Fila f2 = amb.addFila(new Fila("Fila2", 1, 5, -1, -1, 1.0, 3.0));
        f1.addDestino(f2, 1.0);
        amb.agendaChegadaInicial(f1, 2.5);
        amb.run();
        amb.relatorio();
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
