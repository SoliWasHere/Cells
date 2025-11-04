package Cells;

class MathFunctions {
    public static double wrapCoordinate(double coord, double max) {
        if (coord < 0) {
            return coord + max * (Math.floor(-coord / max) + 1);
        } else if (coord >= max) {
            return coord - max * Math.floor(coord / max);
        } else {
            return coord;
        }
    }

    public static double randomEvolve(double evolveFactor) {
        double b = Math.pow( Math.random() , evolveFactor);
        double a = (0.5) * (1.0 - Math.cos(Math.PI * b));
        return a;
    }

    public static double[] normalizeVector(double x, double y) {
        double length = Math.sqrt(x * x + y * y);
        if (length == 0) {
            return new double[]{0, 0};
        }
        return new double[]{x / length, y / length};
    }
}