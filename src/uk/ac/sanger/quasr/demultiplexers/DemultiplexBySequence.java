package uk.ac.sanger.quasr.demultiplexers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import uk.ac.sanger.quasr.invariables.Invariables;
import uk.ac.sanger.quasr.modules.MIDParser;
import uk.ac.sanger.quasr.readers.FastqReader;
import uk.ac.sanger.quasr.readers.SFFReader;
import uk.ac.sanger.quasr.readers.SGSReader;
import uk.ac.sanger.quasr.records.SGSRecord;
import uk.ac.sanger.quasr.writers.FastqWriter;

/**
 *
 * @author sw10
 */
public class DemultiplexBySequence extends AbstractSequenceRemover implements Demultiplexer {

    private SGSReader reader;
    private Map<Integer, Integer> midnumCounts = new HashMap<Integer, Integer>();
    private Map<String, Integer> barcodeToNums = new HashMap<String, Integer>();
    private boolean gzipOutput;
    private String outprefix;
    private Map<Integer, String> outfiles;
    private Map<Integer, FastqWriter> outhandles;
    String infile;

    public DemultiplexBySequence(String infile, String outprefix, String customMIDFile, boolean gzipOutput, String mids, int leeway) throws IOException {
        super(customMIDFile, leeway);
        if (infile.endsWith(".sff") || infile.endsWith(".SFF")) {
            reader = new SFFReader(infile);
        } else {
            reader = new FastqReader(infile);
        }
        this.infile = infile;
        parseFile(customMIDFile);
        this.gzipOutput = gzipOutput;
        MIDParser.parseMIDNums(mids);
        this.outprefix = outprefix;
        outfiles = new HashMap<Integer, String>();
        outhandles = new HashMap<Integer, FastqWriter>();
        midnumCounts = new HashMap<Integer, Integer>();
    }

    public DemultiplexBySequence(String infile, String outprefix, boolean gzipOutput, String mids, int leeway) throws IOException {
        super(leeway);
        if (infile.endsWith(".sff") || infile.endsWith(".SFF")) {
            reader = new SFFReader(infile);
            parseMIDMap(Invariables.ROCHE_MIDS);
        } else {
            reader = new FastqReader(infile);
            parseMIDMap(Invariables.ILLUMINA_MIDS);
        }
        this.infile = infile;
        this.gzipOutput = gzipOutput;
        MIDParser.parseMIDNums(mids);
        this.outprefix = outprefix;
        outfiles = new HashMap<Integer, String>();
        outhandles = new HashMap<Integer, FastqWriter>();
        midnumCounts = new HashMap<Integer, Integer>();
    }

    private void parseMIDMap(Map<Integer, String[]> defaultMIDs) {
        for (int i : defaultMIDs.keySet()) {
            String[] barcodes = defaultMIDs.get(i);
            barcodeToNums.put(barcodes[0], i);
            barcodeToNums.put(barcodes[1], i);
            fSubstrings.add(convertStringToPattern(barcodes[0]));
            rSubstrings.add(convertStringToPattern(barcodes[1]));
        }
    }

    private void parseFile(String midFile) throws IOException {
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
                    case 2:
                        barcodeToNums.put(parts[1], Integer.parseInt(parts[0]));
                        fSubstrings.add(convertStringToPattern(parts[1]));
                        rSubstrings.add(null);
                    case 3:
                        barcodeToNums.put(parts[1], Integer.parseInt(parts[0]));
                        barcodeToNums.put(parts[2], Integer.parseInt(parts[0]));
                        fSubstrings.add(convertStringToPattern(parts[1]));
                        rSubstrings.add(convertStringToPattern(parts[2]));
                    default:
                        System.err.println("Ignoring incorrectly formatted line:\n\t" + line);
                        break;
                }
            }
        } finally {
            binfh.close();
        }
    }

    @Override
    public void performDemultiplexing(int numParsed) throws IOException {
        do {
            //reader.readNextRecords(numParsed);
            List<SGSRecord> records = reader.getRecords(numParsed);
            if (records.isEmpty()) {
                break;
            }
            parseRecords(records);
        } while (true);
        closeFilehandles();
        printOutputStats();
    }

    public void parseRecord(SGSRecord record) throws IOException {
        boolean matchFound = false;
        int fmid = 0, rmid = 0;
        Matcher[] matches = super.matchSubstringInRecord(record.getSequence());
        if (matches[0] == null && matches[1] == null) {
            return;
        }
        int start, end;
        if (matches[0] == null) {
            start = 0;
        } else {
            start = matches[0].end();
            fmid = barcodeToNums.get(matches[0].group());
            if (!MIDParser.midCount.contains(fmid)) {
                return;
            }
            Integer count = midnumCounts.get(fmid);
            if (count == null) {
                count = 0;
            }
            midnumCounts.put(fmid, count + 1);
            matchFound = true;
        }
        if (matches[1] == null) {
            end = record.getLength() - 1;
        } else {
            int numGroups = matches[1].groupCount();
            end = matches[1].start(numGroups);
            if (matchFound == false) {
                rmid = barcodeToNums.get(matches[1].group(numGroups));
                if (!MIDParser.midCount.contains(rmid)) {
                    return;
                }
                Integer count = midnumCounts.get(rmid);
                if (count == null) {
                    count = 0;
                }
                midnumCounts.put(rmid, count + 1);
            }

        }
        // choose MID to add to record
        int mid;
        if (fmid == 0 && rmid == 0) {
            mid = 0;
        } else if (fmid == rmid) {
            mid = fmid;
        } else {
            if (fmid == 0) {
                mid = rmid;
            } else if (rmid == 0) {
                mid = fmid;
            } else {
                throw new RuntimeException("Forward and reverse barcodes do not have same MID for record \""
                        + record.getHeader() + "\": (" + fmid + " / " + rmid + ")");
            }
        }
        record.setSequence(start, end);
        record.setMID(mid);
        if (!outhandles.containsKey(mid)) {
            String outfile = outprefix + "." + mid + ".fq";
            outfiles.put(mid, outfile);
            outhandles.put(mid, new FastqWriter(outfile, gzipOutput));
        }
        outhandles.get(mid).writeToFastq(record);
    }

    public void parseRecords(List<SGSRecord> records) throws IOException {
        for (SGSRecord record : records) {
            parseRecord(record);
        }
    }

    @Override
    public void printOutputStats() {
        if (midnumCounts.isEmpty()) {
            System.out.println("[INFO]: No matches found for parsed MIDs");
        } else {
            System.out.println("[INFO]: MID numbers for \"" + infile + "\":");
            for (int i : midnumCounts.keySet()) {
                System.out.println("\t" + i + ": " + midnumCounts.get(i));
            }
        }
    }

    private void closeFilehandles() throws IOException {
        reader.close();
        for (int i : outhandles.keySet()) {
            outhandles.get(i).close();
        }
    }

    @Override
    public Map<Integer, String> getOutfiles() {
        return outfiles;
    }
}