package NeuralHunter;

public class RewardCalculator {
    private boolean useImmediateReward = true;
    private double currentReward = 0.0;

    void resetReward() {
        currentReward = 0.0;
    }

    void addReward(double amount) {
        if (useImmediateReward) {
            currentReward += amount;
        }
    }

    double getReward() {
        return currentReward;
    }
}
