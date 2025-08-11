grammar PGKeys;

@header {
package pgkeys.validation.parser.antlr4;
}

schema : query *;
query : FOR LPAREN mainVar COLON mainLabel RPAREN restrictor restrictorClause withinClause ? PERIOD;
mainLabel: StringLiteral;
mainVar: StringLiteral;
withinClause: WITHIN whereClause;
whereClause: WildCard;
restrictorClause: WildCard;

StringLiteral : ('A'.. 'Z' | 'a'..'z' | '0'..'9' | '_' | '-' )+ ;
WildCard: (.*)? ;

restrictor: IDENTIFIER | EXCLUSIVE_MANDATORY | EXCLUSIVE_SINGLETON | EXCLUSIVE  | MANDATORY | SINGLETON;

WITHIN: 'WITHIN';
IDENTIFIER: 'IDENTIFIER';
EXCLUSIVE: 'EXCLUSIVE';
EXCLUSIVE_MANDATORY: 'EXCLUSIVE MANDATORY';
EXCLUSIVE_SINGLETON: 'EXCLUSIVE SINGLETON';
MANDATORY: 'MANDATORY';
SINGLETON: 'SINGLETON';

FOR: 'FOR';
LPAREN: '(';
RPAREN: ')';
COLON: ':';
PERIOD: '.';

NEWLINE           : ('\r'? '\n') -> skip;
WS                : [ \t\r\n]+ -> skip;
