package uk.ac.sanger.quasr.pileup;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static uk.ac.sanger.quasr.invariables.Invariables.*;

public class PileupLine {

    private String refName, readBases;
    private StringBuilder readQuals;
    private int refPos, readDepth;
    public char refBase;
    private static final Pattern p = Pattern.compile("\\d+");
    private TreeMap<String, Integer> indels = new TreeMap<String, Integer>();
    private Map<Character, Float> baseFreqs = new TreeMap<Character, Float>();
    private Map<Character, Integer> baseCounts = new TreeMap<Character, Integer>();

    public PileupLine(String refName, int refPos, char refBase, int readDepth, String readBases, String readQuals) {
        this.refName = refName;
        this.refPos = refPos;
        this.refBase = refBase;
        this.readDepth = readDepth;
        this.readQuals = new StringBuilder(readQuals);
        decodeReadBases(readBases);
    }

    private void decodeReadBases(String readBases) {
        StringBuilder output = new StringBuilder(readBases.length());
        for (int i = 0; i < readBases.length(); i++) {
            char c = readBases.charAt(i);
            if (c == '^') {
                i++;
            } else if (c == '$') {
            } else if (c == '.' || c == ',') {
                output.append(refBase);
            } else {
                output.append(c);
            }
        }
        readBases = output.toString();
        Matcher m = p.matcher(readBases);
        while (m.find()) {
            Pattern p2 = Pattern.compile("(\\+|-)" + m.group() + "(A|C|G|T|M|R|W|S|Y|K|V|H|D|B|X|N){" + m.group() + "}", Pattern.CASE_INSENSITIVE);
            Matcher m2 = p2.matcher(readBases);
            StringBuffer sb = new StringBuffer();
            while (m2.find()) {
                m2.appendReplacement(sb, "");
                if (m2.group().startsWith("+")) {
                    String b = m2.group().replace("+" + m.group(), "");
                    Integer count = indels.get(b.toUpperCase());
                    if (count == null) {
                        count = 0;
                    }
                    indels.put(b.toUpperCase(), count + 1);
                }
            }
            m2.appendTail(sb);
            readBases = sb.toString();
        }
        this.readBases = readBases.toUpperCase();
    }

    public void removeLowQualBases(int phredCutoff) {
        Integer[] phreds = convertASCIIToPhred(readQuals.toString());
        assert (readBases.length() == readQuals.length());
        StringBuilder b = new StringBuilder(readBases);
        for (int i = phreds.length - 1; i >= 0; i--) {
            if (phreds[i] < phredCutoff) {
                b.deleteCharAt(i);
                readQuals.deleteCharAt(i);
            }
        }
        readBases = b.toString();
        assert (readBases.length() == readQuals.length());
        readDepth = b.length();
    }

    public char getConsensusBase(float ambiguityCutoff) throws RuntimeException {
        // PileupReader parses gaps in mapping as ""
        if (readBases.equals("")) {
            return '-';
        }
        if (baseFreqs.isEmpty()) {
            calculateBaseFrequencies();
        }
        if (baseFreqs.containsKey('*')) {
            if (baseFreqs.get('*') > 0.5) {
                return '-';
            } else {
                baseFreqs.remove('*');
            }
        }
        StringBuilder output = new StringBuilder(4);
        for (Character base : baseFreqs.keySet()) {
            if (baseFreqs.get(base) >= ambiguityCutoff) {
                output.append(base);
            }
        }
        if (output.length() == 1) {
            return output.charAt(0);
        } else if (output.length() > 1) {
            return determineAmbiguityCode(output);
        } else {
            StringBuilder maxBase = new StringBuilder(4);
            float maxFreq = 0.0F;
            for (Character base : baseFreqs.keySet()) {
                if (baseFreqs.get(base) >= maxFreq) {
                    maxBase.append(base);
                    maxFreq = baseFreqs.get(base);
                }
            }
            if (maxBase.length() == 1) {
                return maxBase.charAt(0);
            } else {
                return determineAmbiguityCode(maxBase);
            }
        }
    }

    private void calculateBaseFrequencies() {
        int total = readBases.length();
        if (baseCounts.isEmpty()) {
            for (int i = 0; i < total; i++) {
                Integer count = baseCounts.get(readBases.charAt(i));
                if (count == null) {
                    count = 0;
                }
                baseCounts.put(readBases.charAt(i), count + 1);
            }
        }
        for (Character base : baseCounts.keySet()) {
            baseFreqs.put(base, (float)baseCounts.get(base) / total);
        }
    }

    public Map<Character, Integer> getBaseCounts() {
        if (baseCounts.isEmpty()) {
            int total = readBases.length();
            for (int i = 0; i < total; i++) {
                Integer count = baseCounts.get(readBases.charAt(i));
                if (count == null) {
                    count = 0;
                }
                baseCounts.put(readBases.charAt(i), count + 1);
            }
        }
        return baseCounts;
    }

    private Character determineAmbiguityCode(StringBuilder input) {
        Character ambiguityCode = AMBIGUITYCODES.get(input.toString());
        if (ambiguityCode == null) {
            System.err.println("Unable to parse '" + input + "' to ambiguity code at '" + refName + " " + refPos + "'. Adding 'N' to consensus");
            return 'N';
        } else {
            return ambiguityCode;
        }
    }

    public Map<Character, Float> getBaseFrequencies() {
        if (baseFreqs.isEmpty()) {
            calculateBaseFrequencies();
        }
        return baseFreqs;
    }

    public String getReadBases() {
        return readBases;
    }

    public int getReadDepth() {
        return readDepth;
    }

    public char getRefBase() {
        return refBase;
    }

    public String getRefName() {
        return refName;
    }

    public int getRefPos() {
        return refPos;
    }

    public TreeMap<String, Integer> getIndelMap() {
        return indels;
    }
}
