package Cells;
import java.lang.Math;

public class MathFunction {
    public static double evolve(double evolveRate) {

        double rand = (Math.random()*2)-1; // [-1,1]
        double multiplier = 1;
        if (rand < 0) {
            multiplier = -1;
            rand = -rand;
        }

        double a = Math.acos((2*rand) - 1) / Math.PI; // [0,1]
        double b = Math.pow(1-a, evolveRate);
        return b*multiplier;
    }
}
