package uk.ac.sanger.quasr.readers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.sanger.quasr.parsers.PileupParser;
import uk.ac.sanger.quasr.pileup.PileupLine;

/**
 *
 * @author sw10
 */
public class PileupReader {

    private PileupParser parser;
    private ArrayList<PileupLine> lines;

    public PileupReader(String infile) throws IOException {
        parser = new PileupParser(infile);
        lines = new ArrayList<PileupLine>();
    }
    
    public PileupReader(String infile, String reference) throws IOException {
        parser = new PileupParser(infile, reference);
        lines = new ArrayList<PileupLine>();
    }

    private void readNextLines(int num) throws IOException {
        lines.clear();
        for (int i = 0; i < num; i++) {
            PileupLine line = parser.getNextLine();
            if (line == null) {
                break;
            } else {
                lines.add(line);
            }
        }
    }

    public List<PileupLine> getLines(int num) throws IOException {
        readNextLines(num);
        return lines;
    }

    public void close() throws IOException {
        parser.close();
    }
}
