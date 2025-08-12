package pgkeys.validation;

import pgkeys.validation.parser.listener.PGKeysListenerImpl;
import pgkeys.validation.parser.listener.Query;
import pgkeys.validation.parser.antlr4.PGKeysLexer;
import pgkeys.validation.parser.antlr4.PGKeysParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import java.util.List;

public class TestParser {
    public static void main(String[] args) {
        String testQuery = """
FOR (c:City)
EXCLUSIVE MANDATORY
c.id
WITHIN something.
""";

        try {
            var charStream = CharStreams.fromString(testQuery);
            var lexer = new PGKeysLexer(charStream);
            var tokens = new CommonTokenStream(lexer);
            var parser = new PGKeysParser(tokens);
            
            // Add error handling
            parser.removeErrorListeners();
            parser.addErrorListener(new org.antlr.v4.runtime.BaseErrorListener() {
                @Override
                public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer,
                                        Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg, org.antlr.v4.runtime.RecognitionException e) {
                    System.err.println("Syntax error at line " + line + ":" + charPositionInLine + " - " + msg);
                }
            });
            
            var tree = parser.schema();
            
            System.out.println("Parse tree: " + tree.toStringTree(parser));
            System.out.println("Number of children: " + tree.getChildCount());

            var walker = new ParseTreeWalker();
            var listener = new PGKeysListenerImpl();
            walker.walk(listener, tree);

            List<Query> queries = listener.getQueries();
            
            System.out.println("SUCCESS! Parsed " + queries.size() + " queries:");
            for (int i = 0; i < queries.size(); i++) {
                Query q = queries.get(i);
                System.out.println("Query " + (i+1) + ":");
                System.out.println("  Main Var: " + q.mainVar);
                System.out.println("  Main Label: " + q.mainVarLabel);
                System.out.println("  Restrictor: " + q.restrictor);
                System.out.println("  Restrictor Clause: " + q.restrictorClause);
                System.out.println("  Where Clause: " + q.whereClause);
                System.out.println();
            }
        } catch (Exception e) {
            System.err.println("Parser failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}