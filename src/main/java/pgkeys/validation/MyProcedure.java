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
    @Description("Returns a simple greeting.")
    public Stream<Result> validateDetailed(@Name("schemaPath") String schemaName) {
        Stream<String> cypher;
        try {
            cypher = readFileAndConvert(schemaName);
        } catch (Exception e) {
            log.error("Failed to read schema ", e);
            throw new IllegalArgumentException("No schema %s exists; %s; %s".formatted(schemaName, e.getMessage(), e));
        }
        log.info("Running pg_keys query: \n%s", cypher);

        return cypher.flatMap(q -> tx.execute(q).stream()).flatMap(Result::fromQueryResult);
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

    public Stream<String> readFileAndConvert(String path) throws Exception {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String pgQuery = new String(encoded);
        List<Query> queries = fromString(pgQuery);

        return queries.stream().map(q -> {
            try {
                return convertPgToCypher(q);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String convertPgToCypher(Query query) throws Exception {
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
                    WHERE id(x2) < id({mainVar})
                    WITH {mainVar}, entries, x2, COLLECT({
                    {identifierParams2}
                    }) as entries2
                    return entries2
                }
                WITH entries, [e in entries where e in entries2] as intersection
                WITH collect(entries) as centries, collect(intersection) as cintersection
                {returnClause}
                """;

        String mainVar = query.forClause().split(":")[0];
        String mainVarLabel = query.forClause().split(":")[1];
        String identifierParams = argsToDictString(query.restrictorClause);
        String withinClause2 = query.withinClause().replace(mainVar, "x2");
        String identifierParams2 = identifierParams.replace(mainVar, "x2");
        String returnClause = returnClause(Restrictor.fromString(query.restrictor));

        Map<String, Object> params = Map.of(
                "forClause", query.forClause(),
                "withinClause", query.withinClause(),
                "identifierParams", identifierParams,
                "mainVar", mainVar,
                "mainVarLabel", mainVarLabel,
                "withinClause2", withinClause2,
                "identifierParams2", identifierParams2,
                "returnClause", returnClause
        );
        return new StringFormatter(format).format(params);
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

    private static List<Query> fromString(String schemaStr) {
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
