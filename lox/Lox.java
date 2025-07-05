package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
    

public class Lox {

    static boolean hadError = false;

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

        // prints tokens for now
        for(Token token: tokens){
            System.out.println(token);
        }

    }

    // flags an error?
    static void error(int line, String message){
        report(line,"", message);
    }

    private static void report(int line, String where, String message){

        System.err.println("[line " + line + "] Error" + where + ": " + message );
        hadError = true;

    }
}

