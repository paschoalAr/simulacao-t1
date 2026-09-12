public class Evento {
    private double tempo;
    private TipoEvento tipo;
    private Fila origem;
    private Fila destino;

    public Evento(double tempo, TipoEvento tipo, Fila origem, Fila destino) {
        this.tempo = tempo;
        this.tipo = tipo;
        this.origem = origem;
        this.destino = destino;
    }

    public double getTempo() {
        return tempo;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public Fila getOrigem() {
        return origem;
    }

    public Fila getDestino() {
        return destino;
    }
}
