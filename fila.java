import java.util.NoSuchElementException;

class Fila {
    int frente, tras, tamanho;
    int capacidade;
    int array[][];
    int[] vazio = { -1 };

    public Fila(int capacidade) {
        this.capacidade = capacidade;
        frente = this.tamanho = 0;
        tras = capacidade - 1;
        array = new int[this.capacidade][];
    }

    boolean taCheio() {
        return (tamanho == capacidade);
    }

    boolean taVazio() {
        return (tamanho == 0);
    }

    void adicionar(int[] item) {
        if (taCheio())
            return;
        tras = (tras + 1) % capacidade;
        array[tras] = item;
        tamanho++;
        System.out.println(item[0] + " entrou na fila.");
    }

    int[] remover() {
        if (taVazio())
            throw new NoSuchElementException("A fila está vazia.");

        int[] item = array[frente];
        frente = (frente + 1) % capacidade;
        tamanho--;
        return item;
    }

    int[] frente() {
        if (taVazio())
            return vazio;

        return array[frente];
    }

    int[] tras() {
        if (taVazio())
            return vazio;

        return array[tras];
    }
}
