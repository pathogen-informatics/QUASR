package uk.ac.sanger.quasr.duplicates;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author sw10
 */
public interface DuplicateRemover {
    public abstract void performDuplicateRemoval() throws IOException, NoSuchAlgorithmException;
    public abstract String[] getOutfiles();
}
