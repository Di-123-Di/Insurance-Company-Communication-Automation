package CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

/**
 * Processes and validates command line arguments. This class doesn't *do* anything with the values provided by the user
 * beyond initial validation. It is another class' responsibility to determine what to do with the user input.
 */
public class CommandLineParser {
  private static final String EMAIL_COM = "--email";
  private static final String EMAIL_TEMP = "--email-template";
  private static final String LETTER_COM = "--letter";

  private static final String LETTER_TEP = "--letter-template";
  private static final String OUTPUT_DIR = "--output-dir";

  private static final String CSV_FILE = "--csv-file";
  private static final String FILE_PATH_REGEX ="^(?:[a-zA-Z]:|\\\\\\\\)?(?:\\\\|/[^\\\\/:*?\"<>|]+)+(\\\\|/)?[^\\\\/:*?\"<>|]*\\.(csv|txt)?$";

  private static final String FOLDER_PATH_REGEX = "^(?:\\/(?:[^\\/]+\\/)*)(?:[^\\/]+)$";
  // Compile the regex pattern

  //Key: option, Value: if possible, the file to path
  private Map<String, String> optionPath ;

  public CommandLineParser(String[] args) throws InvalidArgumentException {
    optionPath = new HashMap<>();

    this.processArgs(args);
  }

  /**
   * Process the arguments and if the command has path to file followed, record the path to the map optionPath
   * @param args
   * @throws InvalidArgumentException
   */

  private void processArgs(String[] args) throws InvalidArgumentException {

    for (int i=0; i<args.length; ++i){

        String currMeg = args[i];
        if (currMeg.equals(EMAIL_COM)){
          optionPath.put(EMAIL_COM, "N/A");
        }
        else if (currMeg.equals(EMAIL_TEMP)){
          if (i+1<args.length){
            if (isValidFilePath(args[i+1])){
              optionPath.put(EMAIL_TEMP, args[i+1]);

            }
            else{
              System.out.println("Not valid path to file for "+EMAIL_TEMP);

            }
            i++;
          }
          else{
            throw new InvalidArgumentException("Missing value for -- email template or its path to file");
          }
        }
        else if (currMeg.equals(LETTER_COM)){
          optionPath.put(LETTER_COM, "N/A");
        }
        else if (currMeg.equals(LETTER_TEP)){
          if (i+1<args.length){
            if (isValidFilePath(args[i+1])){
              optionPath.put(LETTER_TEP, args[i+1]);
            }
            else{
              System.out.println("Not valid path to file for "+LETTER_TEP);

            }
            i++;
          }
          else{
            throw new InvalidArgumentException("Missing value for -- letter template or its path to file");
          }
        }

        else if (currMeg.equals(OUTPUT_DIR)){
          if (i+1<args.length){

            if (isValidFolderPath(args[i+1])){
              optionPath.put(OUTPUT_DIR, args[i+1]);

            }
            else{
              System.out.println("Not valid path to folder for "+OUTPUT_DIR);

            }
            i++;
          }
          else{
            throw new InvalidArgumentException("Missing value for -- output direction or its path to folder");
          }
        }

        else if (currMeg.equals(CSV_FILE)){
          if (i+1<args.length){
            if (isValidFilePath(args[i+1])){
              optionPath.put(CSV_FILE, args[i+1]);

            }
            else{
              System.out.println("Not valid path to file for "+CSV_FILE);

            }
            i++;
          }
          else{
            throw new InvalidArgumentException("Missing value for -- csv file or its path to file");
          }
        }
        else{
          System.out.println("Invalid option, please re-enter");

        }

    }

  }

  /**
   * Check if the command args has the required option
   * @param option - the required option
   * @return boolean value
   */
  public boolean hasOption(String option){
    return optionPath.containsKey(option);
  }

  /**
   * Get the path to file/path to folder for the corresponding option,
   * @param option (String) the user input option
   * @return The string with the path to file/folder
   */
  public String getPath(String option){
    return optionPath.get(option);
  }

  /**
   * Helper method for getting the map of option path
   * @return Map<> optionPath, key: command, value: path to file
   */
  public Map<String, String> getOptionPath() {
    return optionPath;
  }

  /**
   * Helper method for printing the instruction of the program
   */
  private void printInstruction(){
    System.out.println(
        System.lineSeparator() +
            "Usage Instruction:" + System.lineSeparator() +
            "We only accept absolute path to file/folder to path in Unix style" + System.lineSeparator() +
            "--email Generate email messages. If this option is provided, then -- email-template must also be provided." + System.lineSeparator() +
            "--email-template <path/to/file> A filename for the email template. --letter Generate letters. If this option is provided, then --letter- template must also be provided." + System.lineSeparator() +
            "--letter-template <path/to/file> A filename for the letter template. --output-dir <path/to/folder> The folder to store all generated files. This option is required." + System.lineSeparator() +
            "--csv-file <path/to/folder> The CSV file to process. This option is required." + System.lineSeparator() +
            "Examples:" + System.lineSeparator() +
            "--email --email-template email-template.txt --output-dir emails -- csv-file customer.csv" + System.lineSeparator() +
            "--letter --letter-template letter-template.txt --output-dir letters - -csv-file customer.csv");

  }

  /**
   * Helper function to check the presence of required arguments and matched compatible options
   * @param parser (CommandLineParser)
   * @return boolean value
   */
  public boolean validateParser(CommandLineParser parser){
    if (parser.hasOption("--email") && !parser.hasOption("--email-template")){
      System.out.println("Incorrect: --email provided but no --email-template was given.");
      printInstruction();
      return false;
    }
    else if (parser.hasOption("--letter") && !parser.hasOption("--letter-template")){
      System.out.println("Incorrect: --letter provided but no --letter-template was given.");
      printInstruction();
      return false;
    }
    else if (!parser.hasOption("--email") &&!parser.hasOption("--letter")){
      System.out.println("Incorrect: --email and --letter are missing!");
      printInstruction();
      return false;
    }
    else if (!parser.hasOption("--output-dir")){
      System.out.println("Incorrect: --output-dir is missing");
      printInstruction();
      return false;
    }

    else if (!parser.hasOption("--csv-file")){
      System.out.println("Incorrect: --csv-file is missing");
      printInstruction();
      return false;
    }

    return true;
  }

  /**
   * Helper method for checking if the path to file is valid in terms of format (unix style)
   * @param filePath : String of unix style path to file
   * @return boolean value
   */
  public static boolean isValidFilePath(String filePath) {
    if (filePath==null) return false;
    Pattern pattern = Pattern.compile(FILE_PATH_REGEX);
    Matcher matcher = pattern.matcher(filePath);
    return matcher.matches();
  }
  /**
   * Helper method for checking if the path to folder is valid in terms of format (unix style)
   * @param folderPath : String of unix style path to folder
   * @return boolean value
   */
  public static boolean isValidFolderPath(String folderPath){
    if (folderPath==null){

      return false;
    }
    Pattern pattern = Pattern.compile(FOLDER_PATH_REGEX);
    Matcher matcher = pattern.matcher(folderPath);
    return matcher.matches();
  }


}
