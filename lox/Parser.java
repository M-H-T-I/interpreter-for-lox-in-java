package lox;

import java.util.List;
import static lox.TokenType.*;

class Parser {

    private static class ParseError extends RuntimeException{}
    
    private final List<Token> tokens;
    private int current = 0;

    Parser (List<Token> tokens){
        this.tokens = tokens;
    } 


    Expr parse(){
        try{
            return expression();
        }catch (ParseError error){
            return null;
        }
    }

    //expression → equality ;
    // rule for expression coverted to code
    private Expr expression(){
        return equality();
    }
    
    //equality → comparison ( ( "!=" | "==" ) comparison )* ;
    // rule for equality coverted to code
    private Expr equality(){

        // maps to the first comparison non-terminal in the grammar rule for equality
        Expr expr = comparison();

        // runs the remaining part of the rule as many time as == or != occur (0 or more)
        while(match(BANG_EQUAL, EQUAL_EQUAL)){

            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right); // converts the comparison statement into a binary token
        

        }

        return expr;
    }

    // comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )*;
    // converted to java
    private Expr comparison(){

        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){

            Token operator = previous();
            Expr right = term(); // determining the right term
            expr = new Expr.Binary(expr, operator, right);

        }

        return expr;

    }

    //term → factor ( ( "-" | "+" ) factor )* ;
    // rule for term: addition, subtraction
    private Expr term(){

        Expr expr = factor();

        while(match(MINUS, PLUS)){

            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);

        }

        return expr;
    }

    //factor → unary ( ( "/" | "*" ) unary )* ;
    // factor: multiplication and division
    private Expr factor(){

        Expr expr = unary();

        while(match(SLASH, STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    //unary  → ( "!" | "-" ) unary | primary ;
    private Expr unary(){

        if(match(BANG, MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();

    }

    // primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    private Expr primary(){

        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);

        if(match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(LEFT_PAREN)){
            Expr expr = expression();

            consume(RIGHT_PAREN, "Expect ')' after expression");

            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");

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

    // checks to see if an parenthesis is followed by a closing ) 
    private Token consume(TokenType type, String message){

        if (check(type)) return advance();

        throw error(peek(), message);

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


    private ParseError error(Token token, String message){

        Lox.error(token, message);
        return new ParseError();

    }


    private void synchronize(){
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
                
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:    
                    return;

            }   

            advance();

        }
    }


}
