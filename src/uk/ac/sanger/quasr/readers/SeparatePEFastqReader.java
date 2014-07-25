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
public class SeparatePEFastqReader implements PEFastqReader {

    private FastqParser forParser, revParser;
    private List<SGSRecord> forRecords, revRecords;
    
    public SeparatePEFastqReader(String forInfile, String revInfile) throws IOException {
        forParser = new FastqParser(forInfile);
        revParser = new FastqParser(revInfile);
        forRecords = new ArrayList<SGSRecord>();
        revRecords = new ArrayList<SGSRecord>();
    }
    
    @Override
    public void readNextRecords(int num) throws IOException {
        forRecords.clear();
        revRecords.clear();
        for (int i = 0; i < num * 2; i++) {
            SGSRecord forRecord = forParser.getNextRecord();
            SGSRecord revRecord = revParser.getNextRecord();
            if (forRecord == null && revRecord == null) {
                break;
            } else if (forRecord == null || revRecord == null) {
                throw new RuntimeException("Unequal mate pair numbers");
            } else {
                forRecords.add(forRecord);
                revRecords.add(revRecord);
            }
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
        forParser.close();
        revParser.close();
    }
    
    @Override
    public void reopen() throws IOException {
        forParser.reopen();
        revParser.reopen();
    }
}
