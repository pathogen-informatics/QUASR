package uk.ac.sanger.quasr.parsers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import uk.ac.sanger.quasr.records.SFFRecord;
import uk.ac.sanger.quasr.records.SGSRecord;

/**
 *
 * @author sw10
 */
public class SFFParser {

    private String infile;
    private DataInputStream infh;
    private static int FILEPOSITION = 0;
    private short keyLength, flowsPerRead, commonHeaderLength;
    private int readNumbers, counter, numBases;

    public SFFParser(String infile) throws IOException {
        this.infile = infile;
        openFile();
        parseCommonHeader();
        System.out.println("[INFO]: Successfully opened \"" + infile + "\"");
        counter = 0;
    }

    private void openFile() throws IOException {
        infh = new DataInputStream(new BufferedInputStream(new FileInputStream(infile)));
    }

    public SGSRecord getNextRecord() throws IOException {
        short name_length;
        String header, sequence;
        byte[] qualities;
        if (counter == readNumbers) {
            return null;
        }
        // read header
        getHeaderLength();
        name_length = getNameLength();
        getNumberBases();
        getClipQualLeft();
        getClipQualRight();
        getClipAdapterLeft();
        getClipAdapterRight();
        header = getReadName(name_length);
        addPadding();

        // read data
        getFlowgramValues();
        getFlowIndexPerBase();
        sequence = getBases();
        qualities = getQualityScores();
        addPadding();
        counter++;
        return new SFFRecord(header, sequence, qualities);
    }

    private void parseCommonHeader() throws IOException {
        String keySequence;
        byte[] version;

        confirmMagicNumber();
        version = readVersion();
        //SFFRecord.setVersion(version);
        readIndexOffset();
        readIndexLength();
        getReadNumbers();
        getHeaderLength();
        getKeyLength();
        getFlowsPerRead();
        getFlowgramFormatCode();
        getFlowCharacters();
        keySequence = getKeySequence();
        SFFRecord.setKey(keySequence);
        addPadding();
        assert (FILEPOSITION == commonHeaderLength);
    }

    private void confirmMagicNumber() throws IOException, RuntimeException {
        int magic = infh.readInt();
        if (magic != 779314790) {
            throw new RuntimeException("Magic number does not match that of an SFF file");
        }
        FILEPOSITION += 4;
    }

    private byte[] readVersion() throws IOException {
        byte[] chars = new byte[4];
        infh.read(chars);
        FILEPOSITION += 4 * 1;
        return chars;
    }

    private void readIndexOffset() throws IOException {
        infh.readLong();
        FILEPOSITION += 8;
    }

    private void readIndexLength() throws IOException {
        infh.readInt();
        FILEPOSITION += 4;
    }

    private void getReadNumbers() throws IOException {
        readNumbers = infh.readInt();
        FILEPOSITION += 4;
    }

    private void getHeaderLength() throws IOException {
        commonHeaderLength = infh.readShort();
        FILEPOSITION += 2;
    }

    private void getKeyLength() throws IOException {
        keyLength = infh.readShort();
        FILEPOSITION += 2;
    }

    private void getFlowsPerRead() throws IOException {
        flowsPerRead = infh.readShort();
        FILEPOSITION += 2;
    }

    private void getFlowgramFormatCode() throws IOException {
        infh.readByte();
        FILEPOSITION += 1;
    }

    private void getFlowCharacters() throws IOException {
        byte[] bytes = new byte[flowsPerRead];
        infh.read(bytes);
        FILEPOSITION += 1 * flowsPerRead;
    }

    private String getKeySequence() throws IOException {
        byte[] bytes = new byte[keyLength];
        infh.read(bytes);
        FILEPOSITION += 1 * keyLength;
        return new String(bytes, "ASCII");
    }

    private void addPadding() throws IOException {
        int remainder = FILEPOSITION % 8;
        if (remainder != 0) {
            int padding = 8 - (remainder);
            infh.skipBytes(padding); // padding to next 8-byte boundary
            FILEPOSITION += padding;
        }
    }

    private short getNameLength() throws IOException {
        short nameLength = infh.readShort();
        FILEPOSITION += 2;
        return nameLength;
    }

    private void getNumberBases() throws IOException {
        numBases = infh.readInt();
        FILEPOSITION += 4;
    }

    private short getClipQualLeft() throws IOException {
        short cQL = infh.readShort();
        FILEPOSITION += 2;
        return cQL;
    }

    private short getClipQualRight() throws IOException {
        short cQR = infh.readShort();
        FILEPOSITION += 2;
        return cQR;
    }

    private short getClipAdapterLeft() throws IOException {
        short cAL = infh.readShort();
        FILEPOSITION += 2;
        return cAL;
    }

    private short getClipAdapterRight() throws IOException {
        short cAR = infh.readShort();
        FILEPOSITION += 2;
        return cAR;
    }

    private String getReadName(int size) throws IOException {
        byte[] bytes = new byte[size];
        infh.read(bytes);
        FILEPOSITION += 1 * size;
        String seq = new String(bytes, "ASCII");
        return seq;
    }

    private void getFlowgramValues() throws IOException {
        short[] shorts = new short[flowsPerRead];
        for (int i = 0; i < flowsPerRead; i++) {
            shorts[i] = infh.readShort();
        }
        FILEPOSITION += 2 * flowsPerRead;
    }

    private void getFlowIndexPerBase() throws IOException {
        byte[] bytes = new byte[numBases];
        infh.read(bytes);
        FILEPOSITION += 1 * numBases;
    }

    private String getBases() throws IOException {
        byte[] bytes = new byte[numBases];
        infh.read(bytes);
        FILEPOSITION += 1 * numBases;
        String seq = new String(bytes, "ASCII");
        return seq;
    }

    private byte[] getQualityScores() throws IOException {
        byte[] bytes = new byte[numBases];
        infh.read(bytes);
        FILEPOSITION += 1 * numBases;
        return bytes;
    }

    public void close() throws IOException {
        infh.close();
    }

    public void reopen() throws IOException {
        close();
        openFile();
        parseCommonHeader();
        counter = 0;
    }
}
