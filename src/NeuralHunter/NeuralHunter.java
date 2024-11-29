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
