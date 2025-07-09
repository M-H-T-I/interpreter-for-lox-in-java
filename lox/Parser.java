package lox;

import java.util.List;
import static lox.TokenType.*;

class Parser {
    
    private final List<Token> tokens;
    private int current = 0;

    Parser (List<Token> tokens){
        this.tokens = tokens;
    } 

    // rule for expression coverted to code
    private Expr expression(){
        return equality();
    }
    
    // rule for equality coverted to code
    private Expr equality(){

        // maps to the first comparison non-terminal in the grammar rule for equality
        Expr expr = comparison();

        // runs the remaining part of the rule as many time as == or != occur
        while(match(BANG_EQUAL, EQUAL_EQUAL)){

            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right); // converts the comparison statement into a binary token
        

        }

        return expr;
    }


}
