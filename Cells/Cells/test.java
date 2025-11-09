//TEST.JAVA

package Cells;

import java.util.ArrayList;

public class test {
    public static void main(String[] args) {

        ArrayList<Double> numbers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double val = MathFunctions.evolve(10.0);
            numbers.add(val);
        }
        for (double num : numbers) {
            System.out.println(num);
        }
    }
}
