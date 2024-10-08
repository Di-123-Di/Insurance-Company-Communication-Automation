package FileWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to process templates and generate output files based on CSV data.
 */
public class TemplateProcessor {

  /**
   * Method to generate specific files based on provided CSV content and template.
   *
   * @param csvContent   Map<Integer, List<String>>, content of CSV file represented as map of rows.
   * @param templateContent   String, content of the template to be processed.
   * @param outputDir   String, directory where output files will be saved.
   * @param headerMap   Map<String, Integer>, mapping of CSV header values to their column indices.
   * @param type   String, the type of file to be generated.
   * @throws IOException   if an I/O error occurs.
   */
  public static void outputFiles(Map<Integer, List<String>> csvContent,
      String templateContent,
      String outputDir, Map<String, Integer> headerMap, String type) throws IOException {


    createOutputDirectory(outputDir);

    int numRows = csvContent.size();
    for (int i = 1; i < numRows; i++) {
      List<String> rowData = csvContent.get(i);
      String processedContent = processTemplate(templateContent, rowData, headerMap);
      String outputFile = generateOutputFilePath(outputDir, type, i);
      writeOutputFile(outputFile, processedContent);
    }
  }

  /**
   * Creates the output directory if it doesn't exist.
   *
   * @param outputDir   String, directory path to be created.
   * @throws IOException   if directory creation fails.
   */
  static void createOutputDirectory(String outputDir) throws IOException {
    File directory = new File(outputDir);
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new IOException("Failed to create output directory: " + outputDir);
      }
    }
  }

  /**
   * Generates the output file path based on directory, type, and index.
   *
   * @param outputDir   String, directory path where output file will be saved.
   * @param type   String, the type of file.
   * @param index   int, index of the file.
   * @return String, the generated output file path.
   */
  static String generateOutputFilePath(String outputDir, String type, int index) {
    return outputDir + File.separator + type + " " + index + ".txt";
  }


  /**
   * Processes the template content by replacing placeholders with corresponding CSV data.
   *
   * @param template   String, the template content.
   * @param rowData   List<String>, the data of a CSV row.
   * @param headerMap   Map<String, Integer>, mapping of CSV header values to their column indices.
   * @return String, the processed template content.
   */
  static String processTemplate(String template, List<String> rowData,
      Map<String, Integer> headerMap) {

    List<String> placeholders = findAll(template);
    String copyTemplate = template;
    for (String placeholder : placeholders) {
      Integer index = headerMap.get(placeholder);
      if (index != null && index < rowData.size()) {
        String replaceVal = rowData.get(index);
        copyTemplate = copyTemplate.replace("[[" + placeholder + "]]", replaceVal);
      }
    }

    return copyTemplate;
  }

  /**
   * Finds all placeholders in the given input using regular expression.
   *
   * @param input   String, the input to search for placeholders.
   * @return List<String>, a list of found placeholders.
   */
  public static List<String> findAll(String input){
    List<String> matches = new ArrayList<>();
    Pattern pattern = Pattern.compile("\\[\\[([^\\[\\]]*?)\\]\\]");
    Matcher matcher = pattern.matcher(input);

    while(matcher.find()){
      matches.add(matcher.group(1));
    }
    if (matches.isEmpty()) {
      throw new IllegalArgumentException("No placeholders found in the input string.");
    }
    return matches;
  }

  /**
   * Writes the content to the specified file path.
   *
   * @param filePath   String, the path of the file to write.
   * @param content   String, the content to write to the file.
   * @throws IOException   if an I/O error occurs.
   */
  static void writeOutputFile(String filePath, String content) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      writer.write(content);
    }
  }
}
