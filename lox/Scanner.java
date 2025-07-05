package lox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;


class Scanner {
    
    // the string representing the entire file being compiled currently
    private final String source;

    private final List<Token> tokens = new ArrayList<>();
    
    // the first character in the lexeme being scanned
    private int start = 0;

    // the current character being checked
    private int current = 0;

    // the current line
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    // runs when the class is loaded into memory
    static {

        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);

    }

    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens(){

        while(!isAtEnd()){

            // we are at the beginning of the next lexeme
            start = current;
            scanToken();

        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;

    }

    private void scanToken(){

        char c = advance();

        switch (c) {

            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break; 
            case '!': 
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            // handling slash
            case '/':
                if (match('/')){
                    // comments goes till the end of the line
                    while(peek() != '\n' && !isAtEnd()) advance();

                }else if(match('*')){

                    handleBlockComment();

                } else {
                    addToken(SLASH);
                }
                break;     
            
            case ' ':
            case '\r':
            case '\t':
                // Ignore all whitespace
                break;

            case '\n':
                line++;
                break;

            case '"': string(); break;

            // or 
            case 'o':
                
                if (match('r')){
                    addToken(OR);
                }
                break;
                
            default:

                if(isDigit(c)){

                    number();

                // if the lexeme starts with a letter it is an identifier (variable or reserved word)
                }else if (isAlpha(c)){

                    identifier();

                }else{

                    Lox.error(line, "Unexpected character.");

                }

            break;

        }

    }

    private void handleBlockComment(){
        
        // go till the end of the line
        while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {

            advance();

        }

        if (isAtEnd()){

            Lox.error(line, "Unterminated block comment");
            return;

        }

        // moves from * to / to the next character
        current++;
        advance();

    }

    private void identifier(){

        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null ) type = IDENTIFIER;

        addToken(type);

    }

    // converts a string value representing an interger or a fraction value into a double value in java
    private void number(){

        while(isDigit(peek())) advance();

        //look for a fractional part
        if (peek() == '.' && isDigit(peekNext())){

            //Consume the "."
            while (isDigit(peek())) advance();

        }
        // convert the string to a double value
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));

    }

    private void string(){
        
        while(peek() != '"' && !isAtEnd()){

            if(peek() == '\n') line++;
            advance();

        }

        if (isAtEnd()){

            Lox.error(line, "Unterminated string.");
            return;

        }

        // the closing "
        advance();

        // trim the surrounding quotes
        String value = source.substring(start + 1, current-1);
        addToken(STRING, value);

    }

    // checks to see if a certain character follows the previous one for example: to see the equality operators have an equal-to sign immediately after it.
    private boolean match(char expected){

        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        
        current++;
        return true;

    }


    // returns character at current and \0 if at the end of the file
    private char peek(){

        if(isAtEnd()) return '\0';
        return source.charAt(current);

    }

    // shows the next character without advancing current (beacause numbers like 1234. are invalid)
    private char peekNext(){

        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);

    }

    // a custom function go check if a character is a digit 
    private boolean isDigit(char c){

        return c >= '0' && c <= '9';

    }

    //helper function isAlpha for identifier()
    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') || 
        (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c){

        return isAlpha(c) || isDigit(c);

    }

    // tells the scanner whetehr all the characters have been scanned through
    private boolean isAtEnd(){
        return current >= source.length();
    }

    // returns the next character in the source file
    private char advance() {
        return source.charAt(current++);
    }

    // grabs the text of the current lexeme and creates a token for it; lexemes which does not have a literal gets a null value automatically
    private void addToken(TokenType type){

        addToken(type, null);

    }

    // creates token; literal values get passed as a parameter
    private void addToken(TokenType type, Object literal){

        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));

    }
}
