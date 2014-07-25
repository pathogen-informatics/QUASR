package uk.ac.sanger.quasr.qa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.sanger.quasr.PreAssemblyInterface;
import uk.ac.sanger.quasr.invariables.Invariables;
import uk.ac.sanger.quasr.readers.SFFReader;
import uk.ac.sanger.quasr.readers.SGSReader;
import uk.ac.sanger.quasr.records.SGSRecord;

/**
 *
 * @author sw10
 */
public class QAGrapher implements PreAssemblyInterface {

    private int windowLen;
    private BufferedWriter rCommands, rData;
    private File tmpCommands, tmpData;
    private String outfile, rPath = "/software/bin/R-dev";

    public QAGrapher(int endLength, String outputPrefix, String rPath) throws IOException {
        this.windowLen = endLength;
        outfile = outputPrefix + ".jpg";
        this.rPath = rPath;
        createTempFilehandles();
    }

    private void createTempFilehandles() throws IOException {
        tmpCommands = File.createTempFile("commands", ".tmp");
        tmpData = File.createTempFile("data", ".tmp");
        rCommands = new BufferedWriter(new FileWriter(tmpCommands));
        rData = new BufferedWriter(new FileWriter(tmpData));
    }

    @Override
    public void parseRecord(SGSRecord record) {
        // Want read length, GC% and median read quality
        int l = record.getLength();
        float m = record.calcMedianQualityScore();
        float gc = record.calcGCPercentage();
        try {
            rData.write(String.format("%.2f\t%.2f\t%d%s\n", gc, m, l, parseReadEnd(record)));
        } catch (IOException ex) {
        }
    }
    
    private String parseReadEnd(SGSRecord record) {
        Integer[] quals = Invariables.convertASCIIToPhred(record.getQuality());
        StringBuilder out = new StringBuilder();
        int start;
        int l = quals.length;
        if (l < windowLen) {
            for (int i=l; i < windowLen; i++) {
                out.append("\tNA");
            }
            start = 0;
        } else {
            start = l - windowLen;
        }
        for (int j = start; j < l; j++) {
            out.append("\t");
            out.append(quals[j]);
        }
        return out.toString();
    }

    @Override
    public void parseRecords(SGSRecord forRecord, SGSRecord revRecord) {
        parseRecord(forRecord);
        parseRecord(revRecord);
    }

    @Override
    public void parseRecords(List<SGSRecord> records) {
        for (SGSRecord record : records) {
            parseRecord(record);
        }
    }

    @Override
    public void parseRecords(List<SGSRecord> forRecords, List<SGSRecord> revRecords) {
        for (int i = 0; i < forRecords.size(); i++) {
            parseRecord(forRecords.get(i));
            parseRecord(revRecords.get(i));
        }
    }

    @Override
    public void printOutputStats() {
        try {
            rData.close();
        } catch (IOException ex) {
            Logger.getLogger(QAGrapher.class.getName()).log(Level.SEVERE, null, ex);
        }
        StringBuilder commands = new StringBuilder();
        commands.append(String.format("raw.data <- read.table('%s', header=F, sep='\\t')\n", tmpData.getAbsoluteFile()));
        commands.append(String.format("jpeg(file='%s', height=7016, width=4960, res=600)\n", outfile));
        commands.append("par(oma=c(0,0,2,0), mar=c(4,4,4,2), font.main=2, xaxs=\"i\", yaxs=\"i\", cex.axis=0.9)\n");
        commands.append("layout(matrix(c(1,2,3,4,5,5), 3, 2, byrow = TRUE))\n");
        commands.append("mean.length <- round(mean(raw.data[,3]), digits=2)\n");
        commands.append("sd.length <- round(sd(raw.data[,3]), digits=2)\n");
        commands.append("hist(raw.data[,3], breaks=20, xlab=\"Read length\", col=\"skyblue\", xlim=c(0, max(raw.data[,3])), main=paste(\"Mean length:\", mean.length, \"+/-\", sd.length))\n");
        commands.append("mean.gc <- round(mean(raw.data[,1]), digits=2)\n");

        commands.append("sd.gc <- round(sd(raw.data[,1]), digits=2)\n");
        commands.append("hist(raw.data[,1], breaks=20, xlab=\"GC %\", col=\"lemonchiffon1\", main=paste(\"Mean GC%:\", mean.gc, \"+/-\", sd.gc))\n");

        commands.append("mean.median <- round(mean(raw.data[,2]), digits=2)\n");
        commands.append("plot(raw.data[,2]~raw.data[,3], xlim=c(0,max(raw.data[,3])), ylim=c(0,max(raw.data[,2])), pch=18, col=\"gray70\", xlab=\"Read length\", ylab=\"Median quality\", main=\"Read median quality as a function of length\")\n");
        commands.append("abline(h=mean.median, col=\"black\", lty=2)\n");
        commands.append("abline(v=mean.length, col=\"black\", lty=2)\n");

        commands.append("sd.median <- round(sd(raw.data[,2]), digits=2)\n");
        commands.append("hist(raw.data[,2], breaks=20, xlab=\"Read median quality\", xlim=c(0, max(raw.data[,2])), col=\"mistyrose2\", main=paste(\"Mean median-quality:\", mean.median, \"+/-\", sd.median))\n");

        commands.append("par(mar=c(5,7,5,5))\n");
        commands.append("par(xaxs=\"r\")\n");
        commands.append(String.format("means <- colMeans(raw.data[4:%d], na.rm=T)\n", windowLen+3));
        commands.append("y.max <- max(means, na.rm=T)\n");
        commands.append("remainder <- y.max%%5\n");
        commands.append("y.max <- y.max + (5-remainder)\n");
        commands.append("plot(means, ylim=c(0,y.max), xlab=\"Position from end of read\", ylab=\"Mean quality\", axes=F, col=\"pink\", pch=20, main=\"3'-end cross-sectional mean quality\")\n");
        commands.append("points(means, ylim=c(0,y.max), col=\"red\", type='l')\n");
        commands.append(String.format("axis(1, at=seq(0, %d, 10), lab=seq(-%d, 0, 10))\n", windowLen, windowLen));
        commands.append("axis(2, at=seq(0, y.max, 5))\n");

        commands.append("title(main=paste(\"Total sequences:\", length(raw.data[,1])), outer=T)\n");
        commands.append("dev.off()\n");
        try {
            try {
                rCommands.write(commands.toString());
            } finally {
                rCommands.close();
            }
            Process p = Runtime.getRuntime().exec(rPath + " CMD BATCH " + tmpCommands.getAbsolutePath());
            p.waitFor();
            closeHandles();
            System.out.println("[INFO]: QA graphs written to " + outfile);
        } catch (IOException ex) {
            Logger.getLogger(QAGrapher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(QAGrapher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void closeHandles() throws IOException {
        rCommands.close();
        rData.close();
        tmpCommands.delete();
        tmpData.delete();
        new File(tmpCommands.getName() + ".Rout").delete();
    }
}
