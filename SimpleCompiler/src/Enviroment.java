import javax.naming.Name;
import java.util.HashMap;

public class Enviroment {

    private static HashMap<String, String> methods = new HashMap<>();
    public static void addMeth(String methName, String newName){
        methods.put(methName,newName);
    }
    public static String getMeth(String methName){
        String id = methods.getOrDefault(methName,null);
        if(id == null){
            System.err.println("WRONG METHOD NAME " + methName);
            System.exit(1);
        }
        return  id;
    }
    class Variable{
        String name;
        String id;
        String type;
        public Variable(String n, String i, String t){
            name = n;
            id = i;
            type = t;
        }
    }
    static int vars = 0,params = 0;
    String name;
    Enviroment father;
    HashMap<String, Variable> variables = new HashMap<>();
    public Enviroment(String n, Enviroment f){
        name = n;
        father = f;
    }
    public String putVar(String name, String type){
        if(variables.containsKey(name)){
            System.err.println("VARIABLE WITH THIS NAME IS ALREADY EXISTS " +name);
            System.exit(1);
        }

        Variable var = new Variable(name,"id"+vars,type);
        vars++;
        variables.put(var.name,var);
        return var.id;
    }
    public String putParam(String name, String type){
        if(variables.containsKey(name)){
            System.err.println("VARIABLE WITH THIS NAME IS ALREADY EXISTS " +name);
            System.exit(1);
        }

        Variable var = new Variable(name,"param"+params,type);
        params++;
        variables.put(var.name,var);
        return var.id;
    }
    public Variable getVar(String id){
        Variable var = variables.getOrDefault(id, null);
        if(var == null){
            if(father == null){
                System.err.println("CANNOT FIND SUCH VARIABLE " +id);
            } else {
                var = father.getVar(id);
            }
        }
        return var;
    }
}
