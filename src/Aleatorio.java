import java.math.BigInteger;

public class Aleatorio {
    public static class Acabou extends RuntimeException {
    }

    private BigInteger a = BigInteger.valueOf(25214903917L);
    private BigInteger c = BigInteger.valueOf(11);
    private BigInteger m = BigInteger.valueOf(2).pow(48);
    private BigInteger x;
    private long limite;
    private long usados;
    private double[] lista;

    public Aleatorio(long semente, long limite) {
        this.x = BigInteger.valueOf(semente);
        this.limite = limite;
    }

    public Aleatorio(double[] lista) {
        this.lista = lista;
        this.limite = lista.length;
    }

    public boolean temMais() {
        return usados < limite;
    }

    public double proximo() {
        if (!temMais()) {
            throw new Acabou();
        }
        usados++;
        if (lista != null) {
            return lista[(int) usados - 1];
        }
        x = x.multiply(a).add(c).mod(m);
        return x.doubleValue() / m.doubleValue();
    }
}
