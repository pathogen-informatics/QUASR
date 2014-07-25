package uk.ac.sanger.quasr.modules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sw10
 */
public class Stats {

    public static int calculateRoundedMedian(Integer[] numbers) {
        Arrays.sort(numbers);
        float middle = numbers.length / 2;
        if (middle % 2 == 0) {
            return numbers[((int) middle) + 1];
        } else {
            return numbers[(int) Math.ceil(middle)];
        }
    }

    public static float calculateMedian(Integer[] numbers) {
        Arrays.sort(numbers);
        int middle = numbers.length / 2;
        if (middle % 2 == 0) {
            return (numbers[middle] + numbers[middle + 1]) / 2;
        } else {
            return (float) numbers[(int) Math.ceil((middle))];
        }
    }

    public static float calculateMedian(List<Integer> numbers) {
        Collections.sort(numbers);
        int middle = numbers.size() / 2;
        if (middle % 2 == 0) {
            return (numbers.get(middle) + numbers.get(middle + 1)) / (float) 2;
        } else {
            return (float) numbers.get((int) Math.ceil(middle));
        }
    }

    public static float calculateMean(Integer[] numbers) {
        float total = 0;
        for (int i : numbers) {
            total += i;
        }
        return total / numbers.length;
    }
}
