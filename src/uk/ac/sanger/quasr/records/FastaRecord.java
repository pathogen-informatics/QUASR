/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.sanger.quasr.records;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 * @author sw10
 */
public class FastaRecord {

    protected String header, sequence;
    protected int mid = 0;

    public FastaRecord(String header, String sequence) {
        this.header = header.substring(1);
        this.sequence = sequence;
    }

    public String getHeader() {
        return header;
    }

    public void setMID(int num) {
        mid = num;
    }

    public int getMID() {
        return mid;
    }

    public int getLength() {
        return sequence.length();
    }
    
    /**
     * Returns the nucleotide sequence.
     * @return the string containing the nucleotide sequence.
     */
    public String getSequence() {
        return sequence;
    }
    
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
    
    protected void setSequence(int start) {
        sequence = sequence.substring(start);
    }
    
    protected void setSequence(int start, int end) {
        sequence = sequence.substring(start, end);
    }
    
    public void writeToFasta(OutputStreamWriter out) throws IOException {
        if (!header.startsWith(">")) {
            out.write(">");
        }
        out.write(header + "\n" + sequence + "\n");
    }
}
