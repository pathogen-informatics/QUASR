package uk.ac.sanger.quasr.demultiplexers;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author sw10
 */
public interface Demultiplexer {
    public abstract void performDemultiplexing(int numParsed) throws IOException;
    public abstract void printOutputStats();
    public abstract Map<Integer, String> getOutfiles();
}
