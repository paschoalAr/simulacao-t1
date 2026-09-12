import java.util.ArrayList;

public class Fila {
    private String nome;
    private int servidores;
    private int capacidade;
    private double minChegada, maxChegada;
    private double minAtendimento, maxAtendimento;

    private int clientes;
    private int perdas;
    private ArrayList<Double> tempos = new ArrayList<Double>();

    private ArrayList<Fila> destinos = new ArrayList<Fila>();
    private ArrayList<Double> probabilidades = new ArrayList<Double>();

    public Fila(String nome, int servidores, int capacidade, double minChegada, double maxChegada,
            double minAtendimento, double maxAtendimento) {
        this.nome = nome;
        this.servidores = servidores;
        this.capacidade = capacidade;
        this.minChegada = minChegada;
        this.maxChegada = maxChegada;
        this.minAtendimento = minAtendimento;
        this.maxAtendimento = maxAtendimento;
    }

    public Fila(String nome, int servidores, int capacidade, double minAtendimento, double maxAtendimento) {
        this(nome, servidores, capacidade, 0, 0, minAtendimento, maxAtendimento);
    }

    public String getNome() {
        return nome;
    }

    public int getServidores() {
        return servidores;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public double getMinChegada() {
        return minChegada;
    }

    public double getMaxChegada() {
        return maxChegada;
    }

    public double getMinAtendimento() {
        return minAtendimento;
    }

    public double getMaxAtendimento() {
        return maxAtendimento;
    }

    public int getClientes() {
        return clientes;
    }

    public int getPerdas() {
        return perdas;
    }

    public ArrayList<Fila> getDestinos() {
        return destinos;
    }

    public ArrayList<Double> getProbabilidades() {
        return probabilidades;
    }

    public void liga(Fila destino, double probabilidade) {
        destinos.add(destino);
        probabilidades.add(probabilidade);
    }

    public boolean cheia() {
        return capacidade >= 0 && clientes >= capacidade;
    }

    public void entraCliente() {
        clientes++;
    }

    public void saiCliente() {
        clientes--;
    }

    public void perdeCliente() {
        perdas++;
    }

    public void acumula(double delta) {
        while (tempos.size() <= clientes) {
            tempos.add(0.0);
        }
        tempos.set(clientes, tempos.get(clientes) + delta);
    }

    public double getTempo(int estado) {
        if (estado >= tempos.size()) {
            return 0;
        }
        return tempos.get(estado);
    }

    public int getUltimoEstado() {
        if (capacidade >= 0) {
            return capacidade;
        }
        return tempos.size() - 1;
    }
}
