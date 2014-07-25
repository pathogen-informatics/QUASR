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
public class SinglePEFastqReader implements PEFastqReader {

    private FastqParser parser;
    private List<SGSRecord> forRecords, revRecords;

    public SinglePEFastqReader(String infile) throws IOException {
        parser = new FastqParser(infile);
        forRecords = new ArrayList<SGSRecord>();
        revRecords = new ArrayList<SGSRecord>();
    }

    @Override
    public void readNextRecords(int num) throws IOException {
        boolean switcher = true;
        forRecords.clear();
        revRecords.clear();
        for (int i = 0; i < num * 2; i++) {
            SGSRecord rec = parser.getNextRecord();
            if (rec == null) {
                break;
            } else {
                if (switcher == true) {
                    forRecords.add(rec);
                    switcher = false;
                } else {
                    revRecords.add(rec);
                    switcher = true;
                }
            }
        }
        if (forRecords.size() != revRecords.size()) {
            throw new RuntimeException("Unequal mate pair numbers");
        }
    }

    @Override
    public List<SGSRecord> getForwardRecords() {
        return forRecords;
    }

    @Override
    public List<SGSRecord> getReverseRecords() {
        return revRecords;
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
