package NeuralHunter;

import java.util.Random;

public class NeuralNetwork {

    // Configuração de rede
    private static final int DEFAULT_HIDDEN_LAYERS = 30;
    private static final int DEFAULT_INPUT_NEURONS = 4;
    private static final int DEFAULT_OUTPUT_NEURONS = 1;
    private static final int DEFAULT_TOTAL_LAYERS = 3;
    private static final int DEFAULT_LUT_ACTIONS = 5;
    private static final int DEFAULT_LUT_STATES = 64;

    private static final double DEFAULT_LEARNING_RATE = 0.2;
    private static final double DEFAULT_MOMENTUM = 0.6;

    // Estruturas de rede neural
    private double[][][] weights;
    private double[][][] deltaWeights;
    private double[][] neuronCell;
    private double[][] layerError;

    // Parâmetros da rede
    private final int hiddenLayers;
    private final int numInputNeurons;
    private final int numOutputNeurons;
    private final int numLayers;

    // Parâmetros da Tabela de Lookup
    public final int numLUTActions;
    public final int numLUTStates;
    public double[][] tableInput;

    // Parâmetros de treinamento
    private double error;
    private double weightSum;
    private final double alpha;
    private final double momentum;

    public NeuralNetwork() {
        this.hiddenLayers = DEFAULT_HIDDEN_LAYERS;
        this.numInputNeurons = DEFAULT_INPUT_NEURONS;
        this.numOutputNeurons = DEFAULT_OUTPUT_NEURONS;
        this.numLayers = DEFAULT_TOTAL_LAYERS;
        this.numLUTActions = DEFAULT_LUT_ACTIONS;
        this.numLUTStates = DEFAULT_LUT_STATES;
        this.alpha = DEFAULT_LEARNING_RATE;
        this.momentum = DEFAULT_MOMENTUM;

        initialize();
    }

    public void initialize() {
        initMemory();
        initWeights();
    }

    /**
     * Aloca memória para as estruturas de dados da rede neural.
     */
    private void initMemory() {
        initializeNetworkLayers();
        initializeLookupTable();
    }

    /**
     * Inicializa as camadas da rede e suas estruturas associadas.
     */
    private void initializeNetworkLayers() {
        weights = new double[numLayers - 1][][];
        weights[0] = new double[numInputNeurons + 1][hiddenLayers];
        weights[1] = new double[hiddenLayers + 1][numOutputNeurons];

        deltaWeights = new double[numLayers - 1][][];
        deltaWeights[0] = new double[numInputNeurons + 1][hiddenLayers];
        deltaWeights[1] = new double[hiddenLayers + 1][numOutputNeurons];

        neuronCell = new double[numLayers][];
        neuronCell[0] = new double[numInputNeurons + 1];
        neuronCell[1] = new double[hiddenLayers + 1];
        neuronCell[2] = new double[numOutputNeurons];

        // Neurônios de bias
        neuronCell[0][numInputNeurons] = sigmoid(1);
        neuronCell[1][hiddenLayers] = sigmoid(1);

        layerError = new double[numLayers][];
        layerError[0] = new double[numInputNeurons + 1];
        layerError[1] = new double[hiddenLayers + 1];
        layerError[2] = new double[numOutputNeurons];
    }

    /**
     * Inicializa os valores de entrada da LUT.
     */
    private void initializeLookupTable() {
        tableInput = new double[numLUTStates][numLUTActions];

        for (int i = 0; i < numLUTStates; i++) {
            tableInput[i][0] = normalizeState(i, 1);
            tableInput[i][1] = normalizeState(i / 4, 1);
            tableInput[i][2] = normalizeState(i / 16, 1);
        }
    }

    /**
     * Normaliza o valor do estado para o intervalo [-1, 1].
     */
    private double normalizeState(double value, int divisor) {
        return ((value % 4) / divisor) * 2 - 1;
    }

    /**
     * Inicializa os pesos da rede com valores aleatórios.
     */
    private void initWeights() {
        Random random = new Random();
        initializeLayerWeights(weights[0], numInputNeurons + 1, hiddenLayers, random);
        initializeLayerWeights(weights[1], hiddenLayers + 1, numOutputNeurons, random);
    }

    /**
     * Calcula o erro e realiza a propagação para frente.
     *
     * @param actionStates Estados de ação de entrada
     * @return Valor Q calculado
     */
    public double calculateError(double[] actionStates) {
        // Preparar camada de entrada
        System.arraycopy(actionStates, 0, neuronCell[0], 0, numInputNeurons);

        // Propagação para frente
        feedForward(neuronCell[0], weights[0], neuronCell[1], hiddenLayers);
        feedForward(neuronCell[1], weights[1], neuronCell[2], numOutputNeurons);

        return neuronCell[2][0];
    }

    /**
     * Treina os pesos da rede usando backpropagation.
     *
     * @param targetValue Valor Q alvo para treinamento
     */
    public void trainWeights(double targetValue) {
        calculateOutputLayerError(targetValue);
        calculateHiddenLayerError();
        updateWeights();
    }

    /**
     * Realiza a propagação para frente através de uma camada.
     *
     * @param input Neurônios da camada de entrada
     * @param layerWeights Pesos da camada
     * @param output Neurônios da camada de saída
     * @param numNeurons Número de neurônios na camada
     */
    private void feedForward(double[] input, double[][] layerWeights, double[] output, int numNeurons) {
        for (int i = 0; i < numNeurons; i++) {
            weightSum = calculateWeightedSum(input, layerWeights, i);
            output[i] = sigmoid(weightSum);
        }
    }

    /**
     * Calcula a soma ponderada para um neurônio.
     *
     * @param input Neurônios da camada de entrada
     * @param layerWeights Pesos da camada
     * @param neuronIndex Índice do neurônio de destino
     * @return Soma ponderada
     */
    private double calculateWeightedSum(double[] input, double[][] layerWeights, int neuronIndex) {
        double sum = 0;
        for (int j = 0; j < input.length; j++) {
            sum += input[j] * layerWeights[j][neuronIndex];
        }
        return sum;
    }

    private void calculateHiddenLayerError() {
        for (int i = 0; i < hiddenLayers; i++) {
            double hiddenOutput = neuronCell[1][i];
            layerError[1][i] = weights[1][i][0] * layerError[2][0] * hiddenOutput * (1 - hiddenOutput);
        }
    }

    /**
     * Calcula o erro para neurônios da camada de saída.
     * @param targetValue Valor Q alvo
     */
    private void calculateOutputLayerError(double targetValue) {
        layerError[2][0] = neuronCell[2][0] * (1 - neuronCell[2][0]) * (targetValue - neuronCell[2][0]);
    }

    private void updateWeights() {
        updateLayerWeights(weights[1], deltaWeights[1], neuronCell[1], layerError[2], hiddenLayers + 1, numOutputNeurons);
    }

    /**
     * Inicializa os pesos da camada com valores aleatórios.
     *
     * @param layerWeights Matriz de pesos da camada
     * @param rows Número de linhas
     * @param cols Número de colunas
     * @param random Gerador de números aleatórios
     */
    private void initializeLayerWeights(double[][] layerWeights, int rows, int cols, Random random) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                layerWeights[i][j] = random.nextDouble() - 0.5;
            }
        }
    }

    /**
     * Atualiza os pesos para uma camada específica.
     *
     * @param layerWeights Matriz de pesos da camada
     * @param deltaWeights Mudanças de peso anteriores
     * @param input Neurônios da camada de entrada
     * @param layerErrors Valores de erro da camada
     * @param rows Número de linhas
     * @param cols Número de colunas
     */
    private void updateLayerWeights(double[][] layerWeights, double[][] deltaWeights,
                                    double[] input, double[] layerErrors,
                                    int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double weightChange = alpha * layerErrors[j] * input[i] + momentum * deltaWeights[i][j];
                deltaWeights[i][j] = weightChange;
                layerWeights[i][j] += weightChange;
            }
        }
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}