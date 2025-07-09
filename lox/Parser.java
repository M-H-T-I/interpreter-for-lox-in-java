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

    // helper function match: checks to see if any of the given types are present in the current token
    private boolean match(TokenType... types){

        for(TokenType type: types){
            if(check(type)){
                advance();
                return true;
            }

        }

        return false;

    }

    // helper function check: returns true if the current token is of the given type. Unlike match(), it never consumes the token, it only looks at it.
    private boolean check(TokenType type){

        if(isAtEnd()) return false;
        return peek().type == type;

    }

    // helper function advance: returns the current token and increments current for the next iteration
    private Token advance(){
        if(!isAtEnd()) current++;
        return previous();
    }

    // helper function isAtEnd: checks if wehave run out of tokens to parse
    private boolean isAtEnd(){
        return peek().type == EOF;
    }

    // fetches the token at current in List tokens
    private Token peek(){
        return tokens.get(current);
    }

    // returns the most recently processed token
    private Token previous(){
        return tokens.get(current - 1);
    }
}
