package uk.ac.sanger.quasr.readers;

import java.io.IOException;
import java.util.List;
import uk.ac.sanger.quasr.records.SGSRecord;

/**
 *
 * @author sw10
 */
public interface PEFastqReader {
    public abstract void readNextRecords(int num) throws IOException;
    public abstract List<SGSRecord> getForwardRecords();
    public abstract List<SGSRecord> getReverseRecords();
    public abstract void close() throws IOException;
    public abstract void reopen() throws IOException;
}
