package uk.ac.sanger.quasr.readers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import uk.ac.sanger.quasr.records.SGSRecord;
import uk.ac.sanger.quasr.parsers.FastqParser;

/**
 *
 * @author sw10
 */
public class FastqReader implements SGSReader {
    
    private FastqParser parser;
    private List<SGSRecord> records;
    
    public FastqReader(String infile) throws IOException {
        parser = new FastqParser(infile);
        records = new ArrayList<SGSRecord>();
    }
    
    //@Override
    private void readNextRecords(int num) throws IOException {
        records.clear();
        for (int i=0; i<num; i++) {
            SGSRecord rec = parser.getNextRecord();
            if (rec == null) {
                break;
            } else {
                records.add(rec);
            }
        }
    }
    
    @Override
    public List<SGSRecord> getRecords(int num) throws IOException {
        readNextRecords(num);
        return records;
    }
    
    @Override
    public void close() throws IOException {
        parser.close();
    }
    
    @Override
    public void reopen() throws IOException {
        parser.reopen();
    }
}
