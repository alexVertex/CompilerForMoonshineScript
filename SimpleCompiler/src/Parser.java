import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser {

    private static List<String> OR = new ArrayList<>();
    private static List<String> AND = new ArrayList<>();
    private static List<String> EQ = new ArrayList<>();
    private static List<String> OTN = new ArrayList<>();
    private static List<String> ADD = new ArrayList<>();
    private static List<String> MLT = new ArrayList<>();
    private static List<String> UNR = new ArrayList<>();
    private static List<String> alloperators = new ArrayList<>();


    static {
        OR.add("OR");
        AND.add("AND");
        EQ.add("==");
        EQ.add("<>");
        OTN.add(">=");
        OTN.add("<=");
        OTN.add(">");
        OTN.add("<");
        ADD.add("+");
        ADD.add("-");
        MLT.add("*");
        MLT.add("/");
        UNR.add("NEG");
        UNR.add("!");

        alloperators.add("OR");
        alloperators.add("AND");
        alloperators.add("==");
        alloperators.add("<>");
        alloperators.add(">=");
        alloperators.add("<=");
        alloperators.add(">");
        alloperators.add("<");
        alloperators.add("+");
        alloperators.add("-");
        alloperators.add("*");
        alloperators.add("/");
        alloperators.add("NEG");
        alloperators.add("!");
        alloperators.add("=");

    }

    static class Node {
        Node left;
        Node right;
        Node center;
        String operantion;

        public Node(String oper) {
            operantion = oper;
        }
    }

    private static String[] varTypes = {"INTEGER", "DOUBLE"};
    static HashMap<Integer, Node> trees = new HashMap<>();


    static int i = 0;

    public static HashMap<Integer, Node> parsing(List<List<Lexer.Lexem>> lexems) {
        i = 0;
        for (List<Lexer.Lexem> line : lexems) {
            if (isVarCreate(line)) {
                Node node = createVariable(line);
                trees.put(i, node);
            } else if (line.get(0).word.equals("IF")) {
                Node node = IF(line, lexems);
                trees.put(i, node);
            } else if (line.get(0).word.equals("WHILE")) {
                Node node = WHILE(line, lexems);
                trees.put(i, node);
            } else if (line.get(0).word.equals("BREAK")) {
                Node node = BREAK(line, lexems);
                trees.put(i, node);
            } else if (line.get(0).word.equals("METHOD")) {
                Node node = METHOD(line, lexems);
                trees.put(i, node);
            } else if (line.get(0).word.equals("RETURN")) {
                Node node = RETURN(line);
                trees.put(i, node);
            } else if (line.get(0).word.equals("CONTINUE")) {
                Node node = CONTINUE(line, lexems);
                trees.put(i, node);
            } else if (line.get(0).word.equals("WRITE")) {
                Node node = WRITE(line);
                trees.put(i, node);
            }
            else if (line.get(0).word.equals("READ")) {
                Node node = READ(line);
                trees.put(i, node);
            }else if (line.get(0).word.equals("END")) {
                if (!trees.containsKey(i)) {
                    Node empty = new Node("empty");
                    Node left = new Node(""+(i+1));
                    empty.left = left;
                    trees.put(i, empty);
                }
            } else {
                Node node = varSet(line);
                trees.put(i, node);
            }
            i++;
        }
        clearUnusedmeth();
        autoMath();
        System.out.println("Закончили парсинг");
        return trees;
    }
    private static void autoMath(){
        int size = trees.size();
        for(int j = 0; j <size;j++){
            Node ras = trees.get(j);
            if(ras == null || ras.right == null){
                continue;
            }
            if(alloperators.contains(ras.right.operantion)){
                doMath(ras.right);
            }
        }
    }
    private static void doMath(Node el){
        if(alloperators.contains(el.right.operantion)){
            doMath(el.right);
        }
        if(alloperators.contains(el.left.operantion)) {
            doMath(el.left);
        }
        if(isDigit(el.left.operantion ) && isDigit(el.right.operantion)){
            double left = Double.parseDouble(el.left.operantion);
            double right = Double.parseDouble(el.right.operantion);
            double result = calc(left,right,el.operantion);
            if(!el.left.operantion.contains(".") && !el.right.operantion.contains(".")){
                int resint = (int)result;
                el.right = null;
                el.left = null;
                el.operantion = resint + "";
            } else {
                el.right = null;
                el.left = null;
                el.operantion = result + "";
            }
        }
    }
    private static double calc(double first,double second,String operatiom){
        boolean cmp = false;
        switch (operatiom){
            case "+":
                return first+second;
            case "-":
                return first-second;
            case "*":
                return first*second;
            case "/":
                return first/second;
            case "NEG":
                return -second;
            case ">":
                 cmp = first > second;
                return cmp ? 1 : 0;
            case ">=":
                 cmp = first >= second;
                return cmp ? 1 : 0;
            case "<":
                 cmp = first < second;
                return cmp ? 1 : 0;
            case "<=":
                 cmp = first <= second;
                return cmp ? 1 : 0;
            case "<>":
                 cmp = first != second;
                return cmp ? 1 : 0;
            case "==":
                cmp = first == second;
                return cmp ? 1 : 0;
            case "OR":
                cmp = (first + second) > 0;
                return cmp ? 1 : 0;
            case "AND":
                cmp = first > 0 && second > 0;
                return cmp ? 1 : 0;
            case "!":
                return second == 1 ? 0 : 1;
            default:
                return 0;
        }
    }

    private static boolean isDigit(String line){
        if((line.charAt(0) == '-' && line.length()>1 && Character.isDigit(line.charAt(1)) || Character.isDigit(line.charAt(0))))        {
            return true;
        }
        return false;
    }
    private static List<String> unusedmeths = new ArrayList<>();
    private static void clearUnusedmeth(){
        boolean deleteMeth = false;
        boolean deleteDeadCode = false;
        int size = trees.size();
        for(int j = 0; j <size;j++){
            Node ras = trees.get(j);
            if(ras.operantion.equals("METHOD")){
                String name = ras.center.left.operantion;
                if(unusedmeths.contains(name)){
                    deleteMeth = true;
                }
            }
            if(ras.operantion.equals("IF") || ras.left == null){
                deleteDeadCode = true;
                continue;
            }
            if(deleteMeth) {
                if(ras.operantion.equals("NEG")){
                    deleteMeth = false;
                    System.out.println("CLEAR UNUSED METHOD");
                }
                trees.put(j,null);
            }
        }
    }

    private static int findWhile(List<List<Lexer.Lexem>> lexems, int start) {
        int find = start;
        int LVL = 0;
        for (int i = find; i > -1; i--) {
            if (lexems.get(i).get(0).word.equals("WHILE")) {
                return i;
            }
        }
        System.err.println("BREAK/CONTINUE FAILURE IN LINE " + i);
        return -1;
    }

    private static int findEnd(List<List<Lexer.Lexem>> lexems, int start) {
        int find = start + 1;
        int LVL = 0;
        for (int i = find; i < lexems.size(); i++) {
            if (lexems.get(i).get(0).word.equals("END") && i != find) {
                LVL--;
                if (LVL <= 0) {
                    return i + 1;
                }
            }
            if (lexems.get(i).get(lexems.get(i).size() - 1).word.equals("BEGIN")) {
                LVL++;
            }
        }
        System.err.println("CAN’T FIND END FOR LINE " + i);
        return -1;
    }

    private static Node READ(List<Lexer.Lexem> el) {
        Node operation = new Node("READ");
        Node res = new Node(el.get(1).word);
        operation.right = res;
        return operation;
    }
    private static Node RETURN(List<Lexer.Lexem> el) {
        Node operation = new Node("RETURN");
        el.remove(0);
        Node res = getRes(el);
        operation.left = res;
        return operation;
    }
    //разобрано
    private static Node METHOD(List<Lexer.Lexem> el, List<List<Lexer.Lexem>> lexems) {

        if (el.size() < 4) {
            System.err.println("GENERAL PARSER ERROR IN LINE " + i);
        }
        Node operation = new Node("METHOD");

        Node name = new Node("methodName");
        Node methodName = new Node(el.get(1).word);
        if(unusedmeths.contains(el.get(1).word)){
            System.err.println("DUPLICATE METHOD IN LINE " +i);
            System.exit(232);
        }
        unusedmeths.add(el.get(1).word);

        name.left = methodName;

        Node type = new Node("methodType");
        Node methodType = new Node(el.get(3).word);
        type.left = methodType;

        Node writing = getParams(el.get(2).collection);

        operation.center = name;
        name.center = type;
        type.center = writing;

        int find = findEnd(lexems, i-1);
        Node methodEnd = new Node("NEG");
        Node left = new Node(name.left.operantion);
        methodEnd.left = left;
        trees.put(find - 1, methodEnd);

        return operation;
    }

    private static Node getParams(List<Lexer.Lexem> el) {
        if (el.size() < 2) {
            System.err.println("GENERAL PARSER ERROR IN LINE " + i);
        }
        if (!el.get(0).word.equals("DOUBLE") && !el.get(0).word.equals("INTEGER")) {
            System.err.println("WRONG PARAM TYPE IN LINE " + i);
        }
        Node param = new Node("param");
        Node type = new Node(el.get(0).word);
        Node id = new Node(el.get(1).word);
        el.remove(0);
        el.remove(0);
        Node next = null;
        if (el.size() > 0) {
            next = getParams(el);
        }
        param.right = id;
        param.left = type;
        param.center = next;
        return param;
    }

    private static Node WRITE(List<Lexer.Lexem> el) {
        Node operation = new Node("WRITE");
        el.remove(0);
        Node res = getRes(el);
        operation.right = res;
        return operation;
    }

    private static Node BREAK(List<Lexer.Lexem> el, List<List<Lexer.Lexem>> lexems) {
        Node operation = new Node("IF");
        int find = findWhile(lexems, i);
        Node teleportation = new Node(trees.get(find).right.operantion + "");
        operation.right = teleportation;
        return operation;
    }

    private static Node CONTINUE(List<Lexer.Lexem> el, List<List<Lexer.Lexem>> lexems) {
        Node operation = new Node("IF");
        int find = findWhile(lexems, i);
        Node teleportation = new Node(find + "");
        operation.right = teleportation;
        return operation;
    }

    private static Node WHILE(List<Lexer.Lexem> el, List<List<Lexer.Lexem>> lexems) {
        Node operation = new Node("WHILE");
        Node condition = getRes(el.get(1).collection);
        int find = findEnd(lexems, i - 1);
        Node teleportation = new Node(find + "");
        operation.left = condition;
        operation.right = teleportation;
        Node endOfCircle = new Node("ELSE");
        Node returnToCycle = new Node(i + "");
        endOfCircle.right = returnToCycle;
        endOfCircle.left = new Node(find+"");
        trees.put(find - 1, endOfCircle);
        return operation;
    }

    private static Node IF(List<Lexer.Lexem> el, List<List<Lexer.Lexem>> lexems) {
        Node operation = new Node("IF");
        Node condition = getRes(el.get(1).collection);
        int find = findEnd(lexems, i - 1);
        if (lexems.get(find - 1).size() > 1 && lexems.get(find - 1).get(1).word.equals("ELSE")) {
            Node elsee = new Node("ELSE");
            int findese = findEnd(lexems, find - 2);
            Node teleportation = new Node(findese + "");
            elsee.right = teleportation;
            Node left = new Node(""+(find));
            elsee.left = left;
            trees.put(find - 1, elsee);
        }
        Node teleportation = new Node(find + "");
        operation.left = condition;
        operation.right = teleportation;
        return operation;
    }

    private static Node varSet(List<Lexer.Lexem> el) {
        Node id = new Node(el.get(0).word);
        Node operation = new Node("=");
        el.remove(0);
        el.remove(0);
        Node res = getRes(el);
        operation.left = id;
        operation.right = res;
        return operation;
    }

    private static Node getRes(List<Lexer.Lexem> el) {
        List<Lexer.Lexem>[] res = split(el, OR);
        if (res[0].size() == 0) {
            return getAnd(res[1]);
        } else {
            Node ress = new Node(curSplit);
            Node right= getAnd(res[1]);
            Node left  = getRes(res[0]);
            ress.left = left;
            ress.right = right;
            return ress;
        }
    }

    private static Node getAnd(List<Lexer.Lexem> el) {
        List<Lexer.Lexem>[] res = split(el, AND);
        if (res[0].size() == 0) {
            return getEq(res[1]);
        } else {
            Node ress = new Node(curSplit);
            Node right= getEq(res[1]);
            Node left  = getAnd(res[0]);
            ress.left = left;
            ress.right = right;
            return ress;
        }
    }

    private static Node getEq(List<Lexer.Lexem> el) {
        List<Lexer.Lexem>[] res = split(el, EQ);
        if (res[0].size() == 0) {
            return getOtn(res[1]);
        } else {
            Node ress = new Node(curSplit);
            Node right= getOtn(res[1]);
            Node left  = getEq(res[0]);
            ress.left = left;
            ress.right = right;
            return ress;
        }
    }

    private static Node getOtn(List<Lexer.Lexem> el) {
        List<Lexer.Lexem>[] res = split(el, OTN);
        if (res[0].size() == 0) {
            return getAdd(res[1]);
        } else {
            Node ress = new Node(curSplit);
            Node right= getAdd(res[1]);
            Node left  = getOtn(res[0]);
            ress.left = left;
            ress.right = right;
            return ress;
        }
    }

    private static Node getAdd(List<Lexer.Lexem> el) {
        List<Lexer.Lexem>[] res = split(el, ADD);
        if (res[0].size() == 0) {
            return getMLT(res[1]);
        } else {
            Node ress = new Node(curSplit);
            Node right = getMLT(res[1]);
            Node left = getAdd(res[0]);
            ress.left = left;
            ress.right = right;
            return ress;
        }
    }

    private static Node getMLT(List<Lexer.Lexem> el) {
        List<Lexer.Lexem>[] res = split(el, MLT);
        if (res[0].size() == 0) {
            return getUnr(res[1]);
        } else {
            Node ress = new Node(curSplit);
            Node right= getUnr(res[1]);
            Node left  = getMLT(res[0]);
            ress.left = left;
            ress.right = right;
            return ress;
        }
    }

    private static Node getUnr(List<Lexer.Lexem> el) {
        List<Lexer.Lexem>[] res = split(el, UNR);
        if(unarFind){
            Node ress = new Node(curSplit);
            Node left = simple(res[1]);
            ress.left = left;
            return ress;
        } else {
            return simple(res[1]);
        }
    }

    private static Node setParams(List<Lexer.Lexem> el) {
        Node param = new Node("param");
        List<Lexer.Lexem> eles = new ArrayList<>();
        try {
            eles.add(el.get(0));
        } catch (Exception ex) {
            System.err.println("GENERAL PARSER ERROR IN LINE " + i);
        }
        Node res = simple(eles);
        el.remove(0);
        Node next = null;
        if (el.size() > 0) {
            next = setParams(el);
        }
        param.right = res;
        param.center = next;
        return param;
    }
    private static Node simple(List<Lexer.Lexem> el) {
        if (el.size() == 0) {
            System.err.println("EMPTY OPERAND IN LINE  " + i);
        }
        if (el.size() > 1) {
            Node operation = new Node("METHOD");
            Node name = new Node("methodName");
            Node methodName = new Node(el.get(0).word);
            if(unusedmeths.contains(el.get(0).word)){
                unusedmeths.remove(el.get(0).word);
            }
            name.left = methodName;

            Node writing = setParams(el.get(1).collection);

            operation.center = name;
            name.center = writing;
            return operation;
        }
        if (el.get(0).word.equals("()")) {
            return getRes(el.get(0).collection);
        }
        return new Node(el.get(0).word);
    }

    private static boolean isVarCreate(List<Lexer.Lexem> el) {
        if (el.get(0).word.equals(varTypes[0]) || el.get(0).word.equals(varTypes[1])) {
            return true;
        }
        return false;
    }

    private static Node createVariable(List<Lexer.Lexem> el) {//
        Node type = new Node(el.get(0).word);
        Node id = new Node(el.get(1).word);
        Node create = new Node("CREATE");
        create.left = type;
        create.right = id;
        return create;
    }

    private static String curSplit = "";
    private static  boolean unarFind = false;
    private static List<Lexer.Lexem>[] split(List<Lexer.Lexem> els, List<String> splitter) {
        unarFind = false;
        List<Lexer.Lexem>[] splits = new List[2];
        List<Lexer.Lexem> left = new ArrayList<>();
        List<Lexer.Lexem> right = new ArrayList<>();
        int lastSplitter = -1;
        for (Lexer.Lexem el : els) {
            if(splitter.contains(el.word)) {
                lastSplitter = els.indexOf(el);
                curSplit = el.word;
            }
        }
        if(UNR.contains(curSplit)){
            unarFind = true;
        }
        for (int j = 0; j < els.size(); j++) {
            if (j < lastSplitter) {
                left.add(els.get(j));

            }
            if (j > lastSplitter) {
                right.add(els.get(j));
            }
        }
        splits[0] = left;
        splits[1] = right;
        return splits;
    }
}
