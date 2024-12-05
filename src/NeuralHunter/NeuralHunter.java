package NeuralHunter;
import java.awt.*;
import robocode.*;

public class NeuralHunter extends AdvancedRobot{
    public void run(){
        // Set colors
        Color rosio = new Color(122, 13, 50);

        //Arco Iru
        Color vermelho = new Color(255, 0, 0);
        Color laranja = new Color(255, 165, 0);
        Color amarelo = new Color(255, 255, 0);
        Color verde = new Color(0, 255, 0);
        Color AzulMarin = new Color(0, 0, 255);
        Color Roxo = new Color(128, 0, 128);
        Color Rosa = new Color(255, 0, 255);


        setBodyColor(rosio);
        setGunColor(rosio);
        setRadarColor(rosio);
        setBulletColor(rosio);
        setScanColor(rosio);

        while(true){
            setBodyColor(rosio);
            setGunColor(rosio);
            setRadarColor(rosio);
            setBulletColor(rosio);
            setScanColor(rosio);
        }



        while(true){
            setAhead(100);
            setTurnRight(360);
            execute();
        }
    }

/* eventos para o sistema de recompensas e aprendizado
    public void onScannedRobot(ScannedRobotEvent e) {
    }

    public void onBulletHit(BulletHitEvent e) {
    }

    public void onBulletMissed(BulletMissedEvent e) {
    }

    public void onHitByBullet(HitByBulletEvent e) {
    }

    public void onHitWall(HitWallEvent e) {
    }

    public void onWin(WinEvent event) {
    }

    public void onDeath(DeathEvent event) {
    }
*/

    // calcula a ação com maior valor Q para o estado atual
    private int calcMaxQvalueAction(){
        return 0;
    }




}
