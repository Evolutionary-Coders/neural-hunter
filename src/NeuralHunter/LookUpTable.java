package NeuralHunter;
import robocode.RobocodeFileOutputStream;

import java.io.*;

public class LookUpTable {

    private static final double ALPHA = 0.2; // Learning rate
    private static final double GAMMA = 0.6; // Discount factor

    private double[][] table;

    public LookUpTable() {
        States.init();
        this.initializeLUT();
    }

    // Inicialização da LUT
    public void initializeLUT() {
        this.table = new double[States.numStates][Actions.numActions];
        for (int i = 0; i < States.numStates; i++) {
            for (int j = 0; j < Actions.numActions; j++) {
                table[i][j] = 0; // Valores iniciais da LUT
            }
        }
    }

    // Atualização da Q-table
    public void updateQ(int state, int action, double delta) {
        table[state][action] += delta;
    }

    public void updateQOnPolicy(int previousState, int state, int previousAction, int action, double reward) {
        double delta = ALPHA * (reward + GAMMA * table[state][action] - table[previousState][previousAction]);
        updateQ(previousState, previousAction, delta);
    }

    public void updateQOffPolicy(int previousState, int state, int previousAction, int action, double reward) {
        double delta = ALPHA * (reward + GAMMA * findMaxQvalueAction(table[state]) - table[previousState][previousAction]);
        updateQ(previousState, previousAction, delta);
    }

    // Seleção de ação
    public int selectAction(int state, boolean random) {
        double[] qValues = table[state];
        if (random || hasNoQValues(qValues)) {
            return (int) (Math.random() * Actions.numActions);
        } else {
            return findMaxQValueActionIndex(qValues);
        }
    }

    // Consulta de valores da Q-table
    private int indexForState(double[] state) {
        return States.states[States.getHeading(state[0])][States.getTargetDistance(state[1])][States.getTargetHeading(state[2])];
    }

    public double getMaximumQValueForState(double[] state) {
        return findMaxQvalueAction(this.table[indexForState(state)]);
    }

    // Salvar/carregar Q-table
    public void saveQTable(File qTableFile) {
        try (PrintStream out = new PrintStream(new RobocodeFileOutputStream(qTableFile))) {
            for (double[] row : table) {
                for (double value : row) {
                    out.println(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadQTable(File qTableFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(qTableFile))) {
            for (int i = 0; i < States.numStates; i++) {
                for (int j = 0; j < table[i].length; j++) {
                    table[i][j] = Double.parseDouble(reader.readLine());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(String fileName) throws IOException {
        loadQTable(new File(fileName));
    }

    // Métodos auxiliares
    private double findMaxQvalueAction(double[] qValues) {
        double maxQ = Double.NEGATIVE_INFINITY;
        for (double qValue : qValues) {
            maxQ = Math.max(maxQ, qValue);
        }
        return maxQ;
    }

    private int findMaxQValueActionIndex(double[] qValues) {
        double maxQ = Double.NEGATIVE_INFINITY;
        int maxQIndex = 0;
        for (int i = 0; i < qValues.length; i++) {
            if (qValues[i] > maxQ) {
                maxQ = qValues[i];
                maxQIndex = i;
            }
        }
        return maxQIndex;
    }

    private boolean hasNoQValues(double[] row) {
        for (double value : row) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }
}
