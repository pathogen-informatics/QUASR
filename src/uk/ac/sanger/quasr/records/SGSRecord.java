/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.sanger.quasr.records;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 *
 * @author sw10
 */
public interface SGSRecord {
    public abstract String getSequence();
    public abstract void setMID(int num);
    public abstract int getMID();
    public abstract void setSequence(int start);
    public abstract void setSequence(int start, int end);
    public abstract String getHeader();
    public abstract void writeToFastq(Writer outFile) throws IOException;
    public abstract int getLength();
    public abstract void setPassedQCFlag(boolean flag);
    public abstract boolean getPassedQCFlag();
    public abstract String getQuality();
    public abstract void writeToFastq(OutputStreamWriter outFile) throws IOException;
    public abstract float calcMeanQualityScore();
    public abstract float calcMedianQualityScore();
    public abstract int calcRoundedMedianQualityScore();
    public abstract float calcGCPercentage();
}
