package lox;

import java.util.List;

import java.util.Arrays;
import java.util.ArrayList;
import static lox.TokenType.*;

class Parser {

    private static class ParseError extends RuntimeException{}
    
    private final List<Token> tokens;
    private int current = 0;

    Parser (List<Token> tokens){
        this.tokens = tokens;
    } 


    List<Stmt> parse(){
        
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;

    }

    //declaration -> varDecl | statement | funDecl;
    private Stmt declaration(){

        try{

            // fucntion declaration
            if( match(FUN)) return function("function");

            // if variable declaration
            if(match(VAR)) return varDeclaration();

            // else return statement
            return statement();

        } catch (ParseError error){
            synchronize();
            return null;
        }

    }

    // funDecl → "fun" function ;
    // function → IDENTIFIER "(" parameters? ")" block ;
    // parameters → IDENTIFIER ( "," IDENTIFIER )* ;
    private Stmt.Function function(String kind){

        Token name = consume(IDENTIFIER, "Expect "+ kind + "name.");
        
        consume(LEFT_PAREN,"Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();

        if(!check(RIGHT_PAREN)){
            do {

                if (parameters.size() > 255){
                    error(peek(), "Can't have more than 255 parameters");
                }

                parameters.add(consume(IDENTIFIER,"Expect parameter name."));

            }while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        // {body}
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block(); 
        
        return new Stmt.Function(name, parameters, body);

    }


    // varDecl -> var IDENTIFIER (= Expression) ;
    private Stmt varDeclaration(){

        Token name = consume(IDENTIFIER, "Expect variable name.");
        
        // default value
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect  ';' after variable declaration.");
        return new Stmt.Var(name, initializer);

    }

    // statement → exprStmt | printStmt | block | ifStmt | whileStmt | forStmt | returnStmt;
    private Stmt statement(){

        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if(match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();

    }

    // returnStatement -> 'return' expression? ';' ;
    private Stmt returnStatement(){

        //grabs the return keyword for error handling purposes
        Token keyword = previous();
        
        // default return value
        Expr value = null;

        // checks if current token is a semicolon (return default value)
        if (!check(SEMICOLON)){
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);

    }


    // printStatement -> print expression ';' 
    private Stmt printStatement(){
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    // whileStatement -> 'while (' + expression + ')' + statement; 
    private Stmt whileStatement(){
        
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "eXPECT ')' after condition.");
        Stmt body = statement();


        return new Stmt.While(condition, body);
    }

    private Stmt forStatement() {

        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        // initializer
        Stmt initializer;
        if (match(SEMICOLON)){
            initializer = null;
        }else if(match(VAR)){
            initializer = varDeclaration();
        }else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)){
            condition = expression();
        }

        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)){
            increment = expression();
        }

        consume(RIGHT_PAREN, "Expect ')' after clauses.");
        
        Stmt body = statement();


        if (increment != null){
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        if (condition == null ) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null){
            // a block statement wher ethe variable is efined first followed by the while statement
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    // exprStatement -> expression ';'
    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    // block -> {(declaration)*}
    private List<Stmt> block(){

        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block");
        return statements;
    }

    // ifStmt → "if" "(" expression ")" statement ( "else" statement )? ;
    private Stmt ifStatement(){

        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if( match(ELSE)){
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);

    }

    //expression → assignment ;
    private Expr expression(){
        return assignment();
    }


    // assignment -> IDENTIFIER "=" assignment | logic_or ;
    private Expr assignment(){

        // cascades into the higher precedence expressions
        Expr expr = or();

        if(match(EQUAL)){

            Token equals = previous();
            Expr value = assignment();

            // checking to see if left side is a variable
            if (expr instanceof Expr.Variable){

                Token name = ( (Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "invalid assignment target");
        }

        return expr;

    }
  
    // logic_or → logic_and ( "or" logic_and )* ;
    private Expr or(){

        Expr expr = and();
        
        while(match(OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;

    }

    // logic_and → equality ( "and" equality )* ;
    private Expr and(){
        
        Expr expr = equality();

        while (match(AND)){
            
            Token operator = previous();
            Expr right = equality();
            expr  = new Expr.Logical(expr, operator, right);

        }

        return expr;
    }
    
    //equality → comparison ( ( "!=" | "==" ) comparison )* ;
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


    //unary  → ( "!" | "-" ) unary | call;
    private Expr unary(){

        if(match(BANG, MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();

    }

    //call → primary ( "(" arguments? ")" )* ;
    private Expr call(){

        Expr expr = primary();

        while (true){
            if (match(LEFT_PAREN)){
                expr = finishCall(expr);
            } else {
                break;
            }
        }

        return expr;

    }

    // parses the argument list for function call type expressions
    private Expr finishCall(Expr callee){

        List<Expr> arguments = new ArrayList<>();

        if(!check(RIGHT_PAREN)){

            // Keeps adding arguments to list as long as comma is found
            do{

                // limiting the max number of arguments to 255
                if(arguments.size() >= 255 ){
                    error(peek(), "Cant have more than 255 arguments");
                }

                arguments.add(expression());

            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);

    }


    // primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    private Expr primary(){

        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);

        if(match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(IDENTIFIER)) {
            return new Expr.Variable(previous());
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

    // checks to see if type is present e.g. parenthesis is followed by a closing ) 
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
