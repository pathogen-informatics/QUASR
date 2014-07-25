/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.sanger.quasr.records;

import uk.ac.sanger.quasr.invariables.Invariables;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import uk.ac.sanger.quasr.modules.Stats;

/**
 *
 * @author sw10
 */
public class SFFRecord implements SGSRecord {

    private String header, sequence, quality;
    private static String key;
    private int mid = 0;
    private boolean passedQC = true;

    public SFFRecord(String header, String sequence, byte[] phreds) {
        this.header = header;
        this.sequence = sequence;
        this.quality = Invariables.convertPhredToASCII(phreds);
        removeKeySequence();
    }

    private void removeKeySequence() {
        assert (sequence.startsWith(SFFRecord.key));
        setSequence(SFFRecord.key.length());
    }

    public static void setKey(String key) {
        SFFRecord.key = key;
    }

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public String getSequence() {
        return sequence;
    }

    @Override
    public void setMID(int num) {
        mid = num;
    }

    @Override
    public int getMID() {
        return mid;
    }

    @Override
    public void setPassedQCFlag(boolean flag) {
        passedQC = flag;
    }

    @Override
    public int getLength() {
        return sequence.length();
    }

    @Override
    public void setSequence(int start) {
        sequence = sequence.substring(start);
        quality = quality.substring(start);
    }

    @Override
    public void setSequence(int start, int end) {
        sequence = sequence.substring(start, end);
        quality = quality.substring(start, end);
    }

    @Override
    public void writeToFastq(Writer outFile) throws IOException {
        if (!header.startsWith("@")) {
            outFile.write("@");
        }
        outFile.write(header + "\n" + sequence + "\n+\n" + quality + "\n");
    }

    @Override
    public boolean getPassedQCFlag() {
        return passedQC;
    }

    @Override
    public String getQuality() {
        return quality;
    }
    
    @Override
    public float calcMeanQualityScore() {
        return Stats.calculateMean(Invariables.convertASCIIToPhred(quality));
    }
    
    @Override
    public float calcMedianQualityScore() {
        return Stats.calculateMedian(Invariables.convertASCIIToPhred(quality));
    }
    
    @Override
    public int calcRoundedMedianQualityScore() {
        return Stats.calculateRoundedMedian(Invariables.convertASCIIToPhred(quality));
    }

    /**
     * Write the header, sequence, and quality strings as an array of bytes to
     * an OutputStreamWriter. If the header does not start with an @ sign, it
     * adds it in.
     * @param binFile the compressed output stream to which tow rite
     * @throws IOException 
     */
    @Override
    public void writeToFastq(OutputStreamWriter outFile) throws IOException {
        if (!header.startsWith("@")) {
            outFile.write("@");
        }
        outFile.write(header + "\n" + sequence + "\n+\n" + quality + "\n");
    }

    @Override
    public float calcGCPercentage() {
        float count = 0.0F;
        int l = this.getLength();
        for(int i=0; i<l; i++) {
            if (sequence.charAt(i) == 'G' || sequence.charAt(i) == 'C' ||
                    sequence.charAt(i) == 'g' || sequence.charAt(i) == 'c') {
                count++;
            }
        }
        return (count/l)*100;
    }
}
