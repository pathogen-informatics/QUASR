package uk.ac.sanger.quasr;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author sw10
 */
public interface ProcessingInterface {
    public abstract void runDuplicateRemoval() throws IOException, NoSuchAlgorithmException;
}
