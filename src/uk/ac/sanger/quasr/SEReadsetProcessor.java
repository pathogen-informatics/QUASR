package uk.ac.sanger.quasr;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.sanger.quasr.demultiplexers.DemultiplexByHeader;
import uk.ac.sanger.quasr.demultiplexers.DemultiplexBySequence;
import uk.ac.sanger.quasr.demultiplexers.Demultiplexer;
import uk.ac.sanger.quasr.duplicates.DuplicateRemover;
import uk.ac.sanger.quasr.duplicates.SEDuplicateRemover;
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
 * @author sw10
 */
public class SEReadsetProcessor {

    private String infile, outprefix;
    private String primerfile;
    private int primerLeeway = 50;
    private float medianCutoff = 25.0F;
    private int lenCutoff = 50;
    private int windowLen = 50;
    private boolean compress = false;
    private boolean writeOutput = false;
    private Demultiplexer demultiplexer = null;
    private PrimerRemover primerRemover = null;
    private QualityController qualityController = null;
    private QAGrapher qaGrapher = null;
    private boolean demultiplex = false, pr = false, qa = false, qc = false;
    private int numParse = 100;
    private String rPath = "/software/bin/R-dev";

    public SEReadsetProcessor(String infile, String outprefix, boolean compressOutputs) throws IOException {
        this.infile = infile;
        this.outprefix = outprefix;
        this.compress = compressOutputs;
    }

    public void runDuplicateRemoval() throws IOException, NoSuchAlgorithmException {
        System.out.println("[INFO]: Performing duplicate removal");
        DuplicateRemover dr = new SEDuplicateRemover(infile, outprefix, compress);
        dr.performDuplicateRemoval();
        this.infile = outprefix + Invariables.GENERAL_SUFFIXES.get("SE_duplicate") + Invariables.GENERAL_SUFFIXES.get("fastq");
        this.outprefix += Invariables.GENERAL_SUFFIXES.get("SE_duplicate");
    }

    public void runDemultiplexByHeader(String pattern, String mids) throws IOException {
        System.out.println("[INFO]: Performing demultiplexing");
        if (pattern == null) {
            demultiplexer = new DemultiplexByHeader(infile, outprefix, compress, mids);
        } else {
            demultiplexer = new DemultiplexByHeader(infile, outprefix, compress, mids, pattern);
        }
        demultiplexer.performDemultiplexing(100);
        demultiplex = true;
    }

    public void runDemultiplexBySequence(String customfile, String mids, int leeway) throws IOException {
        System.out.println("[INFO]: Performing demultiplexing");
        demultiplexer = new DemultiplexBySequence(infile, outprefix, customfile, compress, mids, leeway);
        demultiplexer.performDemultiplexing(100);
        demultiplex = true;
    }

    public void runDemultiplexBySequence(String mids, int leeway) throws IOException {
        System.out.println("[INFO]: Performing demultiplexing");
        demultiplexer = new DemultiplexBySequence(infile, outprefix, compress, mids, leeway);
        demultiplexer.performDemultiplexing(100);
        demultiplex = true;
    }

    public void addPrimerRemovalToPipeline(String primerfile, int primerLeeway) throws IOException {
        System.out.println("[INFO]: Adding primer removal to pipeline");
        this.primerfile = primerfile;
        this.primerLeeway = primerLeeway;
        pr = true;
        writeOutput = true;
    }

    public void addQualityControlToPipeline(float medianCutoff, int lenCutoff) {
        System.out.println("[INFO]: Adding quality control to pipeline");
        this.medianCutoff = medianCutoff;
        this.lenCutoff = lenCutoff;
        qc = true;
        writeOutput = true;
    }

    public void addQAGraphingToPipeline(int windowLen, String rPath) throws IOException {
        System.out.println("[INFO]: Adding quality assurance graphing to pipeline");
        Properties prop = new Properties();
        prop.load(SEReadsetProcessor.class.getResourceAsStream("quasr.properties"));
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
        SGSReader reader;
        List<SGSRecord> records;
        FastqWriter out = null;
        if (demultiplex == true) {
            Map<Integer, String> infiles = demultiplexer.getOutfiles();
            for (int mid : infiles.keySet()) {
                infile = infiles.get(mid);
                String midOutprefix = outprefix + "." + mid + Invariables.GENERAL_SUFFIXES.get("processor");
                reader = new FastqReader(infile);
                if (writeOutput == true) {
                    out = new FastqWriter(midOutprefix + Invariables.GENERAL_SUFFIXES.get("fastq"), compress);
                }
                try {
                    if (pr == true) {
                        primerRemover = new PrimerRemover(primerfile, primerLeeway);
                    }
                    if (qc == true) {
                        qualityController = new QualityController(lenCutoff, medianCutoff);
                    }
                    if (qa == true) {
                        qaGrapher = new QAGrapher(windowLen, midOutprefix, rPath);
                    }
                    while (true) {
                        records = reader.getRecords(1000);
                        if (records.isEmpty()) {
                            break;
                        }
                        for (SGSRecord record : records) {
                            if (pr == true) {
                                primerRemover.parseRecord(record);
                            }
                            if (qc == true) {
                                qualityController.parseRecord(record);
                            }
                            if (qa == true && record.getPassedQCFlag() == true) {
                                qaGrapher.parseRecord(record);
                            }
                            if (writeOutput == true && record.getPassedQCFlag() == true) {
                                out.writeToFastq(record);
                            }
                        }
                    }
                } finally {
                    if (writeOutput == true) {
                        out.close();
                    }
                }
                outputStats();
            }
        } else {
            if (infile.endsWith(".sff")) {
                reader = new SFFReader(infile);
            } else {
                reader = new FastqReader(infile);
            }
            if (writeOutput == true) {
                outprefix = outprefix + Invariables.GENERAL_SUFFIXES.get("processor");
                out = new FastqWriter(outprefix + Invariables.GENERAL_SUFFIXES.get("fastq"), compress);
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
                    records = reader.getRecords(1000);
                    if (records.isEmpty()) {
                        break;
                    }
                    for (SGSRecord record : records) {
                        if (pr == true) {
                            primerRemover.parseRecord(record);
                        }
                        if (qc == true) {
                            qualityController.parseRecord(record);
                        }
                        if (qa == true && record.getPassedQCFlag() == true) {
                            qaGrapher.parseRecord(record);
                        }
                        if (writeOutput == true && record.getPassedQCFlag() == true) {
                            out.writeToFastq(record);
                        }
                    }
                }
            } finally {
                if (writeOutput == true) {
                    out.close();
                }
            }
            outputStats();
        }
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
