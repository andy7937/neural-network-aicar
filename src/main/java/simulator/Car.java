package simulator;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import neuralNetwork.NeuralNetwork;

public class Car {
        public double x, y; // Car position
        public double velocity = 0; // Car speed
        public double acceleration = 0; // Car acceleration
        public double angle; // Car angle in degrees
        public List<Point> sensorCollisionPoint;
        public List<Double> sensorDistance;
        public int reward;
        public NeuralNetwork neuralNetwork;
        public boolean isDead;
        public Color colour;
        Random rand = new Random(System.currentTimeMillis());

        public Car(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.isDead = false;
            this.colour = randomColour();
        }

        private Color randomColour() {
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            return new Color(r, g, b);
        }

    }