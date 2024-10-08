package FileWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateProcessorTest {

  private static final String OUTPUT_DIR = "output";

  @BeforeEach
  void setUp() {

  }

  @Test
  void outputFiles() throws IOException {

    Map<Integer, List<String>> csvContent = new HashMap<>();
    csvContent.put(1, List.of("John", "Doe", "30"));
    csvContent.put(2, List.of("Jane", "Smith", "25"));


    String templateContent = "Name: [[First Name]] [[Last Name]], Age: [[Age]]";


    Map<String, Integer> headerMap = new HashMap<>();
    headerMap.put("First Name", 0);
    headerMap.put("Last Name", 1);
    headerMap.put("Age", 2);


    TemplateProcessor.outputFiles(csvContent, templateContent, OUTPUT_DIR, headerMap, "output");


    File file1 = new File(OUTPUT_DIR + File.separator + "output 1.txt");
    assertTrue(file1.exists());

  }

  @Test
  void createOutputDirectory() throws IOException {

    TemplateProcessor.createOutputDirectory(OUTPUT_DIR);


    File directory = new File(OUTPUT_DIR);
    assertTrue(directory.exists());
  }

  @Test
  void createOutputDirectory_WhenDirectoryCreationFails() {

    String invalidDirPath = "/path/to/nonexistent/directory/file.txt";


    IOException exception = assertThrows(IOException.class, () -> TemplateProcessor.createOutputDirectory(invalidDirPath));


    assertEquals("Failed to create output directory: " + invalidDirPath, exception.getMessage());
  }


  @Test
  void generateOutputFilePath() {

    String filePath = TemplateProcessor.generateOutputFilePath(OUTPUT_DIR, "output", 1);


    assertEquals(OUTPUT_DIR + File.separator + "output 1.txt", filePath);
  }

  @Test
  void processTemplateSuccessful() {

    String template = "Name: [[First Name]] [[Last Name]], Age: [[Age]]";


    List<String> rowData = List.of("John", "Doe", "30");


    Map<String, Integer> headerMap = new HashMap<>();
    headerMap.put("First Name", 0);
    headerMap.put("Last Name", 1);
    headerMap.put("Age", 2);


    String processedTemplate = TemplateProcessor.processTemplate(template, rowData, headerMap);


    assertEquals("Name: John Doe, Age: 30", processedTemplate);
  }

  @Test
  void findAll() {

    String input = "Name: [[First Name]] [[Last Name]], Age: [[Age]]";
    String invalidInput = "No placeholder";


    List<String> placeholders = TemplateProcessor.findAll(input);



    assertEquals(3, placeholders.size());
    assertTrue(placeholders.contains("First Name"));
    assertTrue(placeholders.contains("Last Name"));
    assertTrue(placeholders.contains("Age"));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> TemplateProcessor.findAll(invalidInput));
    assertEquals("No placeholders found in the input string.", exception.getMessage());
  }

  @Test
  void writeOutputFile() throws IOException {

    String filePath = OUTPUT_DIR + File.separator + "test.txt";
    TemplateProcessor.writeOutputFile(filePath, "Test content");


    File file = new File(filePath);
    assertTrue(file.exists());


    if (file.exists()) {
      file.delete();
    }
  }
}
