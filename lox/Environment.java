package lox;

import java.util.HashMap;
import java.util.Map;


class Environment {
    
    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;

    Environment(){
        enclosing = null;
    }

    Environment(Environment enclosing){
       this.enclosing = enclosing;
    }

    
    // get the value of an existing variable
    Object get(Token name){


        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }

        // searches the parent environment for the variable
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,"Undefined variable '" + name.lexeme + "'.");
    }

    // define a variable
    void define(String name, Object value){

        // puts variable key (name) and value in HashMap
        values.put(name, value);

    }

    Object getAt(int distance, String name){
        return ancestor(distance).values.get(name);
    }

    void assignAt(int distance, Token name, Object value){
        ancestor(distance).values.put(name.lexeme, value);
    }

    Environment ancestor(int distance){
        Environment environment = this;
        for (int i= 0; i < distance; i++){
            environment  = environment.enclosing;
        }

        return environment;
    }

    // assign a value to an existing variable
    void assign(Token name, Object value){

        // check local scope for existance of variable
        if (values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return;
        }

        // if not present in local scope check parent scope
        if (enclosing != null){
            enclosing.assign(name, value);
            return;
        }

        // throw error if variable is not defined
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

}   
