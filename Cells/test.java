//TEST.JAVA

package Cells;

import java.util.ArrayList;

public class test {
    public static void main(String[] args) {

        ArrayList<Double> numbers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double val = ( MathFunctions.evolve(100) + 1);
            numbers.add(val);
        }
        for (double num : numbers) {
            System.out.println(num);
        }

        System.out.println(
            MathFunctions.realMod(1.05,1)
        );
    }
}
