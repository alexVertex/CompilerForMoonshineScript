public class Errors {
    public static void ERR_noSuchVariable(String Name){
        System.err.println("ПЕРЕМЕННАЯ " + Name + " НЕ НАЙДЕНА");
        System.exit(1);
    }
    public static void ERR_unknownToken(String Name){
        System.err.println("НЕИЗВЕСТНЫЙ ТОКЕН " + Name + " - ЕГО БЫТЬ НЕ ДОЛЖНО");
        System.exit(1);
    }
}
