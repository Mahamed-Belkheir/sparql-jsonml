package codes.mahamed.sparqljsonml;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        // System.out.println("'''hello world''' + \\\"stuff'");
        Lexer lex = new Lexer("'''hello world''' + \\\"stuff'");
        try {
            while (lex.hasNext()) {
                System.out.println(lex.next());
            }
        } catch (Exception err) {
            System.out.println(err);
        }

    }
}
