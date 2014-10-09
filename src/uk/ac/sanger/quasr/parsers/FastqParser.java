package uk.ac.sanger.quasr.parsers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import uk.ac.sanger.quasr.records.FastqRecord;
import uk.ac.sanger.quasr.records.SGSRecord;

/**
 *
 * @author sw10
 */
public class FastqParser {

    private BufferedReader binfh;
    private String header, sequence, quality, infile;

    public FastqParser(String infile) throws IOException {
        this.infile = infile;
        openFile(infile);
        checkIfFastqFile(binfh);
        System.out.println("[INFO]: Successfully opened \"" + infile + "\"");
    }

    private void openFile(String infile) throws IOException {
        if (infile.endsWith(".gz") || (infile.endsWith(".gzip"))) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(infile));
            binfh = new BufferedReader(new InputStreamReader(gzip));
        } else {
            binfh = new BufferedReader(new FileReader(infile));
        }
    }

    private void checkIfFastqFile(BufferedReader binfh) throws IOException {
        int counter = 0;
        while (true) {
            String line = binfh.readLine();
            if (line.startsWith("@")) {
                header = line;
                sequence = "";
                quality = "";
                break;
            }
            counter++;
            if (counter == 20) {
                throw new RuntimeException("This does not appear to be a FASTQ file");
            }
        }
    }

    public SGSRecord getNextRecord() throws IOException {
        boolean isQuality = false;
        while (true) {
            String line = binfh.readLine();
            if (line == null) {
                if (header.equals("")) {
                    return null;
                } else {
                    SGSRecord record = new FastqRecord(header, sequence, quality);
                    header = "";
                    sequence = "";
                    quality = "";
                    return record;
                }
            }
            if (line.equals("")) {
                continue;
            }
            if (line.startsWith("@")) {
                if (sequence.equals("")) {
                    header = line;
                } else if (sequence.length() == quality.length()) {
                    SGSRecord record = new FastqRecord(header, sequence, quality);
                    header = line;
                    sequence = "";
                    quality = "";
                    return record;
                } else {
                    quality += line;
                }
            } else if (line.startsWith("+")) {
                if (isQuality == false) {
                    isQuality = true;
                } else {
                    quality += line;
                }
            } else {
                if (isQuality == false) {
                    sequence += line;
                } else {
                    quality += line;
                }
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
