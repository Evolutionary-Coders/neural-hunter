package NeuralHunter;

public class LookUpTable {
    private double[][] table;

    public void updateQ(int state, int action, double delta) {
        table[state][action] += delta;
    }
}
