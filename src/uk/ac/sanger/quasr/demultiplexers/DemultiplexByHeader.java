package uk.ac.sanger.quasr.demultiplexers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class DemultiplexByHeader implements Demultiplexer {

    private SGSReader reader;
    private Pattern headerPattern;
    private Map<Integer, String> outfiles;
    private Map<Integer, FastqWriter> outhandles;
    private Map<Integer, Integer> midnumCounts;
    private boolean gzipOutput;
    private String outprefix;
    String infile;

    public DemultiplexByHeader(String infile, String outprefix, boolean gzipOutput, String mids) throws IOException {
        this(infile, outprefix, gzipOutput, mids, "#(\\d+)/\\d$");
    }

    public DemultiplexByHeader(String infile, String outprefix, boolean gzipOutput, String mids, String pattern) throws IOException {
        if (infile.endsWith(".sff") || infile.endsWith(".SFF")) {
            reader = new SFFReader(infile);
        } else {
            reader = new FastqReader(infile);
        }
        this.infile = infile;
        this.gzipOutput = gzipOutput;
        MIDParser.parseMIDNums(mids);
        this.outprefix = outprefix;
        outfiles = new HashMap<Integer, String>();
        outhandles = new HashMap<Integer, FastqWriter>();
        midnumCounts = new HashMap<Integer, Integer>();
        headerPattern = Pattern.compile(pattern);
    }

    private void closeFilehandles() throws IOException {
        reader.close();
        for (int i : outhandles.keySet()) {
            outhandles.get(i).close();
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

    private void parseRecord(SGSRecord record) throws IOException {
        Matcher match = headerPattern.matcher(record.getHeader());
        if (match.find()) {
            int mid = Integer.parseInt(match.group(1));
            if (MIDParser.midCount.contains(mid)) {
                Integer count = midnumCounts.get(mid);
                if (count == null) {
                    count = 0;
                }
                midnumCounts.put(mid, count + 1);
                record.setMID(mid);
                if (!outhandles.containsKey(mid)) {
                    String outfile = outprefix + "." + mid + ".fq";
                    outfiles.put(mid, outfile);
                    outhandles.put(mid, new FastqWriter(outfile, gzipOutput));
                }
                outhandles.get(mid).writeToFastq(record);
            }
        }
    }

    private void parseRecords(List<SGSRecord> records) throws IOException {
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
    
    @Override
    public Map<Integer, String> getOutfiles() {
        return outfiles;
    }
}
