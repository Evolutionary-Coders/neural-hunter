package NeuralHunter;

import robocode.*;

public class NeuralHunter extends AdvancedRobot{
    public void run(){
        while(true){
            setAhead(100);
            setTurnRight(360);
            execute();
        }
    }
}
