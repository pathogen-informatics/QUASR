package uk.ac.sanger.quasr.writers;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;
import uk.ac.sanger.quasr.records.SGSRecord;

/**
 *
 * @author sw10
 */
public class FastqWriter {
    
    private String outfile;
    private boolean isCompressed = false;
    private OutputStreamWriter outfh;
    
    public FastqWriter(String outfile, boolean isCompressed) throws IOException {
        this.outfile = outfile;
        this.isCompressed = isCompressed;
        openOutputHandle();
        //System.out.println("[INFO]: Successfully opened \"" + this.outfile + "\"");
    }
    
    private void openOutputHandle() throws IOException {
        if (isCompressed == true) {
            outfile += ".gz";
            outfh = new OutputStreamWriter(new GZIPOutputStream(
                        new BufferedOutputStream(new FileOutputStream(outfile))));
        } else {
            outfh = new OutputStreamWriter(new BufferedOutputStream(
                    new FileOutputStream(outfile)));
        }
    }
    
    public void writeToFastq(SGSRecord record) throws IOException {
        outfh.write("@" + record.getHeader() + "\n" +
                record.getSequence() + "\n" +
                "+\n" +
                record.getQuality() + "\n");
    }
    
    public void close() throws IOException {
        outfh.close();
        System.out.println("[INFO]: Sequences written to \"" + outfile + "\"");
    }
}
