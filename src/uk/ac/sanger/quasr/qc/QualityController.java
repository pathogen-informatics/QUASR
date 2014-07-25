package uk.ac.sanger.quasr.qc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import uk.ac.sanger.quasr.PreAssemblyInterface;
import uk.ac.sanger.quasr.records.SGSRecord;
import uk.ac.sanger.quasr.invariables.Invariables;
import uk.ac.sanger.quasr.modules.Stats;

/**
 *
 * @author sw10
 */
public class QualityController implements PreAssemblyInterface {

    private int lengthCutoff;
    private float medianCutoff;
    private int failed, passed;
    private float totalTrimmed;

    public QualityController(int lengthCutoff, float medianCutoff) {
        this.lengthCutoff = lengthCutoff;
        this.medianCutoff = medianCutoff;
        failed = 0;
        passed = 0;
        totalTrimmed = 0;
    }

    @Override
    public void parseRecord(SGSRecord record) {
        if (record.getLength() < lengthCutoff) {
            record.setPassedQCFlag(false);
            failed++;
            return;
        }
        List<Integer> phreds = new ArrayList<Integer>(Arrays.asList(Invariables.convertASCIIToPhred(record.getQuality())));
        int index = phreds.size() - 1;
        int numTrimmed = index;
        List<Integer> sortedPhreds = new ArrayList(phreds);
        Collections.sort(sortedPhreds);
        while (true) {
            if (Stats.calculateMedian(sortedPhreds) >= medianCutoff) {
                record.setSequence(0, index);
                record.setPassedQCFlag(true);
                passed++;
                totalTrimmed += (numTrimmed - index);
                return;
            }
            index--; // effectively removing base
            if (index < lengthCutoff) {
                record.setPassedQCFlag(false);
                failed++;
                return;
            }
            int qual = phreds.get(index);
            sortedPhreds.remove(sortedPhreds.indexOf(qual));
        }
    }

    @Override
    public void parseRecords(SGSRecord forRecord, SGSRecord revRecord) {
        parseRecord(revRecord); // reverse first as more likely to fail
        if (revRecord.getPassedQCFlag() == false) {
            forRecord.setPassedQCFlag(false);
        } else {
            parseRecord(forRecord);
            if (forRecord.getPassedQCFlag() == false) {
                revRecord.setPassedQCFlag(false);
            }
        }
    }

    @Override
    public void parseRecords(List<SGSRecord> records) {
        for (SGSRecord record : records) {
            parseRecord(record);
        }
    }

    @Override
    public void parseRecords(List<SGSRecord> forRecords, List<SGSRecord> revRecords) {
        for (int i = 0; i < forRecords.size(); i++) {
            SGSRecord forRecord = forRecords.get(i);
            SGSRecord revRecord = revRecords.get(i);
            parseRecord(revRecord); // reverse first as more likely to fail
            if (revRecord.getPassedQCFlag() == false) {
                forRecord.setPassedQCFlag(false);
            } else {
                parseRecord(forRecord);
                if (forRecord.getPassedQCFlag() == false) {
                    revRecord.setPassedQCFlag(false);
                }
            }
        }
    }

    @Override
    public void printOutputStats() {
        int total = passed + failed;
        float pass_perc = (passed / (float)total) * 100;
        float fail_perc = (failed / (float)total) * 100;
        System.out.println("[INFO]: Quality control statistics:");
        System.out.println("\tReads read: " + total);
        System.out.printf("\tReads passed: %d (%.2f%%)\n", passed, pass_perc);
        System.out.printf("\tReads failed: %d (%.2f%%)\n", failed, fail_perc);
        System.out.printf("\tMean bases trimmed per read: %.0f\n", (totalTrimmed / total));
    }
}
