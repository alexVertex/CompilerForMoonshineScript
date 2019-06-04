import javax.naming.Name;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Core {
    public static final String GLOBAL_ZONE = "mainZone";
    public static Enviroment global = new Enviroment("Global", null);
    public static HashMap<String, Enviroment> enviroments = new HashMap<>();

    static {
        enviroments.put("Global", global);
    }
    public static int digit;
    public static List<String> worked = new ArrayList<>();
    public static void main(String[] args){
        try {
            FileReader writer = new FileReader("IN.txt");
            BufferedReader bf = new BufferedReader(writer);
            String line = bf.readLine();
            while (line != null) {
                if(line.length()>0) {

                    worked.add(line);
                }
                line = bf.readLine();

            }
            writer.close();
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
        List<List<Lexer.Lexem>> phase2 = Lexer.lexering(worked);
        HashMap<Integer, Parser.Node> phase3 = Parser.parsing(phase2);
        AssemblerGenerate.generate(phase3);
    }
}