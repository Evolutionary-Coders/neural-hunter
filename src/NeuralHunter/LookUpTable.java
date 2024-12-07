package NeuralHunter;
import robocode.RobocodeFileOutputStream;

import java.io.*;

public class LookUpTable {
    private double[][] table;

    public LookUpTable() {
        States.init();
        this.initializeLUT();
    }

    public void initializeLUT() {
        this.table = new double[States.numStates][Actions.numActions];
        for (int i = 0; i < States.numStates; i++) {
            for (int j = 0; j < Actions.numActions; j++) {
                table[i][j] = 0;
            }
        }
    }

    public void saveQTable(File qTableFile) {
        PrintStream out = null;
        try {
            out = new PrintStream(new RobocodeFileOutputStream(qTableFile));
            for (int i = 0; i < States.numStates; i++) {
                for (int j = 0; j < table[i].length; j++) {
                    out.println(table[i][j]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
    } finally {
        if (out != null)
            out.close();
        }
    }

    public void loadQTable(File qTableFile) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(qTableFile));
            for (int i = 0; i < States.numStates; i++) {
                for (int j = 0; j < table[i].length; j++) {
                    table[i][j] = Double.parseDouble(reader.readLine());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    public void updateQ(int state, int action, double delta) {
        table[state][action] += delta;
    }
}
