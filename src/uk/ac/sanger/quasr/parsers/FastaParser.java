package uk.ac.sanger.quasr.parsers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import uk.ac.sanger.quasr.records.FastaRecord;

/**
 *
 * @author sw10
 */
public class FastaParser {
    
    private String infile;
    private BufferedReader binfh;
    private String header, sequence;
    
    public FastaParser(String infile) throws IOException {
        this.infile = infile;
        openFile(infile);
        checkIfFastaFile(binfh);
    }
    
    private void openFile(String infile) throws IOException {
        if (infile.endsWith(".gz") || (infile.endsWith(".gzip"))) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(infile));
            binfh = new BufferedReader(new InputStreamReader(gzip));
        } else {
            binfh = new BufferedReader(new FileReader(infile));
        }
    }
    
    private void checkIfFastaFile(BufferedReader binfh) throws IOException {
        int counter = 0;
        while (true) {
            String line = binfh.readLine();
            if (line.startsWith(">")) {
                header = line;
                sequence = "";
                break;
            }
            counter++;
            if (counter == 20) {
                throw new RuntimeException("This does not appear to be a FASTA file");
            }
        }
    }
    
    public FastaRecord getNextRecord() throws IOException {
        while(true) {
            String line = binfh.readLine();
            if (line == null) {
                if (header.equals("")) {
                    return null;
                } else {
                    FastaRecord record = new FastaRecord(header, sequence);
                    header = "";
                    sequence = "";
                    return record;
                }
            }
            if (line.equals("")) {
                continue;
            }
            if (line.startsWith(">")) {
                if (!header.equals("") && !sequence.equals("")) {
                    FastaRecord record = new FastaRecord(header, sequence);
                    header = line;
                    sequence = "";
                    return record;
                }
                header = line;
            } else {
                sequence += line;
            }
        }
    }
    
    public void close() throws IOException {
        binfh.close();
    }
    
    public void reopen() throws IOException {
        this.close();
        this.openFile(infile);
    }
}
