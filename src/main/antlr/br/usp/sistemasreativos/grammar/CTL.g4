grammar CTL;

@header {
package br.usp.sistemasreativos.grammar;
}

// Lexer rules BEGIN
WS : [ \r\t\n]+ -> skip ;

// Declare logic operators tokens
AND : '&' ;
OR : '|' ;
BICONDITIONAL : '<->' ;
IMPLIES : '->' ;
NOT : '!' ;

// Declare CTL operators tokens
CTLOpUnary : ExistsOrAll ('X' | 'F' | 'G') ;
CTLOpBinary : ExistsOrAll 'U' ;
fragment ExistsOrAll : 'E' | 'A' ;

// Declare property tokens
PropertyID : [a-zA-Z0-9]+ ;
ID : IDFirstChar (IDFirstChar | DIGIT | '$' | '#' | '-')* ;
fragment IDFirstChar : [a-zA-Z_] ;
fragment DIGIT : [0-9] ;

// Declare paren tokens
PAREN_OPEN : '(';
PAREN_CLOSE : ')';
// Lexer rules END

// Parser rules BEGIN
parenExpr : PAREN_OPEN expr PAREN_CLOSE ;

ctlExpr : CTLOpUnary parenExpr #UnaryCTL
        | CTLOpBinary PAREN_OPEN expr ',' expr PAREN_CLOSE #BinaryCTL
        ;

expr : parenExpr #Paren
     | ctlExpr #CTL
     | NOT expr #Not
     | expr AND expr #And
     | expr OR expr #Or
     | expr BICONDITIONAL expr #Biconditional
     | <assoc=right> expr IMPLIES expr #Implies
     | PropertyID #Property
     ;
// Parser rules END
