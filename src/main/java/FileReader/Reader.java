package FileReader;

import static FileWriter.TemplateProcessor.outputFiles;

import CommandLine.CommandLineParser;
import CommandLine.InvalidArgumentException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Reader class provides functionality for processing CSV files and templates
 * to generate emails or letters based on the input data.
 */
public class Reader {


  /**
   * Reads the content of a file and returns it as a list of strings.
   *
   * @param filePath String representing the path to the file.
   * @return List<String> containing the lines of the file.
   * @throws IOException if an I/O error occurs while reading the file.
   */
  protected static List<String> readFileContent(String filePath) throws IOException {
    List<String> fileContent = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        fileContent.add(line);
      }
    }
    if (fileContent.isEmpty()) {
      throw new IllegalArgumentException("CSV file is empty");
    }

    return fileContent;
  }


  /**
   * Parses a line of CSV content.
   *
   * @param line String representing a line of CSV content.
   * @return List<String> containing the parsed values.
   */
  protected static List<String> parseCSVLine(String line) {
    List<String> values = new ArrayList<>();
    String regex = "\"([^\"]*?)\"";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(line);
    while (matcher.find()) {
      String value = matcher.group(1);
      values.add(value);
    }
    return values;
  }


  /**
   * Parses the content of the CSV file into a map.
   *
   * @param fileContent List<String> containing the lines of the CSV file.
   * @return Map<Integer, List<String>> representing the content of the CSV file. Integer represents
   * the number of the customer, and List<String> represents customer information.
   */
  protected static Map<Integer, List<String>> parseCSVContent(List<String> fileContent) {
    Map<Integer, List<String>> mapPeople = new HashMap<>();
    int peopleNum = 0;

    for (String line : fileContent) {
      List<String> values = parseCSVLine(line);
      mapPeople.put(peopleNum, values);
      peopleNum++;
    }
    return mapPeople;
  }



  /**
   * Reads the content of the CSV file and parses it into a map.
   *
   * @param filePath String representing the path to the CSV file.
   * @return Map<Integer, List<String>> representing the content of the CSV file.
   * @throws IOException if an I/O error occurs while reading the file.
   */
  public static Map<Integer, List<String>> readCSV(String filePath) throws IOException {
    List<String> fileContent = readFileContent(filePath);

    return parseCSVContent(fileContent);
  }


  /**
   * Reads the content of a template file.
   *
   * @param filePath String representing the path to the template file.
   * @return String containing the content of the template file.
   * @throws IOException if an I/O error occurs while reading the file.
   */
  public static String readTemplate(String filePath) throws IOException {
    StringBuilder content = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {

        content.append(line).append(System.lineSeparator());
      }
    }
    if (content.isEmpty()) {
      throw new IllegalArgumentException("Template file is empty");
    }
    return content.toString();
  }

  /**
   * Creates a map of header values to their column indices for extensibility
   *
   * @param mapPeople Map<Integer, List<String>> representing CSV data.
   * @return Map<String, Integer> mapping CSV header values to their column indices.
   */
  public static Map<String, Integer> headerIndex(Map<Integer, List<String>> mapPeople){
    List<String> header = mapPeople.get(0);
    Map<String, Integer> indexMap = new HashMap<>();
    int index = 0;
    for (String s : header) {
      indexMap.put(s, index);
      index++;
    }
    return indexMap;
  }
  /**
   * Processes email and/or letter template(s) based on the command line options provided.
   *
   * @param parser      CommandLineParser object containing parsed command line options.
   * @param csvContent  Map<Integer, List<String>> representing the content of the CSV file.
   * @param headerMap   Map<String, Integer> mapping CSV header values to their column indices.
   * @throws IOException if an I/O error occurs while processing files.
   */
  protected static void processTemplates(CommandLineParser parser, Map<Integer, List<String>> csvContent, Map<String, Integer> headerMap)
      throws IOException {

    if (parser.hasOption("--email") && new File(parser.getPath("--email-template")).exists()) {
      String outputDir = parser.getPath("--output-dir");
      String emailTemplateFilePath = parser.getPath("--email-template");
      String emailTemplateContent = readTemplate(emailTemplateFilePath);
      outputFiles(csvContent, emailTemplateContent, outputDir, headerMap, "email");
    }

    if (parser.hasOption("--letter") && new File(parser.getPath("--letter-template")).exists()) {
      String outputDir = parser.getPath("--output-dir");
      String letterTemplateFilePath = parser.getPath("--letter-template");
      String letterTemplateContent = readTemplate(letterTemplateFilePath);
      outputFiles(csvContent, letterTemplateContent, outputDir, headerMap, "letter");
    }

  }


  /**
   * Processes the CSV file provided in the command line arguments to generate
   * output files (emails or letters) based on the data.
   *
   * @param args String array containing command line arguments.
   * @throws InvalidArgumentException if there are issues with the provided command line arguments.
   * @throws IOException              if an I/O error occurs while processing files.
   */
  public static void processFiles(String[] args) throws InvalidArgumentException, IOException {

      CommandLineParser parser = new CommandLineParser(args);
      if (parser.validateParser(parser) && new File(parser.getPath("--csv-file")).exists()) {
        String csvFilePath = parser.getPath("--csv-file");
        Map<Integer, List<String>> csvContent = readCSV(csvFilePath);
        Map<String, Integer> headerMap = headerIndex(csvContent);
        processTemplates(parser, csvContent, headerMap);
      }
  }

}