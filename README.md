QUASR
=====

QUASR is a lightweight pipeline written to process and analyse next-generation sequencing (NGS) data from Illumina, 454, and Ion Torrent platforms. Although originally written for viral data, it is generic enough to work on any NGS dataset. Functions include: duplicate removal demultiplexing primer-removal quality-assurance (QA) graphing quality control (QC) consensus-generation minority-variant determination minority-variant graphing

Building the QUASR app
======================

 **1.** Open a command line terminal

 **2.** Clone the git project

 **3.** cd into the project folder

 **4.** run: ant

 **5.** A folder (quasr_dist) one level up from the project folder will be created.

 **6.** cd into it and run: java -jar readsetProcessor.jar
 

USAGE
=====
-h/--help	Print this usage information

-v/--version	Print version number

-i/--infile	SE or single-PE FASTQ (or GZIPed FASTQ) or SFF ***\****

-r/--reverse	FASTQ (or GZIPed FASTQ) or SFF containing reverse mates

-2/--paired	Input file is paired-end. Only necessary if 1 file parsed

-o/--outprefix	Output directory and file prefix ***\****

-z/--gzip	Compress output files to GZIPed FASTQ

-n/--num	Number of records to parse at a time [default: 100]

-I/--illumina	Quality scores encoded with Illumina offset (+64) instead of Sanger (+33)


Demultiplex:

-M/--mids	Comma-separated MIDs to be parsed. Accepts ranges

-s/--sequence	Demultiplex by parsing sequence. Mutually-exlusive with -d ***\+***

-c/--custom	File containing custom MID sequences. Only allowed with -s

-O/--offset	Maximum offset MID can be within a read. Only allowed with -s [default: 10]

-H/--header	Demultiplex by parsing header. Mutually exclusive with -s ***\+***

-P/--pattern	Regex to match in header. Only allowed with -d [default: "#(\d+)/\d$"]


Duplicate removal:

-d/--duplicate	Perform duplicate removal


Primer removal:

-p/--primer	File containing primer sequences

-L/--leeway	Maximum distance primer can be within a read [default: 40]

Quality control:

-q/--quality	Perform quality control

-l/--length	Minimum read length cutoff [default: 50]

-m/--median	Median read quality cutoff [default: 20.0]


QA graphing:

-g/--graph	Perform quality assurance graphing

-R/--Rpath	Path to R binary (only needs to be set once if current stored path doesn't work)

-w/--window	Window length for 3'-cross sectional dropoff [default:50]

[NOTE]: Options marked ***\**** are mandatory. Those marked ***\+*** are mandatory but mutually-exclusive
All others are optional. All steps are optional. Steps are performed in the order shown.
