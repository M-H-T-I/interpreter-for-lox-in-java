package lox;

import java.util.List;

class LoxFunction implements LoxCallable{
    private final Stmt.Function declaration;
    private final Environment closure;
    
    LoxFunction(Stmt.Function declaration, Environment closure){
        this.declaration = declaration;
        this.closure = closure;
    }

    // the parent scope is bound to the inner scope and used to create a LoxFUnction Object
    LoxFunction bind(LoxInstance instance){
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment);
    }

    @Override 
    public Object call(Interpreter interpreter, List<Object> arguments){

        // creating a local environment with the enclosing scope as its parent
        Environment environment = new Environment(closure);

        for(int i = 0; i < declaration.params.size(); i++){
            environment.define(declaration.params.get(i).lexeme , arguments.get(i));
        }

        try{
            interpreter.executeBlock(declaration.body, environment);
        }catch(Return returnValue){
            return returnValue.value;
        }

        if(isInitializer)return closure.getAt(0, "this");
        return null;
    }

    @Override
    public int arity(){
        return declaration.params.size();
    }

    @Override
    public String toString(){
        return "<fn " + declaration.name.lexeme + ">";
    }
}
