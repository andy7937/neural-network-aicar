package simulator;

import java.util.List;

import neuralNetwork.NeuralNetwork;

public class Car {
        public double x, y; // Car position
        public double velocity = 0; // Car speed
        public double acceleration = 0; // Car acceleration
        public double angle; // Car angle in degrees
        public List<Point> sensorCollisionPoint;
        public int reward;
        public NeuralNetwork neuralNetwork;

        public Car(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

    }