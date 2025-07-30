package lox;

import java.util.ArrayList;
import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();

    // used to store variables and their values
    private Environment environment = globals;


    Interpreter(){
        globals.define("clock", new LoxCallable() {
            
            @Override
            public int arity(){return 0;}

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments){
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString(){return "<native fn>";}

        });
    }



    // Statements ----------

    // expression statements
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){

        evaluate(stmt.expression);
        return null;

    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){

        while(isTruthy(evaluate(stmt.condition))){
            execute(stmt.body);
        }

        return null;
    }
    

    // if statements
    @Override
    public Void visitIfStmt(Stmt.If stmt){
        if (isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch);
        }else if (stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    // print statements
    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    //variable statements
    @Override
    public Void visitVarStmt(Stmt.Var stmt){

        // every variable gets a nil default value
        Object value = null;
        if (stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;

    }

    // block statements
    @Override 
    public Void visitBlockStmt(Stmt.Block stmt){
        excecuteBlock(stmt.statements, new Environment(environment));
        return null;
    }

    // helper function for block statements
    void excecuteBlock(List<Stmt> statements, Environment environment){

        Environment previous = this.environment;

        try{
            this.environment = environment;

            for (Stmt statement: statements){
                execute(statement);
            }

        }finally{
            this.environment = previous;
        }

    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){

        LoxFunction function = new LoxFunction(stmt);
        environment.define(stmt.name.lexeme , function);
        return null;
    }


    @Override
    public Void visitReturnStmt(Stmt.Return stmt){

        //default return value
        Object value = null;

        if(stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    // For expressions ----------------------------


    
    @Override
    public Object visitCallExpr(Expr.Call expr){

        Object callee = evaluate(expr.callee);


        List<Object> arguments = new ArrayList<>();
        for(Expr argument: expr.arguments){
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof LoxCallable)){
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable)callee;

        // checking function's arity
        if (arguments.size() != function.arity()){
            throw new RuntimeError(expr.paren,"Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }



        return function.call(this, arguments);


    }

    // assignment expression
    @Override
    public Object visitAssignExpr(Expr.Assign expr){

        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;

    }

    //variable expression
    @Override
    public Object visitVariableExpr(Expr.Variable expr){

        return environment.get(expr.name);

    }



    // Literal conversion to runtime value:
    @Override
    public Object visitLiteralExpr(Expr.Literal expr){

        return expr.value;

    }

    // logical expressions: AND, OR
    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        
        Object left = evaluate(expr.left);
        
        if (expr.operator.type == TokenType.OR){

            if (isTruthy(left)) return left;

        }else {

            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    // Grouping conversion to runtime value:
    @Override 
    public Object visitGroupingExpr(Expr.Grouping expr){

        return evaluate(expr.expression);

    }


    // Unary Expression conversion:
    public Object visitUnaryExpr(Expr.Unary expr){

        Object right = evaluate(expr.right); // calls the appropriate accept method letting the expression call its own vist method

        switch (expr.operator.type) {

            case BANG:

                return !isTruthy(right);

            case MINUS:

                checkNumberOperand(expr.operator, right);
                return -(double)right;
        
        }

        //unreacable
        return null;
    } 


    // Bianry expression conversion
    @Override
    public Object visitBinaryExpr(Expr.Binary expr){


        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case GREATER:

                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;

            case GREATER_EQUAL:

                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;

            case LESS:

                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;

            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;

            
            case BANG_EQUAL: return !isEqual(left,right);
            case EQUAL_EQUAL: return isEqual(left,right);

            case MINUS:
            
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;

            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                } 

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");

            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            
        }

        //unreachable
        return null;
    }


    void interpret(List<Stmt> statements){
        
        try {
            
            for (Stmt statement: statements){
                execute(statement);
            }
            
        } catch (RuntimeError error) {

            Lox.runtimeError(error);

        }
        
    }

    // Helper Methods

    // checks the operand 
    private void checkNumberOperand(Token operator, Object operand){

        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");

    }

    // checks the number of operands and validates the binary expression
    private void checkNumberOperands(Token operator, Object left, Object right){

        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");

    }

    //evaluate: calls the expression's accept method allowing the expression to call the appropriate type of method
    private Object evaluate(Expr expr){

        return expr.accept(this);

    }

    //isTruthy: determines what value is truth and coversely what is falsey
    private boolean isTruthy(Object object){

        // only null (nil) and false are falsey

        if(object == null) return false;
        if (object instanceof Boolean) return (boolean) object;

        return true;

    }

    private boolean isEqual(Object a, Object b){

        if (a == null && b == null) return true;
        if (a==null) return false;

        return a.equals(b);

    }

    private String stringify(Object object){

        if(object == null) return "nil";

        if (object instanceof Double){
            
            String text = object.toString();

            // in case number is 12.0, 34.0, etc.
            if (text.endsWith(".0")){

                text=text.substring(0, text.length() - 2);

            }

            return text;

        }

        return object.toString();
    }

    // calls the accept method of statements
    private void execute(Stmt stmt){
        stmt.accept(this);
    }
}
