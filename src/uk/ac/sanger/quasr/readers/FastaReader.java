/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.sanger.quasr.readers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import uk.ac.sanger.quasr.parsers.FastaParser;
import uk.ac.sanger.quasr.records.FastaRecord;

/**
 *
 * @author sw10
 */
public class FastaReader {
    
    private FastaParser parser;
    private List<FastaRecord> records;
    
    public FastaReader(String infile) throws IOException {
        parser = new FastaParser(infile);
        records = new ArrayList<FastaRecord>();
    }
    
    private void readNextRecords(int num) throws IOException {
        records.clear();
        for (int i=0; i<num; i++) {
            FastaRecord rec = parser.getNextRecord();
            if (rec == null) {
                break;
            } else {
                records.add(rec);
            }
        }
    }
    
    public List<FastaRecord> getRecords(int num) throws IOException {
        readNextRecords(num);
        return records;
    }
    
    public void close() throws IOException {
        parser.close();
    }
    
    public void reopen() throws IOException {
        parser.reopen();
    }
}
