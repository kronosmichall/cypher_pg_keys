grammar PGKeys;

@header {
package pgkeys.validation.parser.antlr4;
}

schema : query*;
query : FOR LPAREN mainVar COLON mainLabel RPAREN restrictor restrictorClause withinClause? PERIOD;

mainLabel: IDENTIFIER;
mainVar: IDENTIFIER;
withinClause: WITHIN whereClause;
whereClause: cypherExpression;
restrictorClause: anythingUntilWithin;

// More specific rules for structure validation
propertyList: property (COMMA property)*;
property: IDENTIFIER (DOT IDENTIFIER)*;
cypherExpression: LPAREN nodePattern RPAREN relationshipPattern* LPAREN nodePattern RPAREN;
nodePattern: IDENTIFIER (COLON IDENTIFIER)?;
relationshipPattern: DASH LBRACKET COLON IDENTIFIER RBRACKET DASH GREATER;

// Keywords (order matters - put before IDENTIFIER)
WITHIN: 'WITHIN';
FOR: 'FOR';
EXCLUSIVE_MANDATORY: 'EXCLUSIVE MANDATORY';
EXCLUSIVE_SINGLETON: 'EXCLUSIVE SINGLETON';
EXCLUSIVE: 'EXCLUSIVE';
MANDATORY: 'MANDATORY';
SINGLETON: 'SINGLETON';
IDENTIFIER_KEYWORD: 'IDENTIFIER';

// Punctuation
LPAREN: '(';
RPAREN: ')';
LBRACKET: '[';
RBRACKET: ']';
COLON: ':';
COMMA: ',';
DOT: '.';
PERIOD: '.';
DASH: '-';
GREATER: '>';

// Identifiers (must come after keywords)
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;

// Capture anything until WITHIN keyword  
anythingUntilWithin: ~WITHIN+;

restrictor: IDENTIFIER_KEYWORD | EXCLUSIVE_MANDATORY | EXCLUSIVE_SINGLETON | EXCLUSIVE | MANDATORY | SINGLETON;

NEWLINE: ('\r'? '\n') -> skip;
WS: [ \t\r\n]+ -> skip;
