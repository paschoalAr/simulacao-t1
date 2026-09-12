# Simulador de Filas — Simulação e Métodos Analíticos (T1 - 690)

Simulador de eventos discretos para redes de filas (fila simples e filas em *tandem*),
implementado seguindo o algoritmo apresentado no material da disciplina (escalonador de
eventos ordenado por tempo, `AcumulaTempo`, `CHEGADA`, `SAIDA`/`PASSAGEM`).

Disciplina: Simulação e Métodos Analíticos — Prof. Afonso Sales, PUCRS (2026/2).

## Como compilar e rodar

Requer Java 11+.

```bash
cd src
javac SimuladorFilas.java

java SimuladorFilas exemplo   # roda o exemplo resolvido a mao do material (validacao da logica)
java SimuladorFilas m4        # roda os 2 cenarios da entrega M4 (fila simples)
java SimuladorFilas m6        # roda o cenario da entrega M6 (filas em tandem)
java SimuladorFilas todos     # roda os tres (padrao, sem argumentos)
```

## Como definir um cenário (sintaxe de entrada)

Não há arquivo de configuração externo: os cenários são definidos diretamente em código,
através da API do simulador. Isso torna explícito, em Java, cada parâmetro pedido pelo
enunciado (chegada, atendimento, servidores, capacidade e roteamento):

```java
// Fila: nome, nº servidores, capacidade (-1 = infinita), chegadaMin, chegadaMax (-1 se não houver
// chegada externa), atendimentoMin, atendimentoMax
Fila f1 = new Fila("Fila1", 2, 3, 1.0, 5.0, 4.0, 5.0);
Fila f2 = new Fila("Fila2", 1, 5, -1, -1, 1.0, 3.0); // sem chegada externa

// Roteamento (rede/tandem): fila de origem -> fila de destino, com probabilidade
f1.addDestino(f2, 1.0); // 100% do que sai da Fila1 vai para a Fila2

Ambiente amb = new Ambiente(new GeradorCongruencial(semente, 100_000)); // 100.000 aleatórios
amb.addFila(f1);
amb.addFila(f2);
amb.agendaChegadaInicial(f1, 2.5); // 1º cliente chega no tempo 2,5
amb.run();
amb.relatorio();
```

Para adicionar um novo cenário, basta criar um novo método (seguindo `rodarM4`/`rodarM6`
em `SimuladorFilas.java`) com as filas, o roteamento e a chegada inicial desejados.

## Geração de números pseudoaleatórios

Método Congruente Linear com os mesmos parâmetros do `java.util.Random`
(`a = 25.214.903.917`, `c = 11`, `M = 2^48`) — os mesmos usados pelo simulador de
referência da disciplina (`simulator.jar`). A simulação encerra imediatamente ao
esgotar os números pseudoaleatórios disponíveis (100.000), conforme especificado.

## Validação

1. **Exemplo resolvido à mão** (seção 4 do guia de método da disciplina, cenário da
   cafeteria, `G/G/1/4` com lista fixa de aleatórios): o simulador reproduz exatamente
   `TG = 5,3`, `times = [1.0, 2.2, 2.1, 0, 0]`, `P(vazia) = 18,87%`.
2. **Cruzamento com o `simulator.jar`** (gabarito oficial da disciplina): rodando os
   mesmos parâmetros e a mesma semente (`seed = 1`, 100.000 aleatórios) no `simulator.jar`
   fornecido no Módulo 3, os resultados batem número a número (tempos acumulados por
   estado, perdas e tempo global) para os três cenários abaixo.

## Resultados

### M4 | Fila simples

**Cenário 1 — G/G/1/5, chegadas 3..5, atendimento 4..5** (1º cliente em t=3,0)

| Estado | Tempo acumulado | Probabilidade |
|---|---|---|
| 0 | 3,4638 | 0,0016% |
| 1 | 15,7201 | 0,0074% |
| 2 | 20,3436 | 0,0096% |
| 3 | 769,7978 | 0,3636% |
| 4 | 101416,5322 | 47,9019% |
| 5 | 109491,4444 | 51,7159% |

Perdas: 5865 · Tempo Global: 211717,3019

**Cenário 2 — G/G/2/5, chegadas 3..5, atendimento 4..5** (1º cliente em t=3,0)

| Estado | Tempo acumulado | Probabilidade |
|---|---|---|
| 0 | 4087,4672 | 2,0453% |
| 1 | 166539,1176 | 83,3329% |
| 2 | 29221,3219 | 14,6218% |
| 3 | 0,0000 | 0,0000% |
| 4 | 0,0000 | 0,0000% |
| 5 | 0,0000 | 0,0000% |

Perdas: 0 · Tempo Global: 199847,9066

### M6 | Filas em tandem

**Fila 1 — G/G/2/3, chegadas 1..5, atendimento 4..5** (1º cliente em t=2,5)

| Estado | Tempo acumulado | Probabilidade |
|---|---|---|
| 0 | 1087,1284 | 1,0836% |
| 1 | 49096,2257 | 48,9368% |
| 2 | 43721,5218 | 43,5795% |
| 3 | 6421,0032 | 6,4001% |

Perdas: 393

**Fila 2 — G/G/1/5, atendimento 1..3** (recebe 100% da Fila 1)

| Estado | Tempo acumulado | Probabilidade |
|---|---|---|
| 0 | 33818,7727 | 33,7089% |
| 1 | 60134,8206 | 59,9395% |
| 2 | 6364,5787 | 6,3439% |
| 3 | 7,7071 | 0,0077% |
| 4 | 0,0000 | 0,0000% |
| 5 | 0,0000 | 0,0000% |

Perdas: 0 · Tempo Global: 100325,8792

## Estrutura

```
src/SimuladorFilas.java   -- simulador completo (gerador de aleatorios, fila, evento,
                              ambiente/escalonador e os cenarios de entrega)
```
