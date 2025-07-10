package lox;

class Interpreter implements Expr.Visitor<Object> {

    // handling the conversion of literals from syntax tree form to runtime values
    @Override
    public Object visitLiteralExpr(Expr.Literal expr){

        return expr.value;

    }

    // handling conversion of groupings
    @Override 
    public Object visitGroupingExpr(Expr.Grouping expr){

        return evaluate(expr.expression);

    }


    //helper method evaluate
    private Object evaluate(Expr expr){
        
        return expr.accept(this);
    }


}
