package lox;

class Interpreter implements Expr.Visitor<Object> {

    // handling the conversion of literals from syntax tree form to runtime values
    @Override
    public Object visitLiteralExpr(Expr.Literal expr){

        return expr.value;
        
    }
    
}
