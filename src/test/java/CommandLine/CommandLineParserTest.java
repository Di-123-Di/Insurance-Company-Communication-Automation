package CommandLine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class CommandLineParserTest {
  private CommandLineParser testparser;
  @BeforeEach
  void setUp() throws InvalidArgumentException {
    testparser = new CommandLineParser(new String[]{});
  }
  @Test
  void testProcessArgsValid() throws InvalidArgumentException {
    String[] args = {
        "--email",
        "--email-template", "template.txt",
        "--letter",
        "--letter-template", "letter.txt",
        "--output-dir", "/path/to/output",
        "--csv-file", "data.csv"
    };


    CommandLineParser parser = new CommandLineParser(args);

    assertTrue(parser.hasOption("--email"));
    assertEquals("N/A", parser.getPath("--email"));
    assertTrue(parser.hasOption("--letter"));
    assertEquals("N/A", parser.getPath("--letter"));
    assertTrue(parser.hasOption("--output-dir"));

    assertFalse(parser.hasOption("--letter-template"));
    assertFalse(parser.hasOption("--email-template"));

    String[] args2 = {"--output-dir", "/path/to/output",
        "--csv-file", "data.csv"
    };
    CommandLineParser parser1 = new CommandLineParser(args2);
    assertTrue(parser1.hasOption("--output-dir"));

    assertFalse(parser.hasOption("--email-template"));
    assertEquals(null, parser.getPath("--email-template"));
    assertFalse(parser.hasOption("--letter-template"));
    assertEquals(null, parser.getPath("--letter-template"));

    assertTrue(parser.hasOption("--output-dir"));
    assertEquals("/path/to/output", parser.getPath("--output-dir"));
    assertFalse(parser.hasOption("--csv-file"));
    assertEquals(null, parser.getPath("--csv-file"));

    String[] args3 = {"--output-dir", "path/to/output",
        "--csv-file", "/path/to/folder/data.csv",  "--letter",
        "--letter-template", "/path/to/folder/letter.txt",

    };
    CommandLineParser parser2 = new CommandLineParser(args3);
    assertFalse(parser2.hasOption("--output-dir"));
    assertTrue(parser2.hasOption("--letter-template"));
    assertTrue(parser2.hasOption("--csv-file"));
  }

  @Test
  void testProcessArgsInvalid() throws InvalidArgumentException{
    String[] args = {
        "--email",
        "--email-template", "template.txt",
        "--letter",
        "--letter-template", "letter.txt",
        "--output-dir", "/path/to/output",
        "--csv-file"
    };
    assertThrows(InvalidArgumentException.class, ()->new CommandLineParser(args));

    String[] args1 = {
        "--email",
        "--email-template", "template.doc",
        "--letter",
        "--letter-template", "letter.txt",
        "--output-dir", "/path/to/output",
        "--csv-file", "csv.txt"
    };

    CommandLineParser cmd1 = new CommandLineParser(args1);
    assertFalse(cmd1.validateParser(cmd1));

//
    String[] args2 = {
        "--email",
        "--email-template", "/path/to/output/template.txt",
        "--letter",
        "--letter-template",
        "--output-dir", "/path/to/output",
        "--csv-file" // Missing value for CSV file
    };

    assertThrows(InvalidArgumentException.class, () -> {
      new CommandLineParser(args2);
    });


    String[] args3 = {
        "--email",
        "--email-template"
    };

    assertThrows(InvalidArgumentException.class, () -> {
      new CommandLineParser(args3);
    });


    String[] args4 = {
        "--letter",
        "--letter-template"
    };

    assertThrows(InvalidArgumentException.class, () -> {
      new CommandLineParser(args4);
    });

    String[] args5 = {
        "--output-dir"

    };

    assertThrows(InvalidArgumentException.class, () -> {
      new CommandLineParser(args5);
    });

    String[] args6 = {
        "--csv-file"

    };

    assertThrows(InvalidArgumentException.class, () -> {
      new CommandLineParser(args6);
    });

  }
//
  @Test
  void testParserInvalidPath() throws InvalidArgumentException{
    String[] args = {
        "--email",
        "--email-template", "template.pdf",
        "--letter",
        "--letter-template", "letter.txt",
        "--output-dir", "/path/to/output",
        "--csv-file" // Missing value for CSV file
    };

//    CommandLineParser c = new CommandLineParser(args);

    assertThrows(InvalidArgumentException.class, ()->new CommandLineParser(args));

  }
  @Test
  void hasOption() {
    assertFalse(testparser.hasOption("--email"));


    testparser.getOptionPath().put("--email", "N/A");
    assertTrue(testparser.hasOption("--email"));
  }

  @Test
  void getPath() {
    assertNull(testparser.getPath("--email"));

    testparser.getOptionPath().put("--email", "path/to/email");
    assertEquals("path/to/email", testparser.getPath("--email"));
  }

  @Test
  void getOptionPath() {
    assertTrue(testparser.getOptionPath().isEmpty());

    // Test when options are added to the parser
    testparser.getOptionPath().put("--email", "path/to/email");
    testparser.getOptionPath().put("--output-dir", "path/to/output");
    assertFalse(testparser.getOptionPath().isEmpty());
    assertEquals(2, testparser.getOptionPath().size());
  }

  @Test
  void validateParser() throws InvalidArgumentException {
    assertFalse(testparser.validateParser(testparser));

    // Test when the parser has required options
    testparser.getOptionPath().put("--output-dir", "/path/to/output");
    assertFalse(testparser.validateParser(testparser));
    testparser.getOptionPath().put("--csv-file", "/path/to/csv");
    assertFalse(testparser.validateParser(testparser));


    testparser.getOptionPath().put("--email", "N/A");
    assertFalse(testparser.validateParser(testparser));

    testparser.getOptionPath().put("--letter", "N/A");
   System.out.println("optionpath has: "+ testparser.getOptionPath());
    assertFalse(testparser.validateParser(testparser));

    String[] t = {};
    CommandLineParser testParser2 = new CommandLineParser(t);
    testParser2.getOptionPath().put("--letter", "N/A");
    assertFalse(testParser2.validateParser(testParser2));

    String[] p = {};
    CommandLineParser testParser3 = new CommandLineParser(p);
    testParser3.getOptionPath().put("--output-dir", "/path/to/output");
    testParser3.getOptionPath().put("--csv-file", "/path/to/output.txt");
    assertFalse(testParser3.validateParser(testParser3));
  }

  @Test
  void isValidFilePath() {

    assertTrue(CommandLineParser.isValidFilePath("/Users/zoegong/Downloads/hw8_9.05/ama.txt"));


    // Test invalid file paths
    assertFalse(CommandLineParser.isValidFilePath("/invalidpath/file.pdf"));
    assertFalse(CommandLineParser.isValidFilePath("/home/user/file.docs"));
  }

  @Test
  void isValidFolderPath(){
    assertTrue(CommandLineParser.isValidFolderPath("/Users/zoegong/Downloads/hw8_9.05"));
    String s = null;
    assertFalse(CommandLineParser.isValidFolderPath(s));
    assertFalse(CommandLineParser.isValidFilePath(s));
  }
}