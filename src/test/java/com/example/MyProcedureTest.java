package com.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.tuple.Pair;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MyProcedureTest extends TestCase {
    static String testDirectory = "src/test/java/com/example/";
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MyProcedureTest( String testName ) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(MyProcedureTest.class);
    }

    public Pair<String, String> inOutFiles(String directory) {{
        try {
            String inputFilePath = directory + "/in.txt";
            String outputFilePath = directory + "/out.txt";

            String inputContent = new String(Files.readAllBytes(Paths.get(inputFilePath)), StandardCharsets.UTF_8);
            String outputContent = new String(Files.readAllBytes(Paths.get(outputFilePath)), StandardCharsets.UTF_8);

            return Pair.of(inputContent, outputContent);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error reading files from directory: " + directory, e);
        }
    }}

    public void testConvertPgToCypher() {
        MyProcedure p = new MyProcedure();

        Pair<String, String> inOut = inOutFiles(testDirectory + "testConvertPgToCypher");
        String inStr = inOut.getLeft();
        String outStr = inOut.getRight();

        try {
            String result = p.convertPgToCypher(inStr);
            assertEquals("wrong", outStr.trim(), result.trim());
        } catch (Exception e) {
            fail("Exception occurred during test execution: " + e.getMessage());
        }
    }
}
