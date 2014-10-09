package uk.ac.sanger.quasr.modules;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sw10
 */
public class MIDParser {

    public static List<Integer> midCount = new ArrayList<Integer>();
    public static void parseMIDNums(String mids) throws NumberFormatException {
        
        String[] nums = mids.split(",\\s*");
        for (int i = 0; i < nums.length; i++) {
            if (nums[i].contains("-")) {
                String[] range = nums[i].split("-");
                if (range.length != 2) {
                    throw new NumberFormatException("MID range not given as N-M: " + nums[i]);
                }
                int lower = Integer.parseInt(range[0]);
                int upper = Integer.parseInt(range[1]);
                if (lower > upper) {
                    throw new NumberFormatException("Lower MID range greater than upper: " + nums[i]);
                }
                for (int j = lower; j <= upper; j++) {
                    midCount.add(j);
                }
            } else {
                try {
                    midCount.add(Integer.parseInt(nums[i]));
                } catch (NumberFormatException err) {
                    System.err.println("[WARNING]: Ignoring unparseable MID: " + nums[i]);
                }
            }
        }
        System.out.print("[INFO]: MIDs parsed: ");
        for(int num : midCount) {
            System.out.print(num + " ");
        }
        System.out.println("");
    }
}
