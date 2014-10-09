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
public class FastqRecord extends FastaRecord implements SGSRecord {

    private String quality;
    private boolean passedQC = true;

    public FastqRecord(String header, String sequence, String quality) throws RuntimeException {
        super(header, sequence);
        checkQualityLength(quality);
        this.quality = quality;
    }

    private void checkQualityLength(String quality) throws RuntimeException {
        if (quality.length() != this.sequence.length()) {
            throw new RuntimeException("Sequence and quality length mismatch");
        }
    }
    
    /**
     * Sets the starting position of the sequence and quality strings.
     * to the value of start.
     * @param start the integer to which the start of the sequence and quality
     * strings are set.
     */
    @Override
    public void setSequence(int start) {
        sequence = sequence.substring(start);
        quality = quality.substring(start);
    }

    /**
     * Sets the start and end positions of the sequence and quality strings.
     * Creates a substring between the start and end values.
     * @param start the integer to which the start of the sequence and quality
     * strings are set.
     * @param end the integer to which the end of the sequence and quality
     * strings are set.
     */
    @Override
    public void setSequence(int start, int end) {
        sequence = sequence.substring(start, end);
        quality = quality.substring(start, end);
    }

    /**
     * Write the header, sequence, and quality strings as a FASTQ file. If the
     * header does not contain an @ sign, it adds it in.
     * @param outFile the file handle to which to write
     * @throws IOException 
     */
    @Override
    public void writeToFastq(Writer outFile) throws IOException {
        if (!header.startsWith("@")) {
            outFile.write("@");
        }
        outFile.write(header + "\n" + sequence + "\n+\n" + quality + "\n");
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

    /**
     * Sets the boolean flag for whether the record has passed quality control.
     * This is particularly for use with the QualityControl class.
     * @param flag set to true if passed QC. Default is false.
     */
    @Override
    public void setPassedQCFlag(boolean flag) {
        passedQC = flag;
    }

    /**
     * Returns the flag associated with whether the record has passed quality
     * control. Set to false by default.
     * @return the boolean flag indicating whether the record has passed quality
     * control
     */
    @Override
    public boolean getPassedQCFlag() {
        return passedQC;
    }

    /**
     * Returns the ASCII-encoded qualities as a string.
     * @return a string containing the ASCII-encoded qualities
     */
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
