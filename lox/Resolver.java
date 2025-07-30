package lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    
    private final Interpreter interpreter;

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
}
