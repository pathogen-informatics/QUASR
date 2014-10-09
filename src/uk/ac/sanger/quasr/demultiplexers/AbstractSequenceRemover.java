package uk.ac.sanger.quasr.demultiplexers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.sanger.quasr.invariables.Invariables;

/**
 *
 * @author sw10
 */
public abstract class AbstractSequenceRemover {
    protected String substringFile;
    protected List<Pattern> fSubstrings = new ArrayList<Pattern>();
    protected List<Pattern> rSubstrings = new ArrayList<Pattern>();
    protected static final Pattern separator = Pattern.compile("[,\\s]+");
    private int leeway;
    
    public AbstractSequenceRemover(String substringFile, int leeway) {
        this.substringFile = substringFile;
        this.leeway = leeway;
    }
    
    public AbstractSequenceRemover(int leeway) {
        this.leeway = leeway;
    }
    
    protected Pattern convertStringToPattern(String in) {
        if (in.isEmpty()) {
            return null;
        }
        for (String code : Invariables.AMBIGUITYCODES.keySet()) {
            in = in.replaceAll(Invariables.AMBIGUITYCODES.get(code).toString(), "[" + code + "]");
        }
        return Pattern.compile(in, Pattern.CASE_INSENSITIVE);
    }
    
    protected Matcher[] matchSubstringInRecord(String seq) {
        Matcher[] matches = new Matcher[2];
        //boolean matchFound = false;
        for (int i=0; i<fSubstrings.size(); i++) {
            Pattern fPat = fSubstrings.get(i);
            Pattern rPat = rSubstrings.get(i);
            if (fPat != null) {
                Matcher fMatch = fPat.matcher(seq);
                if (fMatch.find() && (fMatch.start()+1) <= leeway) {
                    if (matches[0] != null) {
                        if (fMatch.start() > matches[0].start()) {
                            matches[0] = fMatch;
                            //matchFound = true;
                        }
                    }
                    else {
                        matches[0] = fMatch;
                        //matchFound = true;
                    }
                }
            }
            if (rPat != null) {
                Matcher rMatch = rPat.matcher(seq);
                if (rMatch.find()) {
                    int numGroups = rMatch.groupCount();
                    if (seq.length() - (rMatch.end(numGroups)+1) <= leeway) {
                        if (matches[1] != null) {
                            if (rMatch.start() > matches[1].start()) {
                                matches[1] = rMatch;
                                //matchFound = true;
                            }
                        }
                        else {
                            matches[1] = rMatch;
                            //matchFound = true;
                        }
                    }
                }
            }
            /**
            if (matchFound == true) {
                break;
            }
            */
        }
        return matches;
    }
}
