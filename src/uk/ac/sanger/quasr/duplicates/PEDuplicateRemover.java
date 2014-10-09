package uk.ac.sanger.quasr.duplicates;

import uk.ac.sanger.quasr.invariables.Invariables;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.sanger.quasr.records.SGSRecord;
import uk.ac.sanger.quasr.modules.Stats;
import uk.ac.sanger.quasr.readers.PEFastqReader;
import uk.ac.sanger.quasr.readers.SeparatePEFastqReader;
import uk.ac.sanger.quasr.readers.SinglePEFastqReader;
import uk.ac.sanger.quasr.writers.FastqWriter;

/**
 *
 * @author sw10
 */
public class PEDuplicateRemover implements DuplicateRemover {
    
    private PEFastqReader reader;
    private List<SGSRecord> forRecords, revRecords;
    private Map<String, int[]> uniques;
    private FastqWriter forOut, revOut;
    private String forOutfile, revOutfile;
    private int inNum, outNum;
    
    public PEDuplicateRemover(String infile, String outprefix, boolean gzipOutput) throws IOException {
        reader = new SinglePEFastqReader(infile);
        setupOutputHandles(outprefix, gzipOutput);
    }
    
    public PEDuplicateRemover(String forFile, String revFile, String outprefix, boolean gzipOutput) throws IOException {
        reader = new SeparatePEFastqReader(forFile, revFile);
        setupOutputHandles(outprefix, gzipOutput);
    }
    
    private void setupOutputHandles(String outprefix, boolean gzipOutput) throws IOException {
        forOutfile = outprefix + Invariables.GENERAL_SUFFIXES.get("PE_duplicate_f") + Invariables.GENERAL_SUFFIXES.get("fastq");
        revOutfile = outprefix + Invariables.GENERAL_SUFFIXES.get("PE_duplicate_r") + Invariables.GENERAL_SUFFIXES.get("fastq");
        forOut = new FastqWriter(forOutfile, gzipOutput);
        revOut = new FastqWriter(revOutfile, gzipOutput);
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
            reader.readNextRecords(50);
            forRecords = reader.getForwardRecords();
            revRecords = reader.getReverseRecords();
            if (forRecords.isEmpty()) {
                break;
            }
            for (int i=0; i<forRecords.size(); i++) {
                SGSRecord forRecord = forRecords.get(i);
                SGSRecord revRecord = revRecords.get(i);
                String sequence = forRecord.getSequence() + revRecord.getSequence();
                String header = forRecord.getHeader() + revRecord.getHeader();
                String sequenceHash = generateMD5Hash(sequence);
                int headerHash = header.hashCode();
                String qualities = forRecord.getQuality() + revRecord.getQuality();
                int median = Stats.calculateRoundedMedian(Invariables.convertASCIIToPhred(qualities));
                int[] metaInfo = {headerHash, median};
                if (! uniques.containsKey(sequenceHash)) {
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
            reader.readNextRecords(50);
            forRecords = reader.getForwardRecords();
            revRecords = reader.getReverseRecords();
            if (forRecords.isEmpty()) {
                break;
            }
            
            for (int i=0; i<forRecords.size(); i++) {
                SGSRecord forRecord = forRecords.get(i);
                SGSRecord revRecord = revRecords.get(i);
                String sequence = forRecord.getSequence() + revRecord.getSequence();
                String sequenceHash = generateMD5Hash(sequence);
                String header = forRecord.getHeader() + revRecord.getHeader();
                int headerHash = header.hashCode();
                if (uniques.get(sequenceHash)[0] == headerHash) {
                    forOut.writeToFastq(forRecord);
                    revOut.writeToFastq(revRecord);
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
        return new String[]{forOutfile, revOutfile};
    }
    
    private void closeFilehandles() throws IOException {
        reader.close();
        forOut.close();
        revOut.close();
    }
}
