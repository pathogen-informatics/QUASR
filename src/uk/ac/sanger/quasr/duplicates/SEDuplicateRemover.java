package uk.ac.sanger.quasr.duplicates;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.sanger.quasr.invariables.Invariables;
import uk.ac.sanger.quasr.records.SGSRecord;
import uk.ac.sanger.quasr.readers.FastqReader;
import uk.ac.sanger.quasr.readers.SFFReader;
import uk.ac.sanger.quasr.readers.SGSReader;
import uk.ac.sanger.quasr.writers.FastqWriter;

/**
 *
 * @author sw10
 */
public class SEDuplicateRemover implements DuplicateRemover {

    private SGSReader reader;
    private FastqWriter out;
    private Map<String, int[]> uniques;
    private List<SGSRecord> records;
    private String forOutfile;
    private int inNum, outNum;

    public SEDuplicateRemover(String infile, String outprefix, boolean gzipOutput) throws IOException {
        if (infile.endsWith(".sff") || infile.endsWith(".SFF")) {
            reader = new SFFReader(infile);
        } else {
            reader = new FastqReader(infile);
        }
        forOutfile = outprefix + Invariables.GENERAL_SUFFIXES.get("SE_duplicate") + Invariables.GENERAL_SUFFIXES.get("fastq"); 
        out = new FastqWriter(forOutfile, gzipOutput);
    }

    @Override
    public void performDuplicateRemoval() throws IOException, NoSuchAlgorithmException {
        uniques = new HashMap<String, int[]>();
        hashSequences();
        writeUniquesToFile();
        closeFilehandles();
        printOutputStats();
    }

    private void hashSequences() throws IOException, NoSuchAlgorithmException {
        int inputCounter = 0;
        do {
            //reader.readNextRecords(50);
            records = reader.getRecords(50);
            if (records.isEmpty()) {
                break;
            }
            for (int i = 0; i < records.size(); i++) {
                SGSRecord record = records.get(i);
                String sequenceHash = generateMD5Hash(record.getSequence());
                int headerHash = record.getHeader().hashCode();
                int median = record.calcRoundedMedianQualityScore();
                int[] metaInfo = {headerHash, median};
                if (!uniques.containsKey(sequenceHash)) {
                    uniques.put(sequenceHash, metaInfo);
                } else {
                    if (median > uniques.get(sequenceHash)[1]) {
                        uniques.put(sequenceHash, metaInfo);
                    }
                }
                inputCounter++;
            }
        } while (true);
        inNum = inputCounter;
    }

    private String generateMD5Hash(String sequence) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(sequence.getBytes("UTF-8"));
        BigInteger number = new BigInteger(1, messageDigest);
        return number.toString(16);
    }
    
    private void writeUniquesToFile() throws IOException, NoSuchAlgorithmException {
        reader.reopen();
        int outputCounter = 0;
        do {
            //reader.readNextRecords(50);
            records = reader.getRecords(50);
            if (records.isEmpty()) {
                break;
            }
            for (int i=0; i<records.size(); i++) {
                SGSRecord record = records.get(i);
                String sequenceHash = generateMD5Hash(record.getSequence());
                int headerHash = record.getHeader().hashCode();
                if (uniques.get(sequenceHash)[0] == headerHash) {
                    out.writeToFastq(record);
                    outputCounter++;
                }
            }
        } while (true);
        outNum = outputCounter;
        
    }
    
    private void printOutputStats() {
        float perc_retained = (outNum / (float) inNum) * 100;
        System.out.println("[INFO]: Duplicate removal statistics:");
        System.out.println("\tReads read: " + inNum);
        System.out.printf("\tUnique reads retained: %d (%.2f%%)\n", outNum, perc_retained);
    }
    
    @Override
    public String[] getOutfiles() {
        return new String[]{forOutfile};
    }
    
    private void closeFilehandles() throws IOException {
        reader.close();
        out.close();
    }
}