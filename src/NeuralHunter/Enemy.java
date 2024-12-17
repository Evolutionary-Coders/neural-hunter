package NeuralHunter;

public class Enemy {

    private double angle;
    private double distance;

    public Enemy(double angle, double distance) {
        this.angle = angle;
        this.distance = distance;
    }

    public double getAngle() {
        return angle;
    }

    public double getDistance() {
        return distance;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
