package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    
    public static void main(String[] args) throws IOException{

        if (args.length!=1){

            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);

        }

        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
            "Assign : Token name, Expr value",
            "Binary : Expr left, Token operator, Expr right",
            "Call : Expr callee, Token paren, List<Expr> arguments",
            "Grouping : Expr expression",
            "Literal: Object value",
            "Logical: Expr left, Token operator, Expr right",
            "Unary: Token operator, Expr right",
            "Variable: Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block : List<Stmt> statements",
            "Expression : Expr expression",
            "Function : Token name, List<Token> params, List<Stmt> body",
            "If : Expr condition, Stmt thenBranch, Stmt elseBranch",   
            "Print : Expr expression",
            "Return : Token keyword, Expr value",
            "Var : Token name, Expr initializer",
            "While : Expr condition, Stmt body"
        ));

    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException{

        String path =  outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        // visitor interface 
        defineVisitor(writer, baseName, types);

        // the subclasses
        for (String type: types){

            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer,baseName,className, fields);

        }

        // the base accept method
        writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");


        writer.println("}");
        writer.close();

    }

    // creates the individual subclasses
    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList){

        writer.println(" static class " + className + " extends " + baseName + " {");

        //constructor
        writer.println("    " + className + "(" + fieldList + ") {");

        //store parameters in fields 
        String[] fields = fieldList.split(", ");
        for (String field: fields){

            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");

        }

        writer.println("    }");

        // implementing the accept method itself (overloading)
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("        return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        //fields
        writer.println();
        for(String field: fields){
            writer.println("    final " + field + ";");
        }

        writer.println("}");
        
    }

    // creates the Visitor interface
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types){

        // head of the interface
        writer.println("  interface Visitor<R> {");

        // body of the interface
        for(String type: types){
            String typeName = type.split(":")[0].trim();

            // Example: Binary visitBinaryExpr(Binary expr);
            writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
            
        writer.println("  }");

    }

}
