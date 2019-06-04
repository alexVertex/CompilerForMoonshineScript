import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    static class Lexem{
        List<Lexem> collection = new ArrayList<>();
        String word;
        int type;
        int pos;
        public Lexem(String Word, int Type, int Pos){
            word = Word;
            type = Type;
            pos = Pos;
        }
    }

    private static List<String> RESERVED = new ArrayList<>();

    static {
        RESERVED.add("DOUBLE");RESERVED.add("INTEGER");
        RESERVED.add("METHOD");
        RESERVED.add("START");RESERVED.add("FINISH");
        RESERVED.add("==");RESERVED.add(">=");RESERVED.add("<=");RESERVED.add("<>");RESERVED.add(">");RESERVED.add("<");
        RESERVED.add("(");RESERVED.add(")");
        RESERVED.add("+"); RESERVED.add("-");RESERVED.add("*");RESERVED.add("/");RESERVED.add("=");
        RESERVED.add("IF");RESERVED.add("ELSE");RESERVED.add("WHILE");
        RESERVED.add("BREAK");RESERVED.add("CONTINUE");RESERVED.add("RETURN");
        RESERVED.add("AND");RESERVED.add("OR");RESERVED.add("!");RESERVED.add("NEG");
        RESERVED.add("WRITE");RESERVED.add("READ");
        RESERVED.add("\"");
    }

    static List<List<Lexem>> allLexems = new ArrayList<>();

    public static List<List<Lexem>> lexering(List<String> text) {

        System.out.println("Начали производить лексинг");

        for(String line : text){
            line = removeComments(line);
            labelAllWords(line);
        }
        Enviroments(allLexems);
        System.out.println("Закончили производить лексинг");
        return allLexems;
    }
    static int start = 0;
    private static void labelAllWords(String line){

        start = 0;
        List<Lexem> lexems = new ArrayList<>();
        line = makeQuotes(line,lexems);

        for (int i = 0; i < RESERVED.size();) {
             int index = line.indexOf(RESERVED.get(i),start);
             if(index>-1){
                 Lexem lexem = new Lexem(RESERVED.get(i),0,index);
                 lexems.add(lexem);
                 start = index+1;
             } else {
                 i++;
                 start = 0;
             }
         }
        Pattern pattern = Pattern.compile("[-]?[0-9]+(\\.[0-9]+)?");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()){
            String digit = (line.substring(matcher.start(),matcher.end()));
            Lexem lexem = new Lexem(digit,1,matcher.start());
            lexems.add(lexem);

        }

        pattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");
        matcher = pattern.matcher(line);
        while (matcher.find()){
            String digit = (line.substring(matcher.start(),matcher.end()));
            if(RESERVED.contains(digit)) continue;
            Lexem lexem = new Lexem(digit,2,matcher.start());
            lexems.add(lexem);
        }
        lexems.sort(new Comparator<Lexem>() {
            @Override
            public int compare(Lexem o1, Lexem o2) {
                if (o1.pos > o2.pos) return +1;
                if (o1.pos < o2.pos) return -1;
                return 0;
            }
        });
        for(int i = 0; i < lexems.size()-1;){
            if(lexems.get(i).type == 0 && lexems.get(i+1).type == 0 && !lexems.get(i+1).word.equals("!") && !lexems.get(i+1).word.equals("-")){
                if(lexems.get(i+1).word.equals("(")){ i++; continue;}
                if(lexems.get(i+1).word.equals(")")){ i++; continue;}
                if(lexems.get(i).word.equals("(")){ i++; continue;}
                if(lexems.get(i).word.equals(")")){ i++; continue;}
                if(lexems.get(i+1).word.equals("READ")){ i++; continue;}
                if(lexems.get(i).word.equals("READ")){ i++; continue;}
                if(lexems.get(i+1).word.equals("WRITE")){ i++; continue;}
                if(lexems.get(i).word.equals("WRITE")){ i++; continue;}

                lexems.remove(i+1);
            } else {
                i++;
            }
        }
        makeBrackets(lexems);
        allLexems.add(lexems);
    }
    private static void Enviroments(List<List<Lexem>> allLexems){
        Enviroment curEnviroment = Core.global;
        int lvl = 0;
        int methods = 0;
        for(List<Lexem> el : allLexems){
            if(el.get(0).word.equals("METHOD")){
                Enviroment method = new Enviroment("method"+methods,curEnviroment);
                Core.enviroments.put("method"+methods,method);
                List<Lexem> params = el.get(2).collection;
                Enviroment.addMeth(el.get(1).word,"method"+methods);
                curEnviroment = method;
                for(int k = 0; k < params.size();k+=2){
                    curEnviroment.putParam(params.get(1).word,params.get(0).word);
                }
                methods++;
                lvl++;
            }
            if(el.get(0).word.equals("IF")){
               if(lvl>0)
                lvl++;
            }
            if(el.get(0).word.equals("WHILE")){
                if(lvl>0)
                    lvl++;

            }
            if(el.get(0).word.equals("END")){
                lvl--;
                if(lvl==0) {
                    curEnviroment = curEnviroment.father;
                }
                if(el.size()>1 && el.get(1).word.equals("ELSE")){
                    if(lvl>0)

                        lvl++;
                }
            }
            if(el.get(0).word.equals("DOUBLE") || el.get(0).word.equals("INTEGER")){
                curEnviroment.putVar(el.get(1).word,el.get(0).word);

            }
        }
    }
    private static String makeQuotes(String line1,List<Lexem> lexems){
        boolean findsecond = false;
        int start = 0;
        for(int i = 0; i < line1.length();i++){
            if(line1.charAt(i) == '\"'){
                if(!findsecond) {
                    findsecond = true;
                    start = i;
                } else {
                    findsecond = false;
                    String line = "";
                    for(int j = start; j <i+1;j++){
                        line = line.concat(line1.charAt(j)+"");

                    }
                    line1 = line1.replace(line,"");
                    i-= (start - i - 1);
                    Lexem lex = new Lexem(line,1,start);
                    lexems.add(lex);
                }
            }
        }
        return line1;
    }
    private static void  makeBrackets(List<Lexem> lexems){
        int lvl = 0;
        int start = 0;
        for(int i = 0; i < lexems.size();i++){
            if(lexems.get(i).word.equals("(")){
                lvl++;
                if(lvl == 1){
                    start = i;
                }
            } else if(lexems.get(i).word.equals(")")){
                lvl--;
                if(lvl == 0) {
                    Lexem brackets = new Lexem("()", 0,start);
                    for(int j = start; j <i+1;){
                        brackets.collection.add(lexems.get(j));

                        lexems.remove(start);
                        i--;
                    }
                    brackets.collection.remove(0);
                    brackets.collection.remove(brackets.collection.size()-1);
                    lexems.add(start,brackets);
                    makeBrackets(brackets.collection);
                }
            }
        }
    }
    private static String removeComments(String in){
        String out = in;
        int findCOmmentSymbol = out.indexOf("#");
        if(findCOmmentSymbol > -1){
            out = out.substring(0,findCOmmentSymbol);
        }
        return out;
    }
}