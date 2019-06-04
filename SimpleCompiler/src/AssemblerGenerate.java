import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AssemblerGenerate {
    static int dataStack = 13;
    static int procStack = dataStack+3;
    static String curenv = "Global";
    static boolean procGenerate;
    public static List<String> lines = new ArrayList<>();
    public static void generate(HashMap<Integer, Parser.Node> tree){
        INIT();
        for (int i = 0; i < tree.size();i++){
            Parser.Node rax = tree.get(i);
            if(rax == null){
                continue;
            }
            switch (rax.operantion){
                case "METHOD":
                    METHOD(rax.center);///
                    break;
                case "NEG":
                    NEG(rax.left);
                    break;
                case "WRITE":
                    WRITE(rax.right);///
                    break;
                case "READ":
                    READ(rax);/////
                    break;
                case "IF":
                    IF(rax);///
                    break;
                case "ELSE":
                    ELSE(rax);////
                    break;
                case "WHILE":
                    WHILE(rax,i);///
                    break;
                case "CREATE":
                    CREATE(rax);////
                    break;
                case "empty":
                    END(rax);///
                    break;
                case "=":
                    SET(rax);////
                    break;
                case "RETURN":
                    RETURN(rax);
                    break;
                    default:
                        System.out.println("EMPTY");
                        break;

            }
        }
        CLOSE();
        clearSameLines();
        writeAllCode();
        System.out.println("КОД СГЕНЕРИРОВАН");
    }
    private static void clearSameLines(){
        for(int i = 0; i < lines.size()-1;i++){
            if(lines.get(i).equals(lines.get(i+1))){
                lines.remove(i--);
            }
        }
    }
    private static int jumpLvl = 2;
    private static HashMap<String,Integer> jumps = new HashMap<>();
    private static HashMap<String,String> jumpsInstructions = new HashMap<>();

    static {
        jumpsInstructions.put("==","cmp eax, ebx \njne L");
        jumpsInstructions.put("<>","cmp eax, ebx \nje L");
        jumpsInstructions.put(">","cmp eax, ebx \njle L");
        jumpsInstructions.put("<","cmp eax, ebx \njge L");
        jumpsInstructions.put(">=","cmp eax, ebx \njl L");
        jumpsInstructions.put("<=","cmp eax, ebx \njg L");
    }
    
    private static String param(Parser.Node el){
        String type = el.left.operantion;
        String id = Core.enviroments.get(curenv).getVar(el.right.operantion).id;

        String name = id;
        switch (type){
            case "DOUBLE":
                break;
            case "INTEGER":
                type = "DWORD";
                break;
            default:
                System.err.println("WRONG PARAM TYPE");
                System.exit(1);
                break;
        }
        return name + " :" + type;
    }
    private static void addlines(String line){
        if(procGenerate){
            lines.add(procStack,line);
            procStack++;
        } else {
            lines.add(line);
        }
    }
    private static void addlines(String line, int block){ // 1 - глобальные переменные
        if(block == 1){
            lines.add(dataStack,line);
            dataStack++;
            procStack++;
        }
    }
    private static void RETURN(Parser.Node el){
        OperationHub(el.left,true);
        addlines("ret");
    }
    private static void NEG(Parser.Node el){
        addlines("ret");
        addlines(Enviroment.getMeth(el.operantion)+ " endp");
        curenv = "Global";
        procGenerate = false;
    }
    public static void METHOD(Parser.Node el){
        procGenerate = true;
        System.out.println("METHOD");
        String name = Enviroment.getMeth(el.left.operantion);
        curenv = name;
        String type = el.center.left.operantion;
        String para = "";
        boolean firstParam = true;
        Parser.Node param = el.center.center;
        while (param != null){
            if(firstParam){
                para = param(param);
                firstParam = false;
            } else {
                para = para.concat(", " + param(param));
            }
            param = param.center;
        }
            addlines(name + " proc " + para);
    }
    public static void READ(Parser.Node el){
        addlines("invoke StdIn, addr READ, 100");
        addlines("invoke StripLF, addr READ");
        addlines("invoke atodw, addr READ");
        String id = Core.enviroments.get(curenv).getVar(el.right.operantion).id;
        addlines("mov "+id+", eax");
        System.out.println("READ");
    }
    private static void SET(Parser.Node el) {
        String id = Core.enviroments.get(curenv).getVar(el.left.operantion).id;

        OperationHub(el.right,true);
        addlines("mov " + id+", eax");
        System.out.println("SET");
    }
    private static void CREATE(Parser.Node el) {
        String id = Core.enviroments.get(curenv).getVar(el.right.operantion).id;
        if(procGenerate){
            addlines("LOCAL " + id + ": dword");
        } else {
            addlines(id + " dword  0", 1);
        }
        System.out.println("CREATE");
    }
    private static void WHILE(Parser.Node el,int i) {
        int whilejump = jumpLvl;
        jumpLvl++;
        addlines("L" + whilejump + ":");
        jumps.put(i + "", whilejump);
        OperationHub(el.left, true);
        String testLine = jumpsInstructions.getOrDefault(el.left.operantion, null);
        if (testLine != null) {
            lines.remove(lines.size() - 1);
            addlines(testLine + jumpLvl);
        } else {
            addlines("cmp eax, 0 \nje L" + jumpLvl);
        }
        jumps.put(el.right.operantion, jumpLvl);
        jumpLvl++;
        System.out.println("WHILE");
    }
    private static void IF(Parser.Node el) {
        if(el.left == null){
            addlines("jmp L" + jumps.get(el.right.operantion));
            System.out.println("BREAK/CONTINUE");
        } else {
            OperationHub(el.left, true);
            String testLine = jumpsInstructions.getOrDefault(el.left.operantion, null);
            if (testLine != null) {
                if(procGenerate){
                    procStack--;
                    lines.remove(procStack);

                } else {
                    lines.remove(lines.size() - 1);

                }
                addlines(testLine + jumpLvl);
            } else {
                addlines("cmp eax, 0 \nje L" + jumpLvl);
            }
            jumps.put(el.right.operantion, jumpLvl);
            jumpLvl++;
            System.out.println("IF");
        }

    }
    private static void ELSE(Parser.Node el){
        if(!jumps.containsKey(el.right.operantion)){
            jumps.put(el.right.operantion,jumpLvl);
            jumpLvl++;
            addlines("jmp L"+ jumps.get(el.right.operantion));
            addlines("L"+ jumps.get(el.left.operantion)+ ":");
            System.out.println("ELSE");
        } else {
            addlines("jmp L"+ jumps.get(el.right.operantion));
            addlines("L"+ jumps.get(el.left.operantion)+ ":");
            System.out.println("ENDWHILE");
        }
    }
    private static void END(Parser.Node el){
        addlines("L"+ jumps.get(el.left.operantion)+ ":");
        System.out.println("END");
    }
    private static void OperationHub(Parser.Node el,boolean left){
        if(el == null){
            return;
        }
        if(el.operantion.equals("METHOD")){
            String name = Enviroment.getMeth(el.center.left.operantion);
            String para = "";

            boolean firstParam = true;

            Parser.Node param = el.center.center;

            while (param != null){
                if(firstParam){
                    if(Character.isDigit(param.right.operantion.charAt(0))){
                        para = param.right.operantion;
                    } else {
                        String id = Core.enviroments.get(curenv).getVar(param.right.operantion).id;
                        para = id;
                    }
                    firstParam = false;
                } else {
                    String add = "";
                    if(Character.isDigit(param.right.operantion.charAt(0))){
                        add = param.right.operantion;
                    } else {
                        String id = Core.enviroments.get(curenv).getVar(param.right.operantion).id;
                        add = id;
                    }
                    para = para.concat(", " + add);
                }
                param = param.center;
            }
            addlines("invoke " + name + ", "+para);

            return;
        }
        if(isOperation(el.operantion)){
            ariphmLogOperation(el,left,finalLines.get(el.operantion));
        } else {
            SIMPLE(el,left);
        }
    }

    public static void ariphmLogOperation(Parser.Node el, boolean left,String finalLine){
        if(el.operantion.equals("/")){
            addlines("mov edx, 0");
        }
        OperationHub(el.left,true);
        if(el.right != null) {
            if (sameLvlOperation(el.right.operantion, el.operantion)) {
                OperationHub(el.right, false);
            } else {

                    addlines("mov ecx, eax");
                    addlines("push ecx");
                    OperationHub(el.right, false);
                    addlines("pop ecx");
                    addlines("mov ebx, ecx");

            }
        }
        addlines(finalLine);
    }

    private static void SIMPLE(Parser.Node el, boolean left){
        if(Character.isDigit(el.operantion.charAt(0))) {
            if (left) {
                addlines("mov eax, " + el.operantion);
            } else {
                addlines("mov ebx, " + el.operantion);
            }
        } else {
            String id = Core.enviroments.get(curenv).getVar(el.operantion).id;
            if (left) {
                addlines("mov eax, " + id);
            } else {
                addlines("mov ebx, " + id);
            }
        }
    }
    private static List<String> strings = new ArrayList<>();
    private static void WRITE(Parser.Node el){
        if(el.operantion.charAt(0) == '\"'){
            String line = "_" + el.operantion.substring(1,el.operantion.length()-1)+"_";
            line = line.replaceAll(" ", "_");
            if(!strings.contains(line)) {
                strings.add(line);
                addlines(line + " db   " + el.operantion + ",0dh, 0ah , 0", 1);
            }
            addlines("invoke  crt_printf, ADDR " + line);
        } else {
            OperationHub(el, true);
            addlines("invoke  crt_printf, ADDR tpt, eax");
        }
        System.out.println("WRITE");
    }
    private static void INIT(){
        addlines(".386");
        addlines(".model flat, stdcall");
        addlines("option casemap :none");
        addlines("include \\masm32\\include\\windows.inc");
        addlines("include \\masm32\\include\\masm32.inc");
        addlines("include \\masm32\\include\\msvcrt.inc");
        addlines("include \\masm32\\macros\\macros.asm");
        addlines("includelib \\masm32\\lib\\masm32.lib");
        addlines("includelib \\masm32\\lib\\msvcrt.lib");
        addlines("include \\masm32\\include\\kernel32.inc");
        addlines("includelib \\masm32\\lib\\kernel32.lib");
        addlines(".data");
        addlines("tpt db      '%d',0dh,0ah,0");
        addlines(".data?");
        addlines(" READ dword 100 dup(?)");
        addlines(".code");
        addlines("start:");
    }
    private static void CLOSE(){
        addlines("invoke  crt__getch");
        addlines("invoke  crt_exit,0");
        addlines("end start");
    }
    private static void writeAllCode(){
        for(String el : lines){
            System.out.println(el);
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("OUT.txt"));
            for(String el : lines){
                writer.write(el);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static HashMap<String,Integer> levels = new HashMap<>();
    private static HashMap<String,String> finalLines = new HashMap<>();

    static {
        levels.put("NEG",0);
        levels.put("!",0);
        levels.put("+",2);
        levels.put("-",2);
        levels.put("*",1);
        levels.put("/",1);
        levels.put(">",3);
        levels.put("<",3);
        levels.put(">=",3);
        levels.put("<=",3);
        levels.put("==",4);
        levels.put("<>",4);
        levels.put("METHOD",-2);
        levels.put("AND",5);
        levels.put("OR",6);

        finalLines.put("NEG","neg eax");
        finalLines.put("AND","and eax, ebx");
        finalLines.put("OR","or eax, ebx");
        finalLines.put("+","add eax, ebx");
        finalLines.put("-","sub eax, ebx");
        finalLines.put("*","mul  ebx");
        finalLines.put("/","div  ebx");
        finalLines.put(">","cmp eax, ebx\n" +
                "setg al\n" +
                "movzx eax, al");
        finalLines.put("<","cmp eax, ebx\n" +
                "setl al\n" +
                "movzx eax, al");
        finalLines.put(">=","cmp eax, ebx\n" +
                "setge al\n" +
                "movzx eax, al");
        finalLines.put("<=","cmp eax, ebx\n" +
                "setle al\n" +
                "movzx eax, al");
        finalLines.put("==","cmp eax, ebx\n" +
                "sete al\n" +
                "movzx eax, al");
        finalLines.put("<>","cmp eax, ebx\n" +
                "setne al\n" +
                "movzx eax, al");
        finalLines.put("!","cmp eax, 0\n" +
                "sete al\n" +
                "movzx eax, al");
    }


    private static boolean isOperation(String first){
        int lvlOne = levels.getOrDefault(first, -1);
        return lvlOne > -1;
    }
    private static boolean sameLvlOperation(String first, String second){
        int lvlOne = levels.getOrDefault(first,-1);
        int lvlTwo = levels.getOrDefault(second,-1);
        return lvlOne == lvlTwo || lvlOne == -1 || lvlTwo == -1;
    }
}
