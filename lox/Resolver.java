package lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    
    private final Interpreter interpreter;
    private final Stack<Map<String,Boolean>> scopes = new Stack<>();

    Resolver(Interpreter interpreter){  

        this.interpreter = interpreter;

    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){

        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;

    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){

        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt);
        return null;

    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){

        //declare the variable
        declare(stmt.name);
        if(stmt.initializer != null){
            // if present resolve the initializer
            resolve(stmt.initializer);
        }

        define(stmt.name);
        return null;

    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt){

        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;

    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        if(stmt.value != null){
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    // Expressions ----------------------------------------------------------------------------


    @Override 
    public Void visitVariableExpr(Expr.Variable expr){
        if(!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE){
            Lox.error(expr.name, "Can't read local variable in its own initializer");   
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr){
        resolve(expr.value);
        resolveLocal(expr, expr.value);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }
    
    @Override
    public Void visitCallExpr(Expr.Call expr){
        
        resolve(expr.callee);


        for (Expr argument: expr.arguments){
            resolve(argument);
        }

        return null;
    }

    @Override 
    public Void visitGroupingExpr(Expr.Grouping expr){
        resolve(expr.expression);
        return null;
    }
    
    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }
    
    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {

        resolve(expr.left);
        resolve(expr.right);
        return null;

    }
    
    @Override
    public Void visitUnaryExpr(Expr.Unary expr){
        resolve(expr.right);
        return null;
    }

    //--------------------------- Helper methods -------------------------

    // walks a list of statements and resolves each one: calls resolve method below
    void resolve(List<Stmt> statements){
        for (Stmt statement: statements){
            resolve(statement);
        }
    }

    // calls the accept method of statements
    private void resolve(Stmt stmt){

        stmt.accept(this);
    } 

    // sees which scope has the variable if it does exist  
    private void resolveLocal(Expr expr, Token name){
        for (int i = scopes.size() - 1; i >= 0; i--){
            if (scopes.get(i).containsKey(name.lexeme)){
                // we asssume the variable is global
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    // pushes scope on to the stack we use to keep track of scopes
    private void beginScope(){
        scopes.push(new HashMap<String, Boolean>());
    }

    //ends (terminates) a scope by popping it off of the stack
    private void endScope(){

        scopes.pop();
    }


    // declares a variable in scope 
    private void declare(Token name){
        // in global scope hence no need to resolves
        if(scopes.isEmpty()) return;

        // gets the reference to the innermost scope
        Map<String, Boolean> scope = scopes.peek();
        // declares the variable in said scope
        scope.put(name.lexeme,false);
    }

    //defines a variable's value; marks its value to true in the map
    private void define(Token name){
        if (scopes.isEmpty())return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveFunction(Stmt.Function function){

        beginScope();
        for (Token param: function.params){
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
    }

}
