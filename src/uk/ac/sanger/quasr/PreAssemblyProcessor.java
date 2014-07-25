package uk.ac.sanger.quasr;

import uk.ac.sanger.quasr.excision.PrimerRemover;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import uk.ac.sanger.quasr.qc.QualityController;
import uk.ac.sanger.quasr.readers.FastqReader;
import uk.ac.sanger.quasr.readers.PEFastqReader;
import uk.ac.sanger.quasr.readers.SFFReader;
import uk.ac.sanger.quasr.readers.SGSReader;
import uk.ac.sanger.quasr.readers.SeparatePEFastqReader;
import uk.ac.sanger.quasr.readers.SinglePEFastqReader;
import uk.ac.sanger.quasr.records.SGSRecord;
import uk.ac.sanger.quasr.writers.FastqWriter;

/**
 *
 * @author sw10
 */
public class PreAssemblyProcessor {

    String outprefix;
    PEFastqReader peReader = null;
    SGSReader seReader = null;
    boolean isFastq = true;
    List<PreAssemblyInterface> workflow = new ArrayList<PreAssemblyInterface>();
    PreAssemblyInterface demultiplex = null;
    FastqWriter fOut, rOut = null;

    public PreAssemblyProcessor(String infile, String outprefix, boolean gzipOutput, boolean isPairedEnd) throws IOException {
        isFastq = checkFormat(infile);
        this.outprefix = outprefix;
        if (isPairedEnd == true) {
            if (isFastq == true) {
                peReader = new SinglePEFastqReader(infile);
            } else {
                throw new RuntimeException("Unable to parse PE SFF files");
            }
            fOut = new FastqWriter(outprefix + ".f.fq", gzipOutput);
            rOut = new FastqWriter(outprefix + ".r.fq", gzipOutput);
        } else {
            if (isFastq == true) {
                seReader = new FastqReader(infile);
            } else {
                seReader = new SFFReader(infile);
            }
            fOut = new FastqWriter(outprefix + ".qc.fq", gzipOutput);
        }
    }

    public PreAssemblyProcessor(String infile, String matefile, String outprefix, boolean gzipOutput) throws IOException {
        isFastq = checkFormat(infile);
        if (checkFormat(matefile) != isFastq) {
            throw new RuntimeException("Forward and reverse read files are in different formats");
        }
        if (isFastq == true) {
            peReader = new SeparatePEFastqReader(infile, matefile);
        } else {
            throw new RuntimeException("Unable to parse PE SFF files");
        }
        fOut = new FastqWriter(outprefix + ".f.fq", gzipOutput);
        rOut = new FastqWriter(outprefix + ".r.fq", gzipOutput);
    }

    private boolean checkFormat(String infile) {
        if (infile.endsWith(".sff") || infile.endsWith(".SFF")) {
            return false;
        } else if (infile.endsWith(".fa") || infile.endsWith(".fasta") || infile.endsWith(".fas")) {
            throw new RuntimeException("FASTA files cannot be parsed");
        } else {
            return true;
        }
    }

    public void addQCToWorkflow(int length, float median) {
        workflow.add(new QualityController(length, median));
    }

    public void addPrimerRemovaltoWorkflow(String primerFile, int leeway) throws IOException {
        workflow.add(new PrimerRemover(primerFile, leeway));
    }

    public void executeWorkflow(int numParsed) throws IOException {
        if (peReader != null) {
            List<SGSRecord> fRecords, rRecords;
            SGSRecord fRecord, rRecord;
            try {
                do {
                    peReader.readNextRecords(numParsed);
                    fRecords = peReader.getForwardRecords();
                    rRecords = peReader.getReverseRecords();
                    if (fRecords.isEmpty()) {
                        break;
                    }
                    for (int i = 0; i < fRecords.size(); i++) {
                        fRecord = fRecords.get(i);
                        rRecord = rRecords.get(i);
                        for (PreAssemblyInterface pai : workflow) {
                            pai.parseRecords(fRecord, rRecord);
                        }
                        if (fRecord.getPassedQCFlag() == true && rRecord.getPassedQCFlag() == true) {
                            fOut.writeToFastq(fRecord);
                            rOut.writeToFastq(rRecord);
                        }
                    }
                } while (true);
            } finally {
                peReader.close();
                fOut.close();
                rOut.close();
                for (PreAssemblyInterface pai : workflow) {
                    pai.printOutputStats();
                }
            }
        } else if (seReader != null) {
            List<SGSRecord> records;
            try {
                do {
                    //seReader.readNextRecords(numParsed);
                    records = seReader.getRecords(numParsed);
                    if (records.isEmpty()) {
                        break;
                    }
                    for (SGSRecord record : records) {
                        for (PreAssemblyInterface pai : workflow) {
                            pai.parseRecord(record);
                        }
                        if (record.getPassedQCFlag() == true) {
                            fOut.writeToFastq(record);
                        }
                    }
                } while (true);
            } finally {
                seReader.close();
                fOut.close();
                for (PreAssemblyInterface pai : workflow) {
                    pai.printOutputStats();
                }
            }
        }
    }
}
