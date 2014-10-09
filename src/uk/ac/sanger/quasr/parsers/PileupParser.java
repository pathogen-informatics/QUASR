/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.sanger.quasr.parsers;

import uk.ac.sanger.quasr.records.FastaRecord;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import uk.ac.sanger.quasr.pileup.PileupLine;
import uk.ac.sanger.quasr.readers.FastaReader;

/**
 *
 * @author sw10
 */
public class PileupParser {

    private BufferedReader binfh;
    private int len = -1;
    private int currentPos = 0;
    //private String curretSeg = "";
    private Map<String, Integer> referenceSize = null;
    private String refName = "";
    private int refPos = 1;
    String line = "";

    public PileupParser(String infile) throws IOException {
        parseInfile(infile);
    }

    public PileupParser(String infile, String reference) throws IOException {
        parseInfile(infile);
        parseReferenceFile(reference);
    }

    private void parseInfile(String infile) throws IOException {
        if (infile.endsWith(".gz") || (infile.endsWith(".gzip"))) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(infile));
            binfh = new BufferedReader(new InputStreamReader(gzip));
        } else {
            binfh = new BufferedReader(new FileReader(infile));
        }
        System.out.println("[INFO]: Successfully opened \"" + infile + "\"");
    }

    public PileupLine getNextLine() throws IOException {
        if ("".equals(line)) {
            do {
                line = binfh.readLine();
            } while ("".equals(line));
        }
        currentPos++;
        // handle end of file missing bases
        if (line == null) {
            if (referenceSize != null) {
                Integer size = referenceSize.get(refName);
                if (size == null) {
                    throw new RuntimeException("No reference sequence found for \"" + refName + "\"");
                } else {
                    if (currentPos < size) {
                        return new PileupLine(refName, currentPos, '-', 0, "", "");
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        }

        String[] splitLine = null;
        while (true) {
            splitLine = line.split("\\s+");
            // After a position with deletions, get a line with 3rd and 4th columns as '*'. Unimportant
            if (splitLine[2].equals("*")) {
                line = binfh.readLine();
            } else {
                break;
            }
        }

        if (len == -1) {
            // Get number of columns for working out what each column means
            len = splitLine.length;
        }

        // If a new segment is encountered (not the first segment)
        if ((!splitLine[0].equals(refName)) && (!refName.equals(""))) {
            if (referenceSize != null) {
                Integer size = referenceSize.get(refName);
                if (size == null) {
                    throw new RuntimeException("No reference sequence found for \"" + refName + "\"");
                } else {
                    while (currentPos < size) {
                        return new PileupLine(refName, currentPos, '-', 0, "", "");
                    }
                }
            }
            currentPos = 1;
        }
        refName = splitLine[0];
        refPos = Integer.parseInt(splitLine[1]);

        // handle missing lines in file
        if (currentPos < refPos) {
            return new PileupLine(refName, currentPos, '-', 0, "", "");
        }

        line = binfh.readLine(); // ready for next position
        switch (len) {
            case 11:
            // tba
            case 10:
                return new PileupLine(splitLine[0], Integer.parseInt(splitLine[1]),
                        splitLine[2].charAt(0), Integer.parseInt(splitLine[7]), splitLine[8], splitLine[9]);
            case 7:
            // tba
            case 6: // refName, refBase, readDepth, readBases, readQuals
                return new PileupLine(splitLine[0], Integer.parseInt(splitLine[1]),
                        splitLine[2].charAt(0), Integer.parseInt(splitLine[3]), splitLine[4], splitLine[5]);
            default:
                throw new RuntimeException("Pileup format unsupported");
        }
    }

    private void parseReferenceFile(String refFile) throws IOException {
        referenceSize = new HashMap<String, Integer>();
        FastaReader faReader = new FastaReader(refFile);
        for (FastaRecord faRec : faReader.getRecords(20)) {
            referenceSize.put(faRec.getHeader(), faRec.getLength());
        }
        faReader.close();
        System.out.println("[INFO]: Successfully opened \"" + refFile + "\"");
    }

    public void close() throws IOException {
        binfh.close();
    }
}
