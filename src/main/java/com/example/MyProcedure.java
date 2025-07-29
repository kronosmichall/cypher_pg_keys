package com.example;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.ws.rs.core.Context;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.ResultTransformer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class MyProcedure {
    @org.neo4j.procedure.Context
    public Transaction tx;

    @org.neo4j.procedure.Context
    public Log log;

    @Procedure(name = "pgkeys.validateDetailed", mode = Mode.READ)
    @Description("Returns a simple greeting.")
    public Stream<Result> validateDetailed(@Name("schemaPath") String schemaName) {
         String cypher;
         try {
             cypher = readFileAndConvert(schemaName);
         } catch (Exception e) {
             log.error("Failed to read schema ", e);
             throw new IllegalArgumentException("No schema %s exists; %s; %s".formatted(schemaName, e.getMessage(), e));
         }
         log.info("Running pg_keys query: \n%s", cypher);

         return tx.execute(cypher).stream().flatMap(Result::fromQueryResult);
    }

    public static class Result {
        public Object result;

        public Result(Object obj) {
            this.result = obj;
        }

        public static Stream<Result> fromQueryResult(Map<String, Object> qres) {
            return qres.values().stream().map(Result::new);
        }
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
            "WITHIN\\s*(\\(.*?\\))\\."
        );

        System.out.println(pgQuery);
        var matcher = pattern.matcher(pgQuery);

        if (matcher.find()) {
            var forClause = matcher.group(1).trim();
            var identifierClause = matcher.group(2).trim();
            var withinClause = matcher.group(3).trim();

            // Print individual components
            System.out.println("FOR Clause: " + forClause);
            System.out.println("IDENTIFIER Clause: " + identifierClause);
            System.out.println("WITHIN Clause: " + withinClause);


            String format = """
            MATCH ({forClause})
            OPTIONAL MATCH {withinClause}
            WITH {mainVar}, COLLECT({
            {identifierParams}
            }) as entries
            CALL {
                WITH {mainVar}, entries
                MATCH (x2:{mainVarLabel})
                MATCH {withinClause2} // tu bez optional bo przecięcia z pustymi zbiorami nie są potrzebne
                WHERE id(x2) <> id({mainVar})
                WITH {mainVar}, entries, x2, COLLECT({
                {identifierParams2}
                }) as entries2
                return entries2
            }
            WITH entries, [e in entries where e in entries2] as intersection
            WITH collect(entries) as centries, collect(intersection) as cintersection
            return all(e in centries where size(e) = 1) AND all(e in cintersection where size(e) = 0)
            """; 

            String mainVar = forClause.split(":")[0];
            String mainVarLabel = forClause.split(":")[1];
            String identifierParams = argsToDictString(identifierClause);
            String withinClause2 = withinClause.replace(mainVar, "x2");
            String identifierParams2 = identifierParams.replace(mainVar, "x2");

            Map<String, Object> params = Map.of(
                "forClause", forClause,
                "withinClause", withinClause,
                "identifierParams", identifierParams,
                "mainVar", mainVar,
                "mainVarLabel", mainVarLabel,
                "withinClause2", withinClause2,
                "identifierParams2", identifierParams2
            );
            return new StringFormatter(format).format(params);
        } else {
            System.out.println("Query format is invalid or does not match.");
            throw new Exception("Input query does not match pg_keys patterns");
        }
    }

    public String argsToDictString(String args) {
        String[] elements = args.split(",\\s*");
        elements =  Stream.of(elements)
            .map(String::trim)
            .map(w ->  w.replaceAll("\\.", "") + ":" + w)
            .map(w -> "\t" + w)
            .toArray(String[]::new);
        return String.join(",\n", elements);
    }

    public static class StringFormatter {
        public String str;

        public StringFormatter(String str) {
            this.str = str;
        }

        public String format(Map<String, Object> params) {
            String str2 = new String(str);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                str2 = str2.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
            return str2;
        }
    } 
}
