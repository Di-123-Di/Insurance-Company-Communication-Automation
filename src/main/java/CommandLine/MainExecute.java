package CommandLine;
import FileReader.Reader;
import java.io.IOException;

public class MainExecute {
    public static void main(String[] args) throws InvalidArgumentException, IOException {
      Reader.processFiles(args);
    }
  }
