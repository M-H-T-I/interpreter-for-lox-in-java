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


    // Bianry expression conversion
    @Override
    public Object visitBinaryObject(Expr.Binary expr){

        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case GREATER:
              return (double)left > (double)right;
            case GREATER_EQUAL:
              return (double)left >= (double)right;
            case LESS:
              return (double)left < (double)right;
            case LESS_EQUAL:
              return (double)left <= (double)right;

            
            case BANG_EQUAL: return !isEqual(left,right);
            case EQUAL_EQUAL: return isEqual(left,right);

            case MINUS:
                return (double)left - (double)right;

            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                } 

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                break;

            case SLASH:
                return (double)left / (double)right;

            case STAR:
                return (double)left * (double)right;
            
        }

        //unreachable
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

    private boolean isEqual(Object a, Object b){

        if (a == null && b == null) return true;
        if (a==null) return false;

        return a.equals(b);

    }



}
