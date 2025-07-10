package lox;

class Interpreter implements Expr.Visitor<Object> {

    // Literal conversion to runtime value:
    @Override
    public Object visitLiteralExpr(Expr.Literal expr){

        return expr.value;

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
                return -(double)right;
        
        }

        //unreacable
        return null;
    } 


    // Helper Methods

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



}
