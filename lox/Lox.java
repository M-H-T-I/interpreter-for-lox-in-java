package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
    

public class Lox {

    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException{
        if (args.length > 1){

            System.out.println("Usage: jlox[script]");
            System.exit(64);

        }else if (args.length == 1){
            runFile(args[0]);
        }else{
            runPrompt();
        }
    }

    // runs the file given by converting it into bytes then running the run() function
    private static void runFile(String path) throws IOException{ 

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);

    }

    // if no arguments are given to lox then this function runs; you can input one line of code at a time and this function runs it
    private static void runPrompt() throws IOException{

        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;){
            System.out.println("> ");
            String line = reader.readLine();
            if (line==null) break;
            run(line);

            hadError = false;

        }

    }

    private static void run(String source){

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens(); 

        //parses the list of tokens
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // stop if there is a syntax error
        if (hadError)return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        // interprets all of the syntax
        interpreter.interpret(statements);

    }

    // recieves info of the error and calls report
    static void error(int line, String message){
        report(line,"", message);
    }

    // same function as above; this one is for the parser
    static void error(Token token, String message){

        if(token.type == TokenType.EOF){
            report(token.line, " at end", message);
        } else {
            report(token.line, " at'" + token.lexeme + "'", message);
        }


    }

    // prints error messages to the console
    private static void report(int line, String where, String message){

        System.err.println("[line " + line + "] Error" + where + ": " + message );
        hadError = true;

    }

    static void runtimeError(RuntimeError error){
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}

