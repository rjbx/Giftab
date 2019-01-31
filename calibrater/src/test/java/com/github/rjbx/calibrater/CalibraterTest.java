package com.github.rjbx.calibrater;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Test methods for the {@link Calibrater} class.
 * Equals assertions are more precise as delta parameter approaches zero.
 */
public class CalibraterTest {
    
    private static int PRECISION = Calibrater.STANDARD_PRECISION;
    private static double ERROR = Math.pow(10, -PRECISION);

    /**
     * Asserts whether incrementing and decrementing a {@code double} array element resets all element values.
     */
    @Test public final void testShiftDoubleArrayResetSingle() {

        double[] percentages = { .25d, .25d, .25d, .25d };
        for (double magnitude = 0.01d; magnitude <= .1d; magnitude += 0.01d) {
            for (int index = 0; index < 4; index++) {
                Calibrater.shiftRatings(percentages, index, magnitude, PRECISION);
                Calibrater.shiftRatings(percentages, index, magnitude * -1d, PRECISION);
            }
            assertEquals(.25d, percentages[0], ERROR);
            assertEquals(.25d, percentages[1], ERROR);
            assertEquals(.25d, percentages[2], ERROR);
            assertEquals(.25d, percentages[3], ERROR);
        }
    }

    /**
     * Asserts whether incrementing and decrementing through the range of possible values for
     * two adjacent {@code double} arrays assigns the expected value to each array element.
     */
    @Test public final void testShiftDoubleArrayRepeatRange() {

        int multiplier = 0;
        double[] percentages = { .25d, .25d, .25d, .25d };
        for (double magnitude = 0.01d; magnitude <= .1d; magnitude += 0.01d) {
            for (int index = 0; index < percentages.length; index++) {
                while (percentages[index] <= .99d) {
                    Calibrater.shiftRatings(percentages, index, magnitude, PRECISION);
                    multiplier++;
                }
                while (percentages[index] >= .01d) {
                    Calibrater.shiftRatings(percentages, index, magnitude * -1d, PRECISION);
                    multiplier++;
                }
                int nextIndex = index == 3 ? 0 : index + 1;
                while (percentages[index] >= .01d) {
                    Calibrater.shiftRatings(percentages, nextIndex, magnitude * -1d, PRECISION);
                    multiplier++;
                }
                while (percentages[nextIndex] <= .99d) {
                    Calibrater.shiftRatings(percentages, nextIndex, magnitude, PRECISION);
                    multiplier++;
                }
            }

            double error = ERROR + (ERROR * multiplier);

            assertEquals(1d, percentages[0], error);
            assertEquals(0d, percentages[1], error);
            assertEquals(0d, percentages[2], error);
            assertEquals(0d, percentages[3], error);

            double sum = 0d;
            Calibrater.recalibrateRatings(percentages);
            for (double percent : percentages) sum += percent;
            assertEquals(1d, sum, ERROR);
        }
    }

    /**
     * Asserts whether incrementing and decrementing through the range of possible values for
     * two adjacent {@code double} arrays assigns the expected value to each array element.
     */
    @Test public final void testShiftDoubleArrayResetSingleRepeatRange() {

        double[] percentages = { .25d, .25d, .25d, .25d };
        for (double magnitude = 0.01d; magnitude < 0.1d; magnitude += 0.01d) {
            for (int index = 0; index < 4; index++) {
                double sum;


                for (int i = 0; percentages[index] < 1d; i++) {
                    Calibrater.shiftRatings(percentages, index, magnitude / 2, PRECISION);
                    sum = 0;
                    for (int j = 0; j < percentages.length; j++) {
                        sum += percentages[j];
                    }
                    double error = ERROR + (ERROR * i++);
                    assertEquals(1d, sum, error);
                }
                Calibrater.shiftRatings(percentages, index, -magnitude / 2, PRECISION);
                Calibrater.shiftRatings(percentages, index, -magnitude / 2, PRECISION);
                Calibrater.shiftRatings(percentages, index, magnitude / 2, PRECISION);
                Calibrater.shiftRatings(percentages, index, magnitude / 2, PRECISION);
                assertEquals(1d, percentages[index], ERROR);
                sum = 0;
                for (int i = 0; i < percentages.length; i++) {
                    sum += percentages[i];
                    if (i == percentages.length - 1) assertEquals(1d, sum, ERROR);
                }

                while (percentages[index] > 0d) {
                    Calibrater.shiftRatings(percentages, index, -magnitude / 2, PRECISION);
                    sum = 0;
                    for (int i = 0; i < percentages.length; i++) {
                        sum += percentages[i];
                        if (i == percentages.length - 1) assertEquals(1d, sum, ERROR);
                    }
                }
                Calibrater.shiftRatings(percentages, index, magnitude / 2, PRECISION);
                Calibrater.shiftRatings(percentages, index, magnitude / 2, PRECISION);
                Calibrater.shiftRatings(percentages, index, -magnitude / 2, PRECISION);
                Calibrater.shiftRatings(percentages, index, -magnitude / 2, PRECISION);
                assertEquals(0d, percentages[index], ERROR);
                sum = 0;
                for (int i = 0; i < percentages.length; i++) {
                    sum += percentages[i];
                    if (i == percentages.length - 1) assertEquals(1d, sum, ERROR);
                }

                assertFalse(Calibrater.resetRatings(percentages, PRECISION));
            }
        }
    }

    /**
     * Asserts whether shifting {@code double} array elements to offset an element
     * within a certain margin past the lower limit does not over allocate the unused offset.
     */
    @Test public final void testShiftDoubleArrayLimitAllocation() {

        for (double magnitude = 0.001d; magnitude < 1d; magnitude *= 10) {
            double[] percentages = { 1d, 0d, 0d, 0d, 0d };
            Calibrater.shiftRatings(percentages, 1, magnitude, PRECISION);
            Calibrater.shiftRatings(percentages, 1, magnitude, PRECISION);
            Calibrater.shiftRatings(percentages, 2, magnitude, PRECISION);
            Calibrater.shiftRatings(percentages, 2, magnitude, PRECISION);
            Calibrater.shiftRatings(percentages, 3, magnitude, PRECISION);
            Calibrater.shiftRatings(percentages, 3, magnitude, PRECISION);
            Calibrater.shiftRatings(percentages, 4, magnitude, PRECISION);

            while (percentages[3] > magnitude / 2)
                Calibrater.shiftRatings(percentages, 1, magnitude, PRECISION);

            double sum = 0;
            for (double percentage : percentages) sum += percentage;
            double error = ERROR + (ERROR * percentages.length);
            assertEquals(1d, sum, error);
        }
    }

    /**
     * Asserts whether shifting a {@code double} array with different magnitudes returns
     * the expected boolean value.
     */
    @Test public final void testShiftDoubleArrayReturnValue() {

        double[] percentages = {0d, 0d, 0d, 0d};
        assertFalse(Calibrater.shiftRatings(percentages, 0, -0.01d, PRECISION));
        assertFalse(Calibrater.shiftRatings(percentages, 0, 0d, PRECISION));

        percentages[0] = 1d;
        assertFalse(Calibrater.shiftRatings(percentages, 0, 0.02d, PRECISION));
        assertFalse(Calibrater.shiftRatings(percentages, 0, 0d, PRECISION));

        for (int i = 0; i < percentages.length; i++) percentages[i] = .5d;
        assertTrue(Calibrater.shiftRatings(percentages, 0, -0.1d, PRECISION));
        assertTrue(Calibrater.shiftRatings(percentages, 0, 0.1d, PRECISION));

        for (int i = 0; i < percentages.length; i++) percentages[i] = .1d;
        assertTrue(Calibrater.shiftRatings(percentages, 0, -0.2d, PRECISION));
        assertTrue(Calibrater.shiftRatings(percentages, 0, 0.02d, PRECISION));
    }
    
    /**
     * Asserts whether resetting a {@code double} array assigns the expected value to each array element
     * and results in the expected sum of all elements.
     */
    @Test public final void testResetDoubleArrayEqualDistribution() {

        double magnitude = 1d;
        for (int i = 1; i < 20; i++) {
            double[] percentages = new double[i];
            for (int j = 0; j < percentages.length; j++) percentages[j] = 0d;
            Calibrater.resetRatings(percentages, PRECISION);
            assertEquals(1d / i, percentages[i - 1], 0d);
            double sum = 0d;
            for (Double percentage : percentages) sum += percentage;
            assertEquals(1d, sum, PRECISION);
        }
    }

    /**
     * Asserts whether resetting {@code double} arrays with different element values returns
     * the expected boolean value.
     */
    @Test public final void testResetDoubleArrayReturnValue() {

        double[] percentages = {1d, 0d, 0d, 0d};
        for (int i = 0; i < 2; i++) {
            assertFalse(Calibrater.resetRatings(percentages, PRECISION));
            percentages[0] -= .25d;
            percentages[3 - i] +=.25d;
        }
        
        percentages[3] = .5d;
        assertTrue(Calibrater.resetRatings(percentages, PRECISION));

        for (int i = 0; i < percentages.length - 1; i++) percentages[i] = 0d;
        assertTrue(Calibrater.resetRatings(percentages, PRECISION));

        percentages[3] = 0d;
        assertTrue(Calibrater.resetRatings(percentages, PRECISION));
    }

    /**
     * Asserts whether shifting a {@code double} array with invalid magnitudes and precisions throws
     * the expected exception.
     */
    @Test public final void testShiftWithIllegalArguments() {

        double[] percentages = {0d, 0d, 0d, 0d};

        assertShiftThrows(IllegalArgumentException.class, percentages, 0, 1.0001d, PRECISION);
        assertShiftThrows(IllegalArgumentException.class, percentages, 0, -1.0001d, PRECISION);
        assertShiftThrows(IllegalArgumentException.class, percentages, 0, 0d, 17);
        assertShiftThrows(IllegalArgumentException.class, percentages, 0, 0d, -1);
    }

    /**
     * Asserts whether the parameter {@link Class} of type {@link Throwable} is thrown when
     * invoking {@link Calibrater#shiftRatings(double[], int, double, int)} with the parameter args
     */
    private static <T extends Throwable>void assertShiftThrows(Class<T> throwableClass, Object... args) {

        try {
            Calibrater.shiftRatings(
                    (double[]) args[0], (int) args[1], (double) args[2], (int) args[3]
            );
            fail(); // Exception not thrown

        } catch (Throwable e) {
            assertTrue(throwableClass.isInstance(e));
        }
    }
}