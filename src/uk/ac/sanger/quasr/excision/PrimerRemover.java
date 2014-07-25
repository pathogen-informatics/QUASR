package uk.ac.sanger.quasr.excision;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import uk.ac.sanger.quasr.PreAssemblyInterface;
import uk.ac.sanger.quasr.demultiplexers.AbstractSequenceRemover;
import uk.ac.sanger.quasr.records.SGSRecord;

/**
 *
 * @author sw10
 */
public class PrimerRemover extends AbstractSequenceRemover implements PreAssemblyInterface {

    private Map<String, Integer> substringCounts = new HashMap<String, Integer>();
    
    public PrimerRemover(String substringFile, int leeway) throws IOException {
        super(substringFile, leeway);
        parseFile();
    }

    private void parseFile() throws IOException {
        // populate the substrings map
        BufferedReader binfh = new BufferedReader(new FileReader(substringFile));
        try {
            while (true) {
                String line = binfh.readLine();
                if (line == null) {
                    break;
                }
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = separator.split(line);
                switch (parts.length) {
                    case 1:
                        fSubstrings.add(convertStringToPattern(parts[0]));
                        rSubstrings.add(null);
                        break;
                    case 2:
                        fSubstrings.add(convertStringToPattern(parts[0]));
                        rSubstrings.add(convertStringToPattern(parts[1]));
                        break;
                    default:
                        System.err.println("Ignoring incorrectly formatted line:\n\t" + line);
                        break;
                }
            }
        } finally {
            binfh.close();
            System.out.println("[INFO]: Successfully parsed \"" + substringFile + "\"");
        }
        if (fSubstrings.size() != rSubstrings.size()) {
            throw new RuntimeException("Unable to pair forward and reverse sequences in input file \"" + substringFile + "\"");
        }
    }

    @Override
    public void parseRecord(SGSRecord record) {
        Matcher[] matches = super.matchSubstringInRecord(record.getSequence());
        if (matches[0] == null && matches[1] == null) {
            return;
        }
        int start, end;
        if (matches[0] == null) {
            start = 0;
        } else {
            start = matches[0].end();
            Integer count = substringCounts.get(matches[0].group());
            if (count == null) {
                count = 0;
            }
            substringCounts.put(matches[0].group(), count + 1);
        }
        if (matches[1] == null) {
            end = record.getLength() - 1;
        } else {
            int numGroups = matches[1].groupCount();
            end = matches[1].start(numGroups);
            Integer count = substringCounts.get(matches[1].group(numGroups));
            if (count == null) {
                count = 0;
            }
            substringCounts.put(matches[1].group(numGroups), count + 1);
        }
        
        if (start >= end) {
            System.err.println("[WARNING]: Cannot remove overlapping primer matches for record \"" + record.getHeader() + "\"");
        } else {
            record.setSequence(start, end);
        }
    }
    
    @Override
    public void parseRecords(SGSRecord forRecord, SGSRecord revRecord) {
        parseRecord(forRecord);
        parseRecord(revRecord);
    }
    
    @Override
    public void parseRecords(List<SGSRecord> records) {
        for (SGSRecord record : records) {
            parseRecord(record);
        }
    }
    
    @Override
    public void parseRecords(List<SGSRecord> forRecords, List<SGSRecord> revRecords) {
        parseRecords(forRecords);
        parseRecords(revRecords);
    }
    
    @Override
    public void printOutputStats() {
        System.out.println("[INFO]: Primer removal statistics:");
        for (String substring : substringCounts.keySet()) {
            System.out.println("\tMatches to " + substring + ": " + substringCounts.get(substring));
        }
    }
}