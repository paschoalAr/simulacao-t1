# Simulador de filas - T1 (Simulação e Métodos Analíticos, turma 690)

Simulador de eventos discretos em Java, feito em etapas ao longo da disciplina:
primeiro uma fila simples (M4) e depois duas filas em tandem (M6). A ideia é que
a mesma estrutura sirva depois para uma rede de filas qualquer.

A implementação segue o algoritmo visto em aula: um escalonador de eventos
ordenado por tempo, o procedimento AcumulaTempo que contabiliza quanto tempo
cada fila ficou em cada estado, e os três tipos de evento CHEGADA, SAIDA e
PASSAGEM.

## Como rodar

Precisa de Java 11 ou mais novo. Não usa nenhuma biblioteca de fora.

```
cd src
javac *.java
java Main
```

Sem argumento roda todos os cenários. Dá para rodar cada um separado:

```
java Main exemplo
java Main m4
java Main m6
```

- `exemplo`: o exemplo resolvido à mão no material (cafeteria, G/G/1/4), usando a
  mesma lista fixa de aleatórios. Serve pra conferir se a lógica está certa.
- `m4`: os dois cenários da entrega do M4 (fila simples G/G/1/5 e G/G/2/5).
- `m6`: o cenário da entrega do M6 (duas filas em tandem).

Para o M4 e o M6 o simulador usa 100.000 números pseudoaleatórios gerados com
semente 1. Semente e quantidade estão no começo de `Main.java`.

## Como funciona

### Escalonador

O escalonador é uma lista de eventos mantida ordenada pelo tempo em que cada um
vai acontecer. Quando um evento novo é agendado ele é inserido na posição certa;
o laço principal sempre tira o primeiro da lista (o de menor tempo), avança o
relógio da simulação para esse tempo e chama o tratamento do evento.

Cada evento guarda: o tempo, o tipo (CHEGADA, SAIDA ou PASSAGEM), a fila de
origem e, no caso da PASSAGEM, a fila de destino.

### Tratamento dos eventos

Todo evento começa chamando AcumulaTempo, que soma para cada fila o intervalo
desde o último evento no estado (número de clientes) em que a fila está. Depois:

CHEGADA (cliente vindo de fora do sistema)
- se a fila tem lugar, o cliente entra; se com isso ele ficou dentro do número
  de servidores, já é agendada a saída dele (sorteia o tempo de atendimento);
- se a fila está cheia, conta uma perda;
- de qualquer jeito, agenda a próxima chegada de fora (sorteia o intervalo entre
  chegadas).

SAIDA (cliente termina o atendimento e vai embora do sistema)
- tira o cliente da fila;
- se ainda tem gente esperando (clientes >= servidores), agenda a saída do
  próximo.

PASSAGEM (cliente termina numa fila e vai para outra)
- na fila de origem faz a mesma coisa da SAIDA;
- na fila de destino faz a mesma coisa da chegada, só que sem agendar uma nova
  chegada de fora, porque esse cliente não veio de fora.

Na hora de agendar o fim de um atendimento o simulador consulta para onde a fila
manda os clientes. Se não manda para lugar nenhum, vira uma SAIDA; se manda para
outra fila, vira uma PASSAGEM com o destino já decidido. Quando a fila tem mais
de um destino possível, o destino é sorteado com um aleatório a mais, comparando
com as probabilidades acumuladas. Quando só tem um destino com probabilidade 1
(caso do tandem), não gasta aleatório.

A ordem em que os aleatórios são consumidos é a mesma do pseudocódigo do
material: primeiro o (eventual) sorteio de destino, depois o tempo de
atendimento, e por último o intervalo da próxima chegada.

### Fim da simulação

A simulação termina quando acaba a quantidade de aleatórios informada (100.000
nas entregas). Se um evento precisa de um aleatório e não tem mais, ele é
interrompido ali e o tempo global fica sendo o tempo desse último evento. É o
mesmo critério do simulador de referência da disciplina.

### Números pseudoaleatórios

Gerador congruente linear com a = 25214903917, c = 11 e M = 2^48 (os
parâmetros do `java.util.Random`), com a conta feita em BigInteger para não
estourar. Cada número é x / M, então fica entre 0 e 1. Também aceita uma lista
fixa de números, que é o que o exemplo do material usa.

## Arquivos

- `Main.java`: monta os cenários de cada entrega e chama o simulador.
- `Simulacao.java`: o escalonador, AcumulaTempo e o tratamento de CHEGADA,
  SAIDA e PASSAGEM. Também imprime o relatório no final.
- `Fila.java`: uma fila: nome, número de servidores, capacidade, intervalos de
  chegada e de atendimento, contador de clientes, perdas, tempo acumulado por
  estado e a lista de filas para onde ela manda os clientes (com as
  probabilidades).
- `Evento.java` e `TipoEvento.java`: um evento agendado.
- `Aleatorio.java`: o gerador congruente linear ou a lista fixa de números.

## Como montar um cenário (sintaxe de entrada)

Por enquanto os cenários são montados direto no código, em `Main.java`. Cada
fila é criada informando, nessa ordem: nome, número de servidores, capacidade,
intervalo entre chegadas (mínimo e máximo) e tempo de atendimento (mínimo e
máximo). Fila que não recebe cliente de fora usa o construtor sem os intervalos
de chegada. Capacidade -1 significa fila infinita.

O roteamento é definido com `liga(destino, probabilidade)`, uma chamada por
destino. Se a soma das probabilidades de uma fila for menor que 1, o restante é
cliente que sai do sistema. Fila sem nenhum `liga` manda todo mundo embora.

Exemplo, que é justamente o cenário do M6:

```java
Fila f1 = new Fila("Fila1", 2, 3, 1, 5, 4, 5);
Fila f2 = new Fila("Fila2", 1, 5, 1, 3);
f1.liga(f2, 1.0);

Simulacao s = new Simulacao(new Aleatorio(1, 100000));
s.adiciona(f1);
s.adiciona(f2);
s.primeiraChegada(f1, 2.5);
s.roda();
s.imprime();
```

Ou seja: Fila1 é G/G/2/3 com chegadas entre 1 e 5 e atendimento entre 4 e 5;
Fila2 é G/G/1/5 com atendimento entre 1 e 3 e sem chegada de fora; 100% do que
sai da Fila1 vai para a Fila2; semente 1 com 100.000 aleatórios; o primeiro
cliente chega no tempo 2,5 na Fila1.

Para uma rede maior é só criar mais filas, ligar cada uma nos seus destinos e
adicionar todas na simulação. A ideia para o T1 é trocar isso por um arquivo de
entrada, mas a estrutura interna já está pronta para isso.

## Saída

Para cada fila o programa imprime uma linha por estado (0 até a capacidade)
com o tempo acumulado naquele estado e a probabilidade, que é o tempo do estado
dividido pelo tempo global. Depois o número de perdas da fila. No fim, o tempo
global da simulação.

## Conferência

O exemplo do material (cafeteria, G/G/1/4, chegadas 1..2, atendimento 2..3,
primeiro cliente em 1,0, aleatórios 0,5 0,8 0,1 0,6 0,4 0,2) dá tempo global
5,3 e tempos por estado 1,0 / 2,2 / 2,1, igual ao resolvido à mão.

Os cenários do M4 e do M6 foram comparados com o `simulator.jar` disponibilizado
no módulo 3, com semente 1 e 100.000 aleatórios (no arquivo `.yml` do simulador,
`seeds: [1]` e `rndnumbersPerSeed: 100000`). Os tempos acumulados de cada
estado, as perdas e o tempo global deram iguais nos três cenários.

## Resultados

M4, cenário 1: G/G/1/5, chegadas 3..5, atendimento 4..5, primeiro cliente em 3,0

```
estado          tempo       prob
     0         3,4638    0,0016%
     1        15,7201    0,0074%
     2        20,3436    0,0096%
     3       769,7978    0,3636%
     4    101416,5322   47,9019%
     5    109491,4444   51,7159%
perdas: 5865
tempo global: 211717,3019
```

M4, cenário 2: G/G/2/5, chegadas 3..5, atendimento 4..5, primeiro cliente em 3,0

```
estado          tempo       prob
     0      4087,4672    2,0453%
     1    166539,1176   83,3329%
     2     29221,3219   14,6218%
     3         0,0000    0,0000%
     4         0,0000    0,0000%
     5         0,0000    0,0000%
perdas: 0
tempo global: 199847,9066
```

M6: Fila1 G/G/2/3 (chegadas 1..5, atendimento 4..5) -> Fila2 G/G/1/5
(atendimento 1..3), primeiro cliente em 2,5

```
Fila1 (G/G/2/3)
estado          tempo       prob
     0      1087,1284    1,0836%
     1     49096,2257   48,9368%
     2     43721,5218   43,5795%
     3      6421,0032    6,4001%
perdas: 393

Fila2 (G/G/1/5)
estado          tempo       prob
     0     33818,7727   33,7089%
     1     60134,8206   59,9395%
     2      6364,5787    6,3439%
     3         7,7071    0,0077%
     4         0,0000    0,0000%
     5         0,0000    0,0000%
perdas: 0

tempo global: 100325,8792
```

Dá para ver o efeito do tandem: a Fila1 perde 393 clientes porque tem
capacidade 3 e o atendimento (4..5) é mais lento que as chegadas (1..5), e a
Fila2 quase nunca passa de 2 clientes, porque só recebe o que a Fila1 consegue
atender e atende bem mais rápido (1..3).
