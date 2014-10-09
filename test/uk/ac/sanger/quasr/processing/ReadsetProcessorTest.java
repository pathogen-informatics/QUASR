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
