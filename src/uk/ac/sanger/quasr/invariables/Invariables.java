package uk.ac.sanger.quasr.invariables;

import java.util.HashMap;
import java.util.Map;

public final class Invariables {
	
    // Ensures class isn't instantiated
	private Invariables() {
		throw new AssertionError();
	}
	private static int ASCIIOFFSET = 33;
	
	public static void SetASCIIOffset(int i) {
		Invariables.ASCIIOFFSET = i;
	}
	
	public static final Map<String, Character> AMBIGUITYCODES = new HashMap<String, Character>();
	static {
		AMBIGUITYCODES.put("AC", 'M');	AMBIGUITYCODES.put("AG", 'R');	AMBIGUITYCODES.put("AT", 'W');
		AMBIGUITYCODES.put("CG", 'S');	AMBIGUITYCODES.put("CT", 'Y');	AMBIGUITYCODES.put("GT", 'K');
		AMBIGUITYCODES.put("ACG", 'V');	AMBIGUITYCODES.put("ACT", 'H');	AMBIGUITYCODES.put("AGT", 'D');
		AMBIGUITYCODES.put("CGT", 'B'); AMBIGUITYCODES.put("ACGT", 'N');
	}
	
	private static final Map<Integer, Character> PHRED2ASCII = new HashMap<Integer, Character>();
	static {
		PHRED2ASCII.put(33, '!');		PHRED2ASCII.put(57, '9');		PHRED2ASCII.put(81, 'Q');		PHRED2ASCII.put(105, 'i');
		PHRED2ASCII.put(34, '\"');		PHRED2ASCII.put(58, ':');		PHRED2ASCII.put(82, 'R');		PHRED2ASCII.put(106, 'j');
		PHRED2ASCII.put(35, '#');		PHRED2ASCII.put(59, ';');		PHRED2ASCII.put(83, 'S');		PHRED2ASCII.put(107, 'k');
		PHRED2ASCII.put(36, '$');		PHRED2ASCII.put(60, '<');		PHRED2ASCII.put(84, 'T');		PHRED2ASCII.put(108, 'l');
		PHRED2ASCII.put(37, '%');		PHRED2ASCII.put(61, '=');		PHRED2ASCII.put(85, 'U');		PHRED2ASCII.put(109, 'm');
		PHRED2ASCII.put(38, '&');		PHRED2ASCII.put(62, '>');		PHRED2ASCII.put(86, 'V');		PHRED2ASCII.put(110, 'n');
		PHRED2ASCII.put(39, '\'');		PHRED2ASCII.put(63, '?');		PHRED2ASCII.put(87, 'W');		PHRED2ASCII.put(111, 'o');
		PHRED2ASCII.put(40, '(');		PHRED2ASCII.put(64, '@');		PHRED2ASCII.put(88, 'X');		PHRED2ASCII.put(112, 'p');
		PHRED2ASCII.put(41, ')');		PHRED2ASCII.put(65, 'A');		PHRED2ASCII.put(89, 'Y');		PHRED2ASCII.put(113, 'q');
		PHRED2ASCII.put(42, '*');		PHRED2ASCII.put(66, 'B');		PHRED2ASCII.put(90, 'Z');		PHRED2ASCII.put(114, 'r');
		PHRED2ASCII.put(43, '+');		PHRED2ASCII.put(67, 'C');		PHRED2ASCII.put(91, '[');		PHRED2ASCII.put(115, 's');
		PHRED2ASCII.put(44, ',');		PHRED2ASCII.put(68, 'D');		PHRED2ASCII.put(92, '\\');		PHRED2ASCII.put(116, 't');
		PHRED2ASCII.put(45, '-');		PHRED2ASCII.put(69, 'E');		PHRED2ASCII.put(93, ']');		PHRED2ASCII.put(117, 'u');
		PHRED2ASCII.put(46, '.');		PHRED2ASCII.put(70, 'F');		PHRED2ASCII.put(94, '^');		PHRED2ASCII.put(118, 'v');
		PHRED2ASCII.put(47, '/');		PHRED2ASCII.put(71, 'G');		PHRED2ASCII.put(95, '_');		PHRED2ASCII.put(119, 'w');
		PHRED2ASCII.put(48, '0');		PHRED2ASCII.put(72, 'H');		PHRED2ASCII.put(96, '`');		PHRED2ASCII.put(120, 'x');
		PHRED2ASCII.put(49, '1');		PHRED2ASCII.put(73, 'I');		PHRED2ASCII.put(97, 'a');		PHRED2ASCII.put(121, 'y');
		PHRED2ASCII.put(50, '2');		PHRED2ASCII.put(74, 'J');		PHRED2ASCII.put(98, 'b');		PHRED2ASCII.put(122, 'z');
		PHRED2ASCII.put(51, '3');		PHRED2ASCII.put(75, 'K');		PHRED2ASCII.put(99, 'c');		PHRED2ASCII.put(123, '{');
		PHRED2ASCII.put(52, '4');		PHRED2ASCII.put(76, 'L');		PHRED2ASCII.put(100, 'd');		PHRED2ASCII.put(124, '|');
		PHRED2ASCII.put(53, '5');		PHRED2ASCII.put(77, 'M');		PHRED2ASCII.put(101, 'e');		PHRED2ASCII.put(125, '}');
		PHRED2ASCII.put(54, '6');		PHRED2ASCII.put(78, 'N');		PHRED2ASCII.put(102, 'f');              PHRED2ASCII.put(126, '~');
		PHRED2ASCII.put(55, '7');		PHRED2ASCII.put(79, 'O');		PHRED2ASCII.put(103, 'g');
		PHRED2ASCII.put(56, '8');		PHRED2ASCII.put(80, 'P');		PHRED2ASCII.put(104, 'h');
	}
	
	public static String convertPhredToASCII(byte[] qualities) {
		int len = qualities.length;
		StringBuilder output = new StringBuilder(len);
		for (int i=0; i<len; i++) {
			Character qual = Invariables.PHRED2ASCII.get(qualities[i] + Invariables.ASCIIOFFSET);
			if (qual == null) {
				output.append(Invariables.PHRED2ASCII.get(Invariables.ASCIIOFFSET));
			}
			else {
				output.append(qual);
			}
		}
		return output.toString();
	}
	
	public static Integer[] convertASCIIToPhred(String ascii) {
		int len = ascii.length();
		Integer[] phreds = new Integer[len];
		for (int i=0; i < len; i++) {
			if (ASCII2PHRED.containsKey(ascii.charAt(i))) {
				phreds[i] = ASCII2PHRED.get(ascii.charAt(i)) - ASCIIOFFSET;
			}
			else {
				phreds[i] = 0;
			}
		}
		return phreds;
	}
	
	private static final Map<Character, Integer> ASCII2PHRED = new HashMap<Character, Integer>();
	static {
		ASCII2PHRED.put('!', 33);		ASCII2PHRED.put('9', 57);		ASCII2PHRED.put('Q', 81);		ASCII2PHRED.put('i', 105);
		ASCII2PHRED.put('\"', 34);		ASCII2PHRED.put(':', 58);		ASCII2PHRED.put('R', 82);		ASCII2PHRED.put('j', 106);
		ASCII2PHRED.put('#', 35);		ASCII2PHRED.put(';', 59);		ASCII2PHRED.put('S', 83);		ASCII2PHRED.put('k', 107);
		ASCII2PHRED.put('$', 36);		ASCII2PHRED.put('<', 60);		ASCII2PHRED.put('T', 84);		ASCII2PHRED.put('l', 108);
		ASCII2PHRED.put('%', 37);		ASCII2PHRED.put('=', 61);		ASCII2PHRED.put('U', 85);		ASCII2PHRED.put('m', 109);
		ASCII2PHRED.put('&', 38);		ASCII2PHRED.put('>', 62);		ASCII2PHRED.put('V', 86);		ASCII2PHRED.put('n', 110);
		ASCII2PHRED.put('\'', 39);		ASCII2PHRED.put('?', 63);		ASCII2PHRED.put('W', 87);		ASCII2PHRED.put('o', 111);
		ASCII2PHRED.put('(', 40);		ASCII2PHRED.put('@', 64);		ASCII2PHRED.put('X', 88);		ASCII2PHRED.put('p', 112);
		ASCII2PHRED.put(')', 41);		ASCII2PHRED.put('A', 65);		ASCII2PHRED.put('Y', 89);		ASCII2PHRED.put('q', 113);
		ASCII2PHRED.put('*', 42);		ASCII2PHRED.put('B', 66);		ASCII2PHRED.put('Z', 90);		ASCII2PHRED.put('r', 114);
		ASCII2PHRED.put('+', 43);		ASCII2PHRED.put('C', 67);		ASCII2PHRED.put('[', 91);		ASCII2PHRED.put('s', 115);
		ASCII2PHRED.put(',', 44);		ASCII2PHRED.put('D', 68);		ASCII2PHRED.put('\\', 92);		ASCII2PHRED.put('t', 116);
		ASCII2PHRED.put('-', 45);		ASCII2PHRED.put('E', 69);		ASCII2PHRED.put(']', 93);		ASCII2PHRED.put('u', 117);
		ASCII2PHRED.put('.', 46);		ASCII2PHRED.put('F', 70);		ASCII2PHRED.put('^', 94);		ASCII2PHRED.put('v', 118);
		ASCII2PHRED.put('/', 47);		ASCII2PHRED.put('G', 71);		ASCII2PHRED.put('_', 95);		ASCII2PHRED.put('w', 119);
		ASCII2PHRED.put('0', 48);		ASCII2PHRED.put('H', 72);		ASCII2PHRED.put('`', 96);		ASCII2PHRED.put('x', 120);
		ASCII2PHRED.put('1', 49);		ASCII2PHRED.put('I', 73);		ASCII2PHRED.put('a', 97);		ASCII2PHRED.put('y', 121);
		ASCII2PHRED.put('2', 50);		ASCII2PHRED.put('J', 74);		ASCII2PHRED.put('b', 98);		ASCII2PHRED.put('z', 122);
		ASCII2PHRED.put('3', 51);		ASCII2PHRED.put('K', 75);		ASCII2PHRED.put('c', 99);		ASCII2PHRED.put('{', 123);
		ASCII2PHRED.put('4', 52);		ASCII2PHRED.put('L', 76);		ASCII2PHRED.put('d', 100);		ASCII2PHRED.put('|', 124);
		ASCII2PHRED.put('5', 53);		ASCII2PHRED.put('M', 77);		ASCII2PHRED.put('e', 101);		ASCII2PHRED.put('}', 125);
		ASCII2PHRED.put('6', 54);		ASCII2PHRED.put('N', 78);		ASCII2PHRED.put('f',102);
		ASCII2PHRED.put('7', 55);		ASCII2PHRED.put('O', 79);		ASCII2PHRED.put('g', 103);
		ASCII2PHRED.put('8', 56);		ASCII2PHRED.put('P', 80);		ASCII2PHRED.put('h', 104);
        }
        
        public static final Map<Integer, String[]> ROCHE_MIDS = new HashMap<Integer, String[]>();
        static {
            ROCHE_MIDS.put(1, new String[]{"ACACGACGACT", "AGTCGTGGTGT"});
            ROCHE_MIDS.put(2, new String[]{"ACACGTAGTAT", "ATACTAGGTGT"});
            ROCHE_MIDS.put(3, new String[]{"ACACTACTCGT", "ACGAGTGGTGT"});
            ROCHE_MIDS.put(4, new String[]{"ACGACACGTAT", "ATACGTGGCGT"});
            ROCHE_MIDS.put(5, new String[]{"ACGAGTAGACT", "AGTCTACGCGT"});
            ROCHE_MIDS.put(6, new String[]{"ACGCGTCTAGT", "ACTAGAGGCGT"});
            ROCHE_MIDS.put(7, new String[]{"ACGTACACACT", "AGTGTGTGCGT"});
            ROCHE_MIDS.put(8, new String[]{"ACGTACTGTGT", "ACACAGTGCGT"});
            ROCHE_MIDS.put(9, new String[]{"ACGTAGATCGT", "ACGATCTGCGT"});
            ROCHE_MIDS.put(10, new String[]{"ACTACGTCTCT", "AGAGACGGAGT"});
            ROCHE_MIDS.put(11, new String[]{"ACTATACGAGT", "ACTCGTAGAGT"});
            ROCHE_MIDS.put(12, new String[]{"ACTCGCGTCGT", "ACGACGGGAGT"});
            ROCHE_MIDS.put(13, new String[]{"AGACTCGACGT", "ACGTCGGGTCT"});
            ROCHE_MIDS.put(14, new String[]{"AGTACGAGAGT", "ACTCTCGGACT"});
            ROCHE_MIDS.put(15, new String[]{"AGTACTACTAT", "ATAGTAGGACT"});
            ROCHE_MIDS.put(16, new String[]{"AGTAGACGTCT", "AGACGTCGACT"});
            ROCHE_MIDS.put(17, new String[]{"AGTCGTACACT", "AGTGTAGGACT"});
            ROCHE_MIDS.put(18, new String[]{"AGTGTAGTAGT", "ACTACTAGACT"});
            ROCHE_MIDS.put(19, new String[]{"ATAGTATACGT", "ACGTATAGTAT"});
            ROCHE_MIDS.put(20, new String[]{"CAGTACGTACT", "AGTACGTGCTG"});
            ROCHE_MIDS.put(21, new String[]{"CGACGACGCGT", "ACGCGTGGTCG"});
            ROCHE_MIDS.put(22, new String[]{"CGACGAGTACT", "AGTACTGGTCG"});
            ROCHE_MIDS.put(23, new String[]{"CGATACTACGT", "ACGTAGTGTCG"});
            ROCHE_MIDS.put(24, new String[]{"CGTACGTCGAT", "ATCGACGGACG"});
            ROCHE_MIDS.put(25, new String[]{"CTACTCGTAGT", "ACTACGGGTAG"});
            ROCHE_MIDS.put(26, new String[]{"GTACAGTACGT", "ACGTACGGTAC"});
            ROCHE_MIDS.put(27, new String[]{"GTCGTACGTAT", "ATACGTAGGAC"});
            ROCHE_MIDS.put(28, new String[]{"GTGTACGACGT", "ACGTCGTGCAC"});
            ROCHE_MIDS.put(29, new String[]{"ACACAGTGAGT", "ACTCACGGTGT"});
            ROCHE_MIDS.put(30, new String[]{"ACACTCATACT", "AGTATGGGTGT"});
            ROCHE_MIDS.put(31, new String[]{"ACAGACAGCGT", "ACGCTGTGTGT"});
            ROCHE_MIDS.put(32, new String[]{"ACAGACTATAT", "ATATAGTGTGT"});
            ROCHE_MIDS.put(33, new String[]{"ACAGAGACTCT", "AGAGTCTGTGT"});
            ROCHE_MIDS.put(34, new String[]{"ACAGCTCGTGT", "ACACGAGGTGT"});
            ROCHE_MIDS.put(35, new String[]{"ACAGTGTCGAT", "ATCGACAGTGT"});
            ROCHE_MIDS.put(36, new String[]{"ACGAGCGCGCT", "AGCGCGCGCGT"});
            ROCHE_MIDS.put(37, new String[]{"ACGATGAGTGT", "ACACTCAGCGT"});
            ROCHE_MIDS.put(38, new String[]{"ACGCGAGAGAT", "ATCTCTGGCGT"});
            ROCHE_MIDS.put(39, new String[]{"ACGCTCTCTCT", "AGAGAGGGCGT"});
            ROCHE_MIDS.put(40, new String[]{"ACGTCGCTGAT", "ATCAGCGGCGT"});
            ROCHE_MIDS.put(41, new String[]{"ACGTCTAGCAT", "ATGCTAGGCGT"});
            ROCHE_MIDS.put(42, new String[]{"ACTAGTGATAT", "ATATCACGAGT"});
            ROCHE_MIDS.put(43, new String[]{"ACTCACACTGT", "ACAGTGGGAGT"});
            ROCHE_MIDS.put(44, new String[]{"ACTCACTAGCT", "AGCTAGGGAGT"});
            ROCHE_MIDS.put(45, new String[]{"ACTCTATATAT", "ATATATGGAGT"});
            ROCHE_MIDS.put(46, new String[]{"ACTGATCTCGT", "ACGAGATGAGT"});
            ROCHE_MIDS.put(47, new String[]{"ACTGCTGTACT", "AGTACAGGAGT"});
            ROCHE_MIDS.put(48, new String[]{"ACTGTAGCGCT", "AGCGCTAGAGT"});
        }
	
        public static final Map<Integer, String[]> ILLUMINA_MIDS = new HashMap<Integer, String[]>();
        static {
            ILLUMINA_MIDS.put(1, new String[]{});
        }
        
        public static final Map<String, String> GENERAL_SUFFIXES = new HashMap<String, String>();
        static {
            GENERAL_SUFFIXES.put("fasta", ".fa");
            GENERAL_SUFFIXES.put("fastq", ".fq");
            GENERAL_SUFFIXES.put("sff", ".sff");
            GENERAL_SUFFIXES.put("jpg", ".jpg");
            GENERAL_SUFFIXES.put("pdf", ".pdf");
            GENERAL_SUFFIXES.put("forward", ".f");
            GENERAL_SUFFIXES.put("reverse", ".r");
            GENERAL_SUFFIXES.put("SE_duplicate", ".uniq");
            GENERAL_SUFFIXES.put("PE_duplicate_f", ".uniq.f");
            GENERAL_SUFFIXES.put("PE_duplicate_r", ".uniq.r");
            GENERAL_SUFFIXES.put("processor", ".qc");            
        }
}