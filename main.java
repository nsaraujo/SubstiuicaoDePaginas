import java.util.Arrays;
import java.util.Random;

public class main {

    private static int[][] matrizSWAP;

    public static void main(String[] args) {
        Random gerador = new Random();
        int[] linhas = new int[6];
        Fila fila = new Fila(10);

        int[][] matrizRAM = new int[10][6];
        int[][] matrizSWAP = new int[100][6];

        // Preenchimento da matriz SWAP
        for (int l = 0; l < 100; l++) {
            matrizSWAP[l][0] = l;
            matrizSWAP[l][1] = l + 1;
            matrizSWAP[l][2] = gerador.nextInt(50) + 1;
            matrizSWAP[l][3] = 0;
            matrizSWAP[l][4] = 0;
            matrizSWAP[l][5] = gerador.nextInt(9900) + 100;
        }

        // Para cada linha da matriz RAM, é sorteado um número.
        // Copia os dados para a linha da matriz RAM a partir da matriz SWAP,
        // uso como índice para a linha o número que foi sorteado.
        for (int l = 0; l < 10; l++) {
            int n = gerador.nextInt(100);
            matrizRAM[l] = matrizSWAP[n].clone();
        }

        //Tabela inicial.
        imprimirMatriz("MATRIZ SWAP - Inicial", matrizSWAP);
        imprimirMatriz("MATRIZ RAM - Inicial", matrizRAM);

        for (int h = 0; h < 1000; h++) {
            // Sorteia um número de 1 a 100 referente à instrução.
            int numeroInstrucao = gerador.nextInt(100) + 1;

            boolean encontrado = false;

            for (int l = 0; l < 10; l++) {
                // Verifica se a instrução está carregada na memória RAM.
                if (matrizRAM[l][1] == numeroInstrucao) {
                    //O bit de acesso R recebe o valor 1.
                    matrizRAM[l][3] = 1;
                    
                    if ((gerador.nextInt(100) + 1) < 30) {
                        // 2.1) O campo Dado (D) será atualizado da seguinte maneira: D = D + 1;
                        matrizRAM[l][2]++;
                        // 2.2) O campo Modificado será atualizado: M = 1;
                        matrizRAM[l][4] = 1;
                    }
                    encontrado = true;
                }
            }

            if (!encontrado) {
                // Escolher algoritmo de substituição de página aqui
                substituirPaginaFIFO(matrizRAM, matrizSWAP, fila, linhas, gerador);
                substituirPaginaNRU(matrizRAM, matrizSWAP, fila, linhas, gerador);
                substituirPaginaFIFO_SC(matrizRAM, matrizSWAP, fila, linhas, gerador);
                substituirPaginaRelogio(matrizRAM, fila, linhas, gerador);
                substituirPaginaWSClock(matrizRAM, fila, linhas, gerador);
            }

            // A cada 10 instruções, zere o Bit R para todas as páginas na memória RAM.
            if ((h + 1) % 10 == 0) {
                zerarBitR(matrizRAM);
            }
        }

        //Tabela final.
        imprimirMatriz("MATRIZ SWAP - Final", matrizSWAP);
        imprimirMatriz("MATRIZ RAM - Final", matrizRAM);
    }

    private static void substituirPaginaFIFO(int[][] matrizRAM, int[][] matrizSWAP, Fila fila, int[] linhas, Random gerador) {
        // FIFO: Remover a página mais antiga da fila e adicionar a nova página.
        int[] paginaRemovida = fila.remover();
        for (int a = 0; a < 100; a++) {
            if (matrizSWAP[a][1] == paginaRemovida[1]) {
                matrizSWAP[a][0] = linhas[0];
                matrizSWAP[a][1] = linhas[1];
                matrizSWAP[a][2] = linhas[2];
                matrizSWAP[a][3] = linhas[3];
                matrizSWAP[a][4] = linhas[4];
                matrizSWAP[a][5] = linhas[5];
            }
        }
        fila.adicionar(matrizSWAP[paginaRemovida[0]].clone());
    }

    private static void substituirPaginaNRU(int[][] matrizRAM, int[][] matrizSWAP, Fila fila, int[] linhas, Random gerador) {
        // NRU - Algoritmo de Substituição de Página
        // Divide as páginas em quatro classes: (R=0, M=0), (R=0, M=1), (R=1, M=0), (R=1, M=1)
        // Substitui uma página aleatória da classe com menor prioridade.

        int classe = 0;
        while (true) {
            for (int i = 0; i < 10; i++) {
                if (matrizRAM[i][3] == classe / 2 && matrizRAM[i][4] == classe % 2) {
                    // Substitui a página encontrada
                    int paginaRemovida = fila.remover()[1];
                    int indexMatrizSwap = Arrays.stream(matrizSWAP).filter(row -> row[1] == paginaRemovida).findFirst().map(row -> row[0]).orElse(-1);

                    if (indexMatrizSwap != -1) {
                        // Salva a página removida na matriz SWAP
                        matrizSWAP[indexMatrizSwap][4] = 0; // Bit M = 0
                        fila.adicionar(matrizSWAP[indexMatrizSwap].clone());

                        // Carrega a nova página na matriz RAM
                        matrizRAM[i] = Arrays.copyOf(linhas, 6);
                        matrizRAM[i][3] = 1; // Bit R = 1
                        matrizRAM[i][4] = 0; // Bit M = 0

                        return;
                    }
                }
            }
            classe = (classe + 1) % 4;
        }
    }

    private static void substituirPaginaFIFO_SC(int[][] matrizRAM, int[][] matrizSWAP, Fila fila, int[] linhas, Random gerador) {
        // FIFO-SC - Algoritmo de Substituição de Página com Segunda Chance
        // Implementação similar ao FIFO, mas com um bit adicional (bit SC) para segunda chance.
        // A página mais antiga (com bit SC = 0) tem mais chance de ser substituída.

        while (true) {
            int[] paginaRemovida = fila.remover();
            int indexMatrizSwap = Arrays.stream(matrizSWAP).filter(row -> row[1] == paginaRemovida[1]).findFirst().map(row -> row[0]).orElse(-1);

            if (indexMatrizSwap != -1) {
                // Salva a página removida na matriz SWAP
                matrizSWAP[indexMatrizSwap][4] = 0; // Bit M = 0
                fila.adicionar(matrizSWAP[indexMatrizSwap].clone());

                // Carrega a nova página na matriz RAM
                int indexMatrizRAM = Arrays.stream(matrizRAM).filter(row -> row[1] == linhas[1]).findFirst().map(row -> row[0]).orElse(-1);
                matrizRAM[indexMatrizRAM] = Arrays.copyOf(linhas, 6);
                matrizRAM[indexMatrizRAM][3] = 1; // Bit R = 1
                matrizRAM[indexMatrizRAM][4] = 0; // Bit M = 0

                return;
            }
        }
    }

    private static void substituirPaginaRelogio(int[][] matrizRAM, Fila fila, int[] linhas, Random gerador) {
        // RELÓGIO - Algoritmo de Substituição de Página baseado em Relógio
        // Utiliza um ponteiro para circular pelas páginas, marcando o bit R como 0.
        // Substitui a primeira página com bit R = 0 encontrada.

        int ponteiro = 0;

        while (true) {
            if (matrizRAM[ponteiro][3] == 0) {
                // Substitui a página encontrada
                int paginaRemovida = fila.remover()[1];
                int indexMatrizSwap = Arrays.stream(matrizSWAP).filter(row -> row[1] == paginaRemovida).findFirst().map(row -> row[0]).orElse(-1);

                if (indexMatrizSwap != -1) {
                    // Salva a página removida na matriz SWAP
                    matrizSWAP[indexMatrizSwap][4] = 0; // Bit M = 0
                    fila.adicionar(matrizSWAP[indexMatrizSwap].clone());

                    // Carrega a nova página na matriz RAM
                    matrizRAM[ponteiro] = Arrays.copyOf(linhas, 6);
                    matrizRAM[ponteiro][3] = 1; // Bit R = 1
                    matrizRAM[ponteiro][4] = 0; // Bit M = 0

                    return;
                }
            }

            // Marca o bit R como 0 e move o ponteiro
            matrizRAM[ponteiro][3] = 0;
            ponteiro = (ponteiro + 1) % 10;
        }
    }

    private static void substituirPaginaWSClock(int[][] matrizRAM, Fila fila, int[] linhas, Random gerador) {
        // WS-CLOCK - Algoritmo de Substituição de Página baseado em WS-CLOCK
        // Similar ao RELÓGIO, mas também leva em consideração o tempo de envelhecimento (T).
        // Substitui a primeira página com bit R = 0 e (EP > T) encontrada.

        int ponteiro = 0;

        while (true) {
            if (matrizRAM[ponteiro][3] == 0 && matrizRAM[ponteiro][5] > linhas[5]) {
                // Substitui a página encontrada
                int paginaRemovida = fila.remover()[1];
                int indexMatrizSwap = Arrays.stream(matrizSWAP).filter(row -> row[1] == paginaRemovida).findFirst().map(row -> row[0]).orElse(-1);

                if (indexMatrizSwap != -1) {
                    // Salva a página removida na matriz SWAP
                    matrizSWAP[indexMatrizSwap][4] = 0; // Bit M = 0
                    fila.adicionar(matrizSWAP[indexMatrizSwap].clone());

                    // Carrega a nova página na matriz RAM
                    matrizRAM[ponteiro] = Arrays.copyOf(linhas, 6);
                    matrizRAM[ponteiro][3] = 1; // Bit R = 1
                    matrizRAM[ponteiro][4] = 0; // Bit M = 0

                    return;
                }
            }

            // Marca o bit R como 0 e move o ponteiro
            matrizRAM[ponteiro][3] = 0;
            ponteiro = (ponteiro + 1) % 10;
        }
    }

    // Método para zerar o Bit R para todas as páginas na memória RAM.
    private static void zerarBitR(int[][] matrizRAM) {
        // Zera o Bit R para todas as páginas na memória RAM.
        for (int l = 0; l < 10; l++) {
            matrizRAM[l][3] = 0;
        }

        // Zera o Bit R para todas as páginas na memória SWAP.
        for (int l = 0; l < 100; l++) {
            matrizSWAP[l][3] = 0;
        }
    }

    // Exemplo de impressão de matriz
    private static void imprimirMatriz(String nomeMatriz, int[][] matriz) {
        System.out.println();
        System.out.println(nomeMatriz);
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.printf("%15s", "N° de Página (N)");                                                     
        System.out.printf("%15s", "Instrução (I)");
        System.out.printf("%11s", "Dado (D)");
        System.out.printf("%22s", "Bit de Acesso R");
        System.out.printf("%22s", "Bit de Modificação M ");
        System.out.printf("%22s%n", " Tempo de Envelhecimento (T)");
        System.out.println("-------------------------------------------------------------------------------------------");

        for (int l = 0; l < matriz.length; l++) {
            for (int c = 0; c < matriz[0].length; c++) {
                System.out.printf("%15s", matriz[l][c]);
            }
            System.out.println();
        }
    }
}
