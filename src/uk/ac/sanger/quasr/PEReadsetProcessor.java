package uk.ac.sanger.quasr;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import uk.ac.sanger.quasr.duplicates.DuplicateRemover;
import uk.ac.sanger.quasr.duplicates.PEDuplicateRemover;
import uk.ac.sanger.quasr.excision.PrimerRemover;
import uk.ac.sanger.quasr.invariables.Invariables;
import uk.ac.sanger.quasr.qa.QAGrapher;
import uk.ac.sanger.quasr.qc.QualityController;
import uk.ac.sanger.quasr.readers.FastqReader;
import uk.ac.sanger.quasr.readers.SFFReader;
import uk.ac.sanger.quasr.readers.SGSReader;
import uk.ac.sanger.quasr.records.SGSRecord;
import uk.ac.sanger.quasr.writers.FastqWriter;

/**
 * PE readset parse does the following:
 * duplicate removal
 * primer removal
 * quality control
 * quality assurance
 * 
 * NB: NO DEMULTIPLEXING!
 * @author sw10
 */
public class PEReadsetProcessor {

    private String infileF;
    private String infileR = null;
    private String outprefix;
    private boolean compress = false;
    private String primerfile;
    private int primerLeeway = 50;
    private boolean pr = false, qc = false, qa = false;
    private float medianCutoff = 25.0F;
    private int lenCutoff = 50;
    private int windowLen = 50;
    private PrimerRemover primerRemover = null;
    private QualityController qualityController = null;
    private QAGrapher qaGrapher = null;
    private int numParse = 100;
    private boolean writeOutput = false;
    private String rPath = "/software/bin/R-dev";

    public PEReadsetProcessor(String infileF, String infileR, String outprefix, boolean compressOutput) {
        this.infileF = infileF;
        this.infileR = infileR;
        this.outprefix = outprefix;
        this.compress = compressOutput;
    }

    public PEReadsetProcessor(String infile, String outprefix, boolean compressOutput) {
        this.infileF = infile;
        this.outprefix = outprefix;
        this.compress = compressOutput;
    }

    public void runDuplicateRemoval() throws IOException, NoSuchAlgorithmException {
        DuplicateRemover dr;
        if (infileR == null) {
            dr = new PEDuplicateRemover(infileF, outprefix, compress);
        } else {
            dr = new PEDuplicateRemover(infileF, infileR, outprefix, compress);
        }
        dr.performDuplicateRemoval();
        this.infileF = outprefix + Invariables.GENERAL_SUFFIXES.get("PE_duplicate_f") + Invariables.GENERAL_SUFFIXES.get("fastq");
        this.infileR = outprefix + Invariables.GENERAL_SUFFIXES.get("PE_duplicate_r") + Invariables.GENERAL_SUFFIXES.get("fastq");
        this.outprefix += Invariables.GENERAL_SUFFIXES.get("SE_duplicate");
    }

    public void addPrimerRemovalToPipeline(String primerfile, int primerLeeway) {
        this.primerfile = primerfile;
        this.primerLeeway = primerLeeway;
        pr = true;
        writeOutput = true;
    }

    public void addQualityControlToPipeline(float medianCutoff, int lenCutoff) {
        this.medianCutoff = medianCutoff;
        this.lenCutoff = lenCutoff;
        qc = true;
        writeOutput = true;
    }

    public void addQAGraphingToPipeline(int windowLen, String rPath) {
        this.windowLen = windowLen;
        this.rPath = rPath;
        qa = true;
    }

    public void runPipeline() throws IOException {
        if (writeOutput == false && qa == false) {
            return;
        } else {
            System.out.println("[INFO]: Executing pipeline");
        }
        SGSReader readerF, readerR;
        List<SGSRecord> recordsF, recordsR;
        FastqWriter outF = null, outR = null;
        if (infileF.endsWith(".sff")) {
            readerF = new SFFReader(infileF);
        } else {
            readerF = new FastqReader(infileF);
        }
        if (infileR.endsWith(".sff")) {
            readerR = new SFFReader(infileR);
        } else {
            readerR = new FastqReader(infileR);
        }
        if (writeOutput == true) {
            outprefix = outprefix + Invariables.GENERAL_SUFFIXES.get("processor");
            outF = new FastqWriter(outprefix + Invariables.GENERAL_SUFFIXES.get("forward") + Invariables.GENERAL_SUFFIXES.get("fastq"), compress);
            outR = new FastqWriter(outprefix + Invariables.GENERAL_SUFFIXES.get("reverse") + Invariables.GENERAL_SUFFIXES.get("fastq"), compress);
        }
        try {
            if (pr == true) {
                primerRemover = new PrimerRemover(primerfile, primerLeeway);
            }
            if (qc == true) {
                qualityController = new QualityController(lenCutoff, medianCutoff);
            }
            if (qa == true) {
                qaGrapher = new QAGrapher(windowLen, outprefix, rPath);
            }
            while (true) {
                recordsF = readerF.getRecords(numParse);
                recordsR = readerR.getRecords(numParse);
                assert recordsF.size() == recordsR.size();
                if (recordsF.isEmpty()) {
                    break;
                }
                for (int i = 0; i < recordsF.size(); i++) {
                    SGSRecord recordF = recordsF.get(i);
                    SGSRecord recordR = recordsR.get(i);
                    if (pr == true) {
                        primerRemover.parseRecord(recordF);
                        primerRemover.parseRecord(recordR);
                    }
                    if (qc == true) {
                        qualityController.parseRecords(recordF, recordR);
                    }
                    if (qa == true && recordF.getPassedQCFlag() == true && recordR.getPassedQCFlag() == true) {
                        qaGrapher.parseRecord(recordF);
                        qaGrapher.parseRecord(recordR);
                    }
                    if (writeOutput == true && recordF.getPassedQCFlag() == true && recordR.getPassedQCFlag() == true) {
                        outF.writeToFastq(recordF);
                        outR.writeToFastq(recordR);
                    }
                }
            }
        } finally {
            if (writeOutput == true) {
                outF.close();
                outR.close();
            }
        }
        outputStats();
    }

    private void outputStats() {
        if (pr == true) {
            primerRemover.printOutputStats();
        }
        if (qc == true) {
            qualityController.printOutputStats();
        }
        if (qa == true) {
            qaGrapher.printOutputStats();
        }
    }
}