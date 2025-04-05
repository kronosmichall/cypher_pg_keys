package com.example;

import org.neo4j.procedure.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.ws.rs.core.Context;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyProcedure {
    @Context
    public org.neo4j.graphdb.GraphDatabaseService db;

    @Context
    public org.neo4j.logging.Log log;
    
    @Procedure(name = "example.hello", mode = Mode.READ)
    @Description("Returns a simple greeting.")
    public Stream<Greeting> hello() {
        return Stream.of(new Greeting("Hello, Neo4j!"));
    }

    public String readFileAndConvert(String path) throws Exception {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String pgQuery = new String(encoded);
        return convertPgToCypher(pgQuery);
    }

    public String convertPgToCypher(String pgQuery) throws Exception {
        Pattern pattern = Pattern.compile(
            "FOR\\s*\\((.*?)\\)\\s*" +
            "IDENTIFIER\\s*(.*?)\\s*" +
            "WITHIN\\s*\\((.*?)\\)\\."
        );

        Matcher matcher = pattern.matcher(pgQuery);

        if (matcher.find()) {
            String forClause = matcher.group(1).trim();
            String identifierClause = matcher.group(2).trim();
            String withinClause = matcher.group(3).trim();

            // Print individual components
            System.out.println("FOR Clause: " + forClause);
            System.out.println("IDENTIFIER Clause: " + identifierClause);
            System.out.println("WITHIN Clause: " + withinClause);


            String format = """
                MATCH %s
                RETURN COLLECT {
                    MATCH %s
                    RETURN %s
                } as result;
            """; 
            return String.format(format, forClause, withinClause, argsToDictString(identifierClause));
        } else {
            System.out.println("Query format is invalid or does not match.");
            throw new Exception("Input query does not match pg_keys patterns");
        }
    }

    public String argsToDictString(String args) {
        String[] elements = args.split(",\\s*");

        elements = Stream.of(elements)
            .map(w -> w.trim())
            .map(w ->  w.replaceAll("\\.", "") + ":" + w)
            .toArray(String[]::new);

        String result = "{" + String.join(", ", elements) + "}";
        return result;
    }

    public static class Greeting {
        public String message;

        public Greeting(String message) {
            this.message = message;
        }
    }


}
