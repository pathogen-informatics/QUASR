package uk.ac.sanger.quasr;

import java.util.List;
import uk.ac.sanger.quasr.records.SGSRecord;

/**
 *
 * @author sw10
 */
public interface PreAssemblyInterface {
    abstract void parseRecord(SGSRecord record);
    abstract void parseRecords(SGSRecord forRecord, SGSRecord revRecord);
    abstract void parseRecords(List<SGSRecord> records);
    abstract void parseRecords(List<SGSRecord> forRecords, List<SGSRecord> revRecords);
    abstract void printOutputStats();
}
