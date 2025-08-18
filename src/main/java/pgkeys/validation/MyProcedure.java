package pgkeys.validation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import pgkeys.validation.parser.antlr4.PGKeysLexer;
import pgkeys.validation.parser.antlr4.PGKeysParser;
import pgkeys.validation.parser.listener.PGKeysListenerImpl;
import pgkeys.validation.parser.listener.Query;

public class MyProcedure {
    @org.neo4j.procedure.Context
    public Transaction tx;

    @org.neo4j.procedure.Context
    public Log log;

    @Procedure(name = "pgkeys.validateDetailed", mode = Mode.READ)
    @Description("Validates PG-keys schema queries and returns detailed results with query information.")
    public Stream<Result> validateDetailed(@Name("schemaPath") String schemaName) {
        List<Query> queries;
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(schemaName));
            String pgQuery = new String(encoded);
            queries = fromString(pgQuery);
        } catch (Exception e) {
            log.error("Failed to read schema ", e);
            throw new IllegalArgumentException("No schema %s exists; %s; %s".formatted(schemaName, e.getMessage(), e));
        }
        
        return queries.parallelStream().flatMap(query -> {
            try {
                String cypherQuery = convertPgToCypher(query);
                log.info("Running pg_keys query: \n%s", cypherQuery);
                return tx.execute(cypherQuery).stream()
                    .flatMap(qres -> Result.fromQueryResult(qres, cypherQuery, query.stringQuery));
            } catch (Exception e) {
                log.error("Failed to convert or execute query", e);
                throw new RuntimeException("Failed to process query: " + query.stringQuery, e);
            }
        });
    }

    public static class Result {
        public Object result;
        public String query;
        public String originalQuery;

        public Result(Object result, String query, String originalQuery) {
            this.result = result;
            this.query = query;
            this.originalQuery = originalQuery;
        }

        public static Stream<Result> fromQueryResult(Map<String, Object> qres, String query, String originalQuery) {
            return qres.values().stream().map(value -> new Result(value, query, originalQuery));
        }
    }


    public String convertPgToCypher(Query query) throws Exception {
        String format = """
                MATCH {forClause}
                OPTIONAL MATCH {withinClause}
                WITH {mainVar}, COLLECT({
                {identifierParams}
                }) as entries
                CALL {
                    WITH {mainVar}, entries
                    MATCH ({mainVar2}:{mainVarLabel})
                    MATCH {withinClause2}
                    WHERE id({mainVar2}) < id({mainVar})
                    WITH {mainVar}, entries, {mainVar2}, COLLECT({
                    {identifierParams2}
                    }) as entries2
                    return entries2
                }
                WITH entries, [e in entries where e in entries2] as intersection
                WITH collect(entries) as centries, collect(intersection) as cintersection
                {returnClause}
                """;

        String mainVar = query.mainVar;
        String mainVar2 = "__internal__" + mainVar;
        String mainVarLabel = query.mainVarLabel;
        String identifierParams = argsToDictString(query.restrictorClause);
        String withinClause2 = replaceWholeWord(query.whereClause, mainVar, mainVar2);
        String identifierParams2 = replaceWholeWord(identifierParams, mainVar, mainVar2);
        String returnClause = returnClause(Restrictor.fromString(query.restrictor));

        Map<String, Object> params = Map.of(
                "forClause", query.forClause(),
                "withinClause", query.whereClause,
                "identifierParams", identifierParams,
                "mainVar", mainVar,
                "mainVar2", mainVar2,
                "mainVarLabel", mainVarLabel,
                "withinClause2", withinClause2,
                "identifierParams2", identifierParams2,
                "returnClause", returnClause
        );
        String result = new StringFormatter(format).format(params);
        log.info("Generated query: \n%s\n", result);

        return result;
    }

    private String returnClause(Restrictor restrictor) {
        switch (restrictor) {
            case IDENTIFIER -> {
                return "return all(e in centries where size(e) = 1) AND all(e in cintersection where size(e) = 0)";
            }
            case EXCLUSIVE_MANDATORY -> {
                return "return all(e in centries where size(e) >= 1) AND all(e in cintersection where size(e) = 0)";
            }
            case EXCLUSIVE_SINGLETON -> {
                return "return all(e in centries where size(e) <= 1) AND all(e in cintersection where size(e) = 0)";
            }
            case EXCLUSIVE -> {
                return "all(e in cintersection where size(e) = 0)";
            }
            case MANDATORY -> {
                return "return all(e in centries where size(e) >= 1)";
            }
            case SINGLETON -> {
                return "return all(e in centries where size(e) <= 1)";
            }
            default -> throw new IllegalStateException("Unexpected value: " + restrictor);
        }
    }

    private String replaceWholeWord(String text, String word, String replacement) {
        // Use word boundaries to ensure we only replace whole words
        // This regex will match the word only when it's not preceded or followed by word characters
        String regex = "\\b" + Pattern.quote(word) + "\\b";
        return text.replaceAll(regex, replacement);
    }

    private String argsToDictString(String args) {
        String[] elements = args.split(",\\s*");
        elements = Stream.of(elements)
                .map(String::trim)
                .map(w -> w.replaceAll("\\.", "") + ":" + w)
                .map(w -> "\t" + w)
                .toArray(String[]::new);
        return String.join(",\n", elements);
    }

    private static class StringFormatter {
        String str;

        StringFormatter(String str) {
            this.str = str;
        }

        String format(Map<String, Object> params) {
            String str2 = new String(str);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                str2 = str2.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
            return str2;
        }
    }

   List<Query> fromString(String schemaStr) {
        var charStream = CharStreams.fromString(schemaStr);
        var lexer = new PGKeysLexer(charStream);
        var tokens = new CommonTokenStream(lexer);

        var parser = new PGKeysParser(tokens);
        var tree = parser.schema();
        
        var walker = new ParseTreeWalker();
        var listener = new PGKeysListenerImpl();
        walker.walk(listener, tree);

        return listener.getQueries();
    }
}
