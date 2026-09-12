import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SimuladorFilas {

    public interface FonteAleatoria {
        boolean hasNext();
        double next();
    }

    public static class SemMaisAleatorios extends RuntimeException {}

    // mesmos parametros do java.util.Random, usados no simulator.jar da disciplina
    public static class GeradorCongruencial implements FonteAleatoria {
        static final BigInteger A = BigInteger.valueOf(25214903917L);
        static final BigInteger C = BigInteger.valueOf(11L);
        static final BigInteger M = BigInteger.ONE.shiftLeft(48);

        private BigInteger x;
        private final long limite;
        private long count = 0;

        public GeradorCongruencial(long semente, long limite) {
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

    public static class ListaFixa implements FonteAleatoria {
        private final double[] valores;
        private int i = 0;
        public ListaFixa(double... valores) { this.valores = valores; }
        public boolean hasNext() { return i < valores.length; }
        public double next() {
            if (!hasNext()) throw new SemMaisAleatorios();
            return valores[i++];
        }
    }

    public static class Destino {
        final Fila alvo;
        final double probabilidade;
        public Destino(Fila alvo, double probabilidade) { this.alvo = alvo; this.probabilidade = probabilidade; }
    }

    public static class Fila {
        public final String nome;
        public final int servidores;
        public final int capacidade; // -1 = infinita
        public final double chegadaMin, chegadaMax; // -1 se nao houver chegada externa
        public final double servMin, servMax;

        int populacao = 0;
        int perdidos = 0;
        final Map<Integer, Double> times = new TreeMap<>();
        final List<Destino> destinos = new ArrayList<>();

        public Fila(String nome, int servidores, int capacidade,
                    double chegadaMin, double chegadaMax, double servMin, double servMax) {
            this.nome = nome;
            this.servidores = servidores;
            this.capacidade = capacidade;
            this.chegadaMin = chegadaMin;
            this.chegadaMax = chegadaMax;
            this.servMin = servMin;
            this.servMax = servMax;
        }

        public void addDestino(Fila alvo, double probabilidade) {
            destinos.add(new Destino(alvo, probabilidade));
            destinos.sort(Comparator.comparingDouble(d -> d.probabilidade));
        }

        boolean temCapacidade() { return capacidade < 0 || populacao < capacidade; }

        public double getTempo(int estado) { return times.getOrDefault(estado, 0.0); }
        public int getPerdidos() { return perdidos; }
    }

    private enum TipoEvento { CHEGADA, SAIDA }

    private static class Evento implements Comparable<Evento> {
        double tempo;
        TipoEvento tipo;
        Fila fila;
        Fila destinoNaSaida;
        public int compareTo(Evento o) { return Double.compare(tempo, o.tempo); }
    }

    private final List<Fila> filas = new ArrayList<>();
    private final List<Evento> escalonador = new ArrayList<>();
    private final FonteAleatoria rnd;

    private double TG = 0.0;
    private double ultimoEvento = 0.0;

    public SimuladorFilas(FonteAleatoria rnd) { this.rnd = rnd; }

    public Fila addFila(Fila f) { filas.add(f); return f; }

    public void agendaChegadaInicial(Fila fila, double tempoAbsoluto) {
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

    public void run() {
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

    public double getTempoGlobal() { return TG; }

    public void relatorio() {
        for (Fila f : filas) {
            System.out.println(f.nome + ":");
            int max = f.capacidade < 0 ? Collections.max(f.times.keySet()) : f.capacidade;
            for (int i = 0; i <= max; i++) {
                double t = f.getTempo(i);
                System.out.printf("  estado %d: %.4f (%.4f%%)%n", i, t, t / TG * 100.0);
            }
            System.out.println("  perdas: " + f.getPerdidos());
        }
        System.out.printf("tempo global: %.4f%n", TG);
    }
}
