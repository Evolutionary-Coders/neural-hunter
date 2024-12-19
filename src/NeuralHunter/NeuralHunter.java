package NeuralHunter;

import robocode.*;

import java.awt.*;
import java.io.*;
import java.util.*;

public class NeuralHunter extends AdvancedRobot {

    private double enemyDistance;
    private double enemyBearing;
    private boolean enemyFound = false;
    private double currentReward = 0.0;

    private NeuralNetwork neuralNetwork = new NeuralNetwork();
    private LookUpTable lookupTable = new LookUpTable();

    private double[] oldStates = new double[5];
    private int oldAction;
    private int currentAction;

    @Override
    public void run() {
        mainColor();
        prepareRobot();
        mainBattleLoop();
    }

    Color disparo = new Color(255, 213, 0);

    private void mainColor() {
        Color rosio = new Color(255, 255, 255, 255);
        setBodyColor(rosio);
        setGunColor(rosio);
        setRadarColor(rosio);
        setBulletColor(disparo);
        setScanColor(rosio);
    }

    private void penaltyColor() {
        Color rosio = new Color(255, 0, 0);
        setBodyColor(rosio);
        setGunColor(rosio);
        setRadarColor(rosio);
        setBulletColor(disparo);
        setScanColor(rosio);
    }

    private void rewardColor() {
        Color rosio = new Color(132, 255, 0);
        setBodyColor(rosio);
        setGunColor(rosio);
        setRadarColor(rosio);
        setBulletColor(disparo);
        setScanColor(rosio);
    }


    private void prepareRobot() {
        setAdjustGunForRobotTurn(false);
        setAdjustRadarForGunTurn(false);
        turnRadarRightRadians(2 * Math.PI);

        oldStates = getCurrentNNStates();
        oldAction = selectBestAction(oldStates);
        currentAction = oldAction;
    }

    private void mainBattleLoop() {
        while (true) {
            resetReward();
            executeSelectedAction(currentAction);
            execute();

            waitForActionCompletion();
            turnRadarRightRadians(2 * Math.PI);

            updateLearningCycle();
        }
    }

    private void waitForActionCompletion() {
        while (getDistanceRemaining() != 0 || getTurnRemaining() != 0) {
            execute();
        }
    }

   private void updateLearningCycle() {
        double[] newStates = getCurrentNNStates();
        int newAction = selectBestAction(newStates);

        updateNeuralNetworkLearning(
                oldStates, oldAction,
                newStates, newAction,
                currentReward
        );

        currentAction = newAction;
        System.arraycopy(newStates, 0, oldStates, 0, newStates.length);
        oldAction = newAction;
    }

    private void updateNeuralNetworkLearning(
            double[] oldState, int oldAction,
            double[] newState, int newAction,
            double reward) {
        double newQValue = neuralNetwork.calculateError(
                mergeStatesAndAction(newState, newAction)
        );
        double oldQValue = neuralNetwork.calculateError(
                mergeStatesAndAction(oldState, oldAction)
        );

        double deltaQValue = RobotConfig.LEARNING_RATE * (
                reward + RobotConfig.DISCOUNT_RATE * newQValue - oldQValue
        );

        neuralNetwork.trainWeights(oldQValue + deltaQValue);
    }

    private int selectBestAction(double[] state) {
        double maxQ = Double.MIN_VALUE;
        int bestAction = 0;

        for (int i = 0; i < Actions.numActions; i++) {
            double[] stateAction = mergeStatesAndAction(state, i);
            double qValue = neuralNetwork.calculateError(stateAction);

            if (qValue > maxQ) {
                maxQ = qValue;
                bestAction = i;
            }
        }
        return bestAction;
    }

    private double[] mergeStatesAndAction(double[] states, int action) {
        double[] stateAction = Arrays.copyOf(states, states.length + 1);
        stateAction[states.length] = action;
        return stateAction;
    }

    private void executeSelectedAction(int action) {
        switch (action) {
            case Actions.AHEAD_LEFT:
                setAhead(Actions.DISTANCE);
                setTurnLeft(Actions.TURN_DEGREE);
                break;
            case Actions.AHEAD_RIGHT:
                setAhead(Actions.DISTANCE);
                setTurnRight(Actions.TURN_DEGREE);
                break;
            case Actions.BACK_LEFT:
                setBack(Actions.DISTANCE);
                setTurnRight(Actions.TURN_DEGREE);
                break;
            case Actions.BACK_RIGHT:
                setBack(Actions.DISTANCE);
                setTurnLeft(Actions.TURN_DEGREE);
                break;
            case Actions.FIRE:
                scanAndFire();
                break;
        }
    }

    private void scanAndFire() {
        while (!enemyFound) {
            setTurnRadarLeft(360);
            execute();
        }

        turnGunLeft(getGunHeading() - getHeading() - enemyBearing);
        fire(RobotConfig.FIRE_POWER);
    }

    private double[] getCurrentNNStates() {
        return new double[]{
                States.reduceHeadingDimension(getHeadingRadians()),
                States.reduceEnemyDistanceDimension(enemyDistance),
                States.reduceEnemyAngleDimension(enemyBearing),
        };
    }

    private void resetReward() {
        currentReward = 0.0;
    }

    private void addReward(double amount) {
        currentReward += amount;
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        mainColor();
        enemyDistance = e.getDistance();
        enemyBearing = e.getBearing();
        enemyFound = true;
    }

    @Override
    public void onBulletHit(BulletHitEvent e) {
        rewardColor();
        addReward(RobotConfig.REWARD_BULLET_HIT);
    }

    @Override
    public void onBulletMissed(BulletMissedEvent e) {
        penaltyColor();
        addReward(RobotConfig.PENALTY_BULLET_MISSED);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        penaltyColor();
        addReward(RobotConfig.PENALTY_HIT_BY_BULLET * e.getBullet().getPower());
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        penaltyColor();
        addReward(RobotConfig.PENALTY_WALL_HIT);
    }

    @Override
    public void onWin(WinEvent event) {
        saveBattleResult(true);
        rewardColor();
        addReward(RobotConfig.REWARD_WIN);
    }

    @Override
    public void onDeath(DeathEvent event) {
        saveBattleResult(false);
        addReward(RobotConfig.PENALTY_DEATH);
        penaltyColor();
    }

    private void saveBattleResult(boolean win) {
        try (PrintStream writer = new PrintStream(
                new RobocodeFileOutputStream(getDataFile("battle_history.dat").getAbsolutePath(), true))) {
            writer.println(win ? 1 : 0);
        } catch (IOException e) {
            System.err.println("Erro ao salvar histórico de batalha: " + e.getMessage());
        }
    }

    public void loadTrainingData() {
        try {
            lookupTable.loadQTable(getDataFile("LUT.dat"));
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados de treinamento: " + e.getMessage());
        }
    }



}
