package pgkeys.validation.parser.listener;

public class Query {
    public String mainVar;
    public String mainVarLabel;
    public String whereClause;
    public String restrictorClause;
    public String restrictor;

    public String forClause() {
       return '(' + mainVar + ':' + mainVarLabel + ')';
    }

    public String withinClause() {
        if (whereClause == null) return null;
        return "WITHIN " + whereClause;
    }

}
