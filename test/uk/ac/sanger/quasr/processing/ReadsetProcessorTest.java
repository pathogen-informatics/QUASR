package uk.ac.sanger.quasr.processing;

import uk.ac.sanger.quasr.SEReadsetProcessor;
import org.junit.Test;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;


public class ReadsetProcessorTest 
{
  @Test
  /**
   * Test removing a single primer/adapter from the start
   */
  public void TestPrimerRemovalStart()
  {
    try{
       check_files_equal("data/no_adapters.fastq","data/expected/no_adapters.qc.fq");
       check_files_equal("data/adapter_at_end.fastq","data/expected/adapter_at_end.qc.fq");
       check_files_equal("data/adapter_at_end_3_times.fastq","data/expected/adapter_at_end_3_times.qc.fq");
       check_files_equal("data/adapter_at_end_one_mismatch.fastq","data/expected/adapter_at_end_one_mismatch.qc.fq");
       check_files_equal("data/adapter_at_end_three_mismatches.fastq","data/expected/adapter_at_end_three_mismatches.qc.fq");
       check_files_equal("data/adapter_at_end_two_mismatches.fastq","data/expected/adapter_at_end_two_mismatches.qc.fq");
       check_files_equal("data/adapter_at_start.fastq","data/expected/adapter_at_start.qc.fq");
       check_files_equal("data/adapter_at_start_3_times.fastq","data/expected/adapter_at_start_3_times.qc.fq");
       check_files_equal("data/adapter_at_start_one_mismatch.fastq","data/expected/adapter_at_start_one_mismatch.qc.fq");
       check_files_equal("data/adapter_at_start_three_mismatches.fastq","data/expected/adapter_at_start_three_mismatches.qc.fq");
       check_files_equal("data/adapter_at_start_two_mismatches.fastq","data/expected/adapter_at_start_two_mismatches.qc.fq");
       check_files_equal("data/adapter_in_middle.fastq","data/expected/adapter_in_middle.qc.fq");
       check_files_equal("data/adapters_at_start_and_end_fwd_rev.fastq","data/expected/adapters_at_start_and_end_fwd_rev.qc.fq");
       check_files_equal("data/different_adapters_at_end_and_middle_one_mismatch.fastq","data/expected/different_adapters_at_end_and_middle_one_mismatch.qc.fq");
       check_files_equal("data/different_adapters_at_end_and_middle_two_mismatches.fastq","data/expected/different_adapters_at_end_and_middle_two_mismatches.qc.fq");
       check_files_equal("data/different_adapters_at_start_and_middle_fwd.fastq","data/expected/different_adapters_at_start_and_middle_fwd.qc.fq");
       check_files_equal("data/different_adapters_at_start_and_middle_fwd_rev.fastq","data/expected/different_adapters_at_start_and_middle_fwd_rev.qc.fq");
       check_files_equal("data/different_adapters_at_start_and_middle_one_mismatch.fastq","data/expected/different_adapters_at_start_and_middle_one_mismatch.qc.fq");
       check_files_equal("data/different_adapters_at_start_and_middle_rev.fastq","data/expected/different_adapters_at_start_and_middle_rev.qc.fq");
       check_files_equal("data/different_adapters_at_start_and_middle_two_mismatches.fastq","data/expected/different_adapters_at_start_and_middle_two_mismatches.qc.fq");
       
    }catch (IOException ex) {
      System.out.println(ex.toString());
    }
  }
  
  public static void check_files_equal(String input_filename, String expected_filename) throws IOException {
    try{
         String actual_filename = "test.qc.fq";
         SEReadsetProcessor se = new SEReadsetProcessor(input_filename, "test", false);
         se.addPrimerRemovalToPipeline("data/primers.quasr", 40);
         se.runPipeline(); 
         BufferedReader expected = new BufferedReader(new FileReader(expected_filename));
         BufferedReader actual = new BufferedReader(new FileReader(actual_filename));
         String line;
         while ((line = expected.readLine()) != null) {
           assertEquals(line, actual.readLine());
         }
         
         assertNull("Actual had more lines then the expected.", actual.readLine());
         assertNull("Expected had more lines then the actual.", expected.readLine());
       }catch (IOException ex) {
         System.out.println (ex.toString());
       }
     } 
}
