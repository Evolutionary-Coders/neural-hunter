package NeuralHunter;

public class States {
    public static final int X_SIZE = 800;
    public static final int Y_SIZE = 600;

    public static final int NUM_HEADING = 4;
    public static final int NUM_TARGET_DISTANCE = 4;
    public static final int NUM_TARGET_HEADING = 4;
    public static final int NUM_X_POSITION = X_SIZE / 100;
    public static final int NUM_Y_POSITION = Y_SIZE / 100;

    public static final int[][][][][] states = new int[NUM_HEADING][NUM_TARGET_DISTANCE][NUM_TARGET_HEADING][NUM_X_POSITION][NUM_Y_POSITION];
    public static int numStates = 0;

    public static double[] angleStates;
    public static double[] heightStates;
    public static double[] widthStates;

    public static void init() {
        int statesIndex = 0;
        // cria um índice para cada estado
        for (int i = 0; i < NUM_HEADING; i++) {
            for (int j = 0; j < NUM_TARGET_DISTANCE; j++) {
                for (int k = 0; k < NUM_TARGET_HEADING; k++) {
                    for (int l = 0; l < NUM_X_POSITION; l++) {
                        for (int m = 0; m < NUM_Y_POSITION; m++) {
                            states[i][j][k][l][m] = statesIndex;
                            statesIndex++;
                        }
                    }
                }
            }
        }
        numStates = statesIndex;
        angleStates = initStateArray(NUM_HEADING);
        heightStates = initStateArray(NUM_Y_POSITION);
        widthStates = initStateArray(NUM_X_POSITION);
    }

    private static double[] initStateArray(int size) {
     double[] stateArray = new double[size];
        for (int i = 0; i < size; i++) {
            // normaliza os valores de índice entre -1 e 1
            stateArray[i] = ((Math.floor((double) i) % size) / (size - 1)) * 2 - 1;
        }
        return stateArray;
    }

    public static int getXPosition(double x){
        double sizeUnit = (double) X_SIZE / NUM_X_POSITION;
        return (int) Math.floor(x / sizeUnit);
    }

    public static int getYPosition(double y){
        double sizeUnit = (double) Y_SIZE / NUM_Y_POSITION;
        return (int) Math.floor(y / sizeUnit);
    }

    public static int getHeading(double heading){
        double unitAngle = 2 * Math.PI / NUM_HEADING;
        return (int) Math.floor(heading / unitAngle);
    }

    public static int getTargetDistance(double distance){
        int distanceBin = (int) (distance / 100);
        return Math.min(distanceBin, NUM_TARGET_DISTANCE - 1);
    }

    public static int getTargetHeading(double angle){
        double unitAngle = 2 * Math.PI / NUM_TARGET_HEADING;
        return (int) Math.floor((angle + Math.PI) / unitAngle);
    }

    public static double reduceXPositionDimension(double x){
        return widthStates[getXPosition(x)];
    }

    public static double reduceYPositionDimension(double y){
        return heightStates[getYPosition(y)];
    }

    public static double reduceHeadingDimension(double heading){
        if (heading == 0) return angleStates[0];
        return angleStates[getHeading(heading)];
    }

    public static double reduceEnemyDistanceDimension(double distance){
        if (distance < 100) return angleStates[0];
        if (distance < 200) return angleStates[1];
        if (distance < 300) return angleStates[2];
        return angleStates[3];
    }

    public static double reduceEnemyAngleDimension(double angle){
        double angleOffset = angle + Math.PI;
        if (angleOffset < Math.PI / 2) return angleStates[0];
        if (angleOffset < Math.PI) return angleStates[1];
        if (angleOffset < 3 * Math.PI / 2) return angleStates[2];
        return angleStates[3];
    }
}
