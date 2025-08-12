package pgkeys.validation.parser.listener;

import pgkeys.validation.parser.antlr4.PGKeysBaseListener;
import pgkeys.validation.parser.antlr4.PGKeysParser;

import java.util.ArrayList;
import java.util.List;

public class PGKeysListenerImpl extends PGKeysBaseListener {
    private List<Query> queries;
    private Query currentQuery;

    public PGKeysListenerImpl() {
        queries = new ArrayList<>();
    }

    public List<Query> getQueries() {
        return queries;
    }

    @Override
    public void enterSchema(PGKeysParser.SchemaContext ctx) {
        System.out.println("enterSchema called with " + ctx.getChildCount() + " children");
        for (int i = 0; i < ctx.getChildCount(); i++) {
            System.out.println("Child " + i + ": " + ctx.getChild(i).getText());
        }
    }

    @Override
    public void enterQuery(PGKeysParser.QueryContext ctx) {
        System.out.println("enterQuery called with text: " + ctx.getText());
        currentQuery = new Query();
        currentQuery.stringQuery = ctx.getText();
    }

    @Override
    public void exitQuery(PGKeysParser.QueryContext ctx) {
        queries.add(currentQuery);
        currentQuery = null;
    }

    @Override
    public void enterMainLabel(PGKeysParser.MainLabelContext ctx) {
       currentQuery.mainVarLabel = ctx.getText();
    }

    @Override
    public void enterMainVar(PGKeysParser.MainVarContext ctx) {
        currentQuery.mainVar = ctx.getText();
    }

    @Override
    public void enterRestrictor(PGKeysParser.RestrictorContext ctx) {
        currentQuery.restrictor = ctx.getText();
    }

    @Override
    public void enterRestrictorClause(PGKeysParser.RestrictorClauseContext ctx) {
        // Use the original input text to preserve spacing
        int start = ctx.getStart().getStartIndex();
        int stop = ctx.getStop().getStopIndex();
        currentQuery.restrictorClause = ctx.getStart().getInputStream().getText(org.antlr.v4.runtime.misc.Interval.of(start, stop));
    }

    @Override
    public void enterWhereClause(PGKeysParser.WhereClauseContext ctx) {
        // Use getText() to get the parsed text without including extra tokens
        currentQuery.whereClause = ctx.getText();
    }
}
