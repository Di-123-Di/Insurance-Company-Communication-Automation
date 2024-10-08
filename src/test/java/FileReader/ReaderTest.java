package FileReader;

import static org.junit.jupiter.api.Assertions.*;
import CommandLine.InvalidArgumentException;
import FileWriter.TemplateProcessor;
import CommandLine.CommandLineParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.List;
import java.util.Map;


class ReaderTest {
  @TempDir
  static File tempDir;

  private static File createTempFileWithContent(String content, String fileExtension) throws IOException {
    File tempFile = File.createTempFile("temp", fileExtension, tempDir);
    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write(content);
    }
    return tempFile;
  }

  @Test
  void readFileContent() {
    String content = "Line 1\nLine 2\nLine 3";
    File tempFile;
    try {
      tempFile = createTempFileWithContent(content, ".txt");
      List<String> fileContent = Reader.readFileContent(tempFile.getPath());
      assertEquals(3, fileContent.size());
      assertEquals(content, String.join("\n", fileContent));
    } catch (IOException e) {
      fail("IOException occurred while creating temporary file.");
    }


    assertThrows(FileNotFoundException.class, () -> Reader.readFileContent("non-existing-file.txt"));
  }

  @Test
  void parseCSVLine() {
    String csvLine = "\"John\",\"Doe\",\"ACME\"";
    List<String> values = Reader.parseCSVLine(csvLine);
    assertEquals(3, values.size());
    assertEquals("John", values.get(0));
    assertEquals("Doe", values.get(1));
    assertEquals("ACME", values.get(2));
  }

  @Test
  void parseCSVContent() {
    List<String> csvLines = List.of("\"first_name\",\"last_name\",\"company_name\"");
    Map<Integer, List<String>> csvContent = Reader.parseCSVContent(csvLines);
    assertEquals(1, csvContent.size());
    assertEquals(List.of("first_name", "last_name", "company_name"), csvContent.get(0));
  }

  @Test
  void readCSV() throws IOException {
    String csvContent = "\"first_name\",\"last_name\",\"company_name\"\n\"John\",\"Doe\",\"ACME\"";
    File tempFile = createTempFileWithContent(csvContent, ".csv");
    Map<Integer, List<String>> csvContentMap = Reader.readCSV(tempFile.getPath());
    assertEquals(2, csvContentMap.size());
    assertEquals(List.of("John", "Doe", "ACME"), csvContentMap.get(1));


    String emptyContent = "";
    tempFile = createTempFileWithContent(emptyContent, ".csv");
    File finalTempFile = tempFile;
    assertThrows(IllegalArgumentException.class, () -> Reader.readCSV(finalTempFile.getPath()));
  }


  @Test
  void processFilesValid() throws IOException {
    File templateFile = createTempFileWithContent("template content", ".txt");
    File letterFile = createTempFileWithContent("letter content", ".txt");
    File csvFile = createTempFileWithContent("csv content", ".csv");

    String[] validArgs = {"--email",
        "--email-template", templateFile.getAbsolutePath(),
        "--letter",
        "--letter-template", letterFile.getAbsolutePath(),
        "--output-dir", tempDir.getPath(),
        "--csv-file", csvFile.getAbsolutePath()};
    assertDoesNotThrow(() -> Reader.processFiles(validArgs)); }


 @Test
    void processFilesNoCVSOption() throws IOException, InvalidArgumentException {
   File templateFile = createTempFileWithContent("template content", ".txt");
    File letterFile = createTempFileWithContent("letter content", ".txt");
    File csvFile = createTempFileWithContent("csv content", ".csv");

    String[] noCSVOptionArgs = {"--email",
        "--email-template", templateFile.getAbsolutePath(),
        "--letter",
        "--letter-template", letterFile.getAbsolutePath(),
        "--output-dir", tempDir.getPath(),
        csvFile.getAbsolutePath()};
      ByteArrayOutputStream outContent = new ByteArrayOutputStream();
      System.setOut(new PrintStream(outContent));
      Reader.processFiles(noCSVOptionArgs);
      System.setOut(System.out);
      assertTrue(outContent.toString().contains("Incorrect: --csv-file is missing"));}

  @Test
  void readTemplate() throws IOException {
    String templateContent = "Dear [[first_name]] [[last_name]]," + System.lineSeparator() +
        "We are pleased to inform you..."+ System.lineSeparator();
    File tempFile = createTempFileWithContent(templateContent, ".txt");
    String actualContent = Reader.readTemplate(tempFile.getPath());
    assertEquals(templateContent, actualContent);


    String emptyContent = "";
    tempFile = createTempFileWithContent(emptyContent, ".txt");
    File finalTempFile = tempFile;
    assertThrows(IllegalArgumentException.class, () -> Reader.readTemplate(finalTempFile.getPath()));
  }

  @Test
  void processTemplatesEmail() throws IOException {
    String csvContent = "\"first_name\",\"last_name\",\"company_name\",\"address\",\"city\",\"county\",\"state\",\"zip\",\"phone1\",\"phone2\",\"email\",\"web\"\n" +
        "\"James\",\"Butt\",\"Benton, John B Jr\",\"6649 N Blue Gum St\",\"New Orleans\",\"Orleans\",\"LA\",\"70116\",\"504-621-8927\",\"504-845-1427\",\"jbutt@gmail.com\",\"http://www.bentonjohnbjr.com\"\n" +
        "\"Josephine\",\"Darakjy\",\"Chanay, Jeffrey A Esq\",\"4 B Blue Ridge Blvd\",\"Brighton\",\"Livingston\",\"MI\",\"48116\",\"810-292-9388\",\"810-374-9840\",\"josephine_darakjy@darakjy.org\",\"http://www.chanayjeffreyaesq.com\"";
    File csvFile;
    try {
      csvFile = createTempFileWithContent(csvContent, ".csv");
    } catch (IOException e) {
      fail("IOException occurred while creating temporary CSV file.");
      return;
    }

    Map<Integer, List<String>> read1 = Reader.readCSV(csvFile.getPath());


    String emailTemplateContent = "To: [[email]]\n" +
        "Subject: Insurance company – information about recent data breach\n" +
        "Dear [[first_name]] [[last_name]],";
    File emailTemplateFile;
    try {
      emailTemplateFile = createTempFileWithContent(emailTemplateContent, ".txt");
    } catch (IOException e) {
      fail("IOException occurred while creating temporary email template file.");
      return;
    }

    Map<String, Integer> indexMap = Reader.headerIndex(read1);
    assertEquals(12, indexMap.size());
    assertEquals(0, indexMap.get("first_name"));
    assertEquals(1, indexMap.get("last_name"));
    assertEquals(2, indexMap.get("company_name"));

    String[] emailArgs = {"--email", "--email-template", emailTemplateFile.getPath(), "--output-dir", tempDir.getPath(),"--csv-file",csvFile.getPath()};
    assertDoesNotThrow(() -> Reader.processTemplates(new CommandLineParser(emailArgs), read1, indexMap));

    String[] invalidEmailArgs = {"--output-dir", tempDir.getPath(),"--csv-file",csvFile.getPath(),"--email","--email-template"};
    assertThrows(InvalidArgumentException.class, () -> Reader.processTemplates(new CommandLineParser(invalidEmailArgs), read1, indexMap));

  }







}