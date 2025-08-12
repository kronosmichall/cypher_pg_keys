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
    public void enterQuery(PGKeysParser.QueryContext ctx) {
        currentQuery = new Query();
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
        currentQuery.restrictorClause = ctx.getText();
    }

    @Override
    public void enterWhereClause(PGKeysParser.WhereClauseContext ctx) {
        currentQuery.whereClause = ctx.getText();
    }
}
