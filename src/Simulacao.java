import java.util.ArrayList;

public class Simulacao {
    private ArrayList<Fila> filas = new ArrayList<Fila>();
    private ArrayList<Evento> escalonador = new ArrayList<Evento>();
    private Aleatorio rnd;
    private double tempo;
    private double tempoAnterior;

    public Simulacao(Aleatorio rnd) {
        this.rnd = rnd;
    }

    public void adiciona(Fila f) {
        filas.add(f);
    }

    public void primeiraChegada(Fila f, double t) {
        agenda(new Evento(t, TipoEvento.CHEGADA, f, null));
    }

    public double getTempo() {
        return tempo;
    }

    private void agenda(Evento e) {
        int i = 0;
        while (i < escalonador.size() && escalonador.get(i).getTempo() <= e.getTempo()) {
            i++;
        }
        escalonador.add(i, e);
    }

    private void acumulaTempo() {
        for (Fila f : filas) {
            f.acumula(tempo - tempoAnterior);
        }
        tempoAnterior = tempo;
    }

    private double sorteia(double min, double max) {
        return min + (max - min) * rnd.proximo();
    }

    private Fila proximaFila(Fila f) {
        ArrayList<Fila> destinos = f.getDestinos();
        ArrayList<Double> probabilidades = f.getProbabilidades();
        if (destinos.size() == 0) {
            return null;
        }
        if (destinos.size() == 1 && probabilidades.get(0) >= 1.0) {
            return destinos.get(0);
        }
        double r = rnd.proximo();
        double soma = 0;
        for (int i = 0; i < destinos.size(); i++) {
            soma += probabilidades.get(i);
            if (r <= soma) {
                return destinos.get(i);
            }
        }
        return null;
    }

    private void agendaSaida(Fila f) {
        Fila destino = proximaFila(f);
        double t = tempo + sorteia(f.getMinAtendimento(), f.getMaxAtendimento());
        if (destino == null) {
            agenda(new Evento(t, TipoEvento.SAIDA, f, null));
        } else {
            agenda(new Evento(t, TipoEvento.PASSAGEM, f, destino));
        }
    }

    private void entra(Fila f) {
        if (f.cheia()) {
            f.perdeCliente();
            return;
        }
        f.entraCliente();
        if (f.getClientes() <= f.getServidores()) {
            agendaSaida(f);
        }
    }

    private void sai(Fila f) {
        f.saiCliente();
        if (f.getClientes() >= f.getServidores()) {
            agendaSaida(f);
        }
    }

    private void chegada(Fila f) {
        acumulaTempo();
        entra(f);
        double t = tempo + sorteia(f.getMinChegada(), f.getMaxChegada());
        agenda(new Evento(t, TipoEvento.CHEGADA, f, null));
    }

    private void saida(Fila f) {
        acumulaTempo();
        sai(f);
    }

    private void passagem(Fila origem, Fila destino) {
        acumulaTempo();
        sai(origem);
        entra(destino);
    }

    public void roda() {
        try {
            while (rnd.temMais() && !escalonador.isEmpty()) {
                Evento e = escalonador.remove(0);
                tempo = e.getTempo();
                if (e.getTipo() == TipoEvento.CHEGADA) {
                    chegada(e.getOrigem());
                } else if (e.getTipo() == TipoEvento.SAIDA) {
                    saida(e.getOrigem());
                } else {
                    passagem(e.getOrigem(), e.getDestino());
                }
            }
        } catch (Aleatorio.Acabou fim) {
        }
    }

    public void imprime() {
        for (Fila f : filas) {
            System.out.println(f.getNome() + " (G/G/" + f.getServidores() + "/" + f.getCapacidade() + ")");
            System.out.println("  estado          tempo       prob");
            for (int i = 0; i <= f.getUltimoEstado(); i++) {
                System.out.printf("  %4d %14.4f %9.4f%%\n", i, f.getTempo(i), 100 * f.getTempo(i) / tempo);
            }
            System.out.println("  perdas: " + f.getPerdas());
        }
        System.out.printf("tempo global: %.4f\n", tempo);
        System.out.println();
    }
}
