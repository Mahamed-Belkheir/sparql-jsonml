package codes.mahamed.sparqljsonml;


public class Lexer {
    private String text;
    private int currentIndex = 0;
    private Context ctx = Context.outside;
    private char quoteBreak = '"';
    private boolean ended = false;

    public Lexer(String text) {
        this.text = text;
    }

    public boolean hasNext() {
        return !ended && currentIndex < text.length();
    }

    private interface ExtractorFunc {
        String run() throws Exception;
    }
    private ExtractorFunc[] extractorList = new ExtractorFunc[]{
        () -> isEscaped(),
        () -> isTrippleQuotes("\"\"\""),
        () -> isTrippleQuotes("'''"),
        () -> isSingleQuote(),
    };

    public String next() throws Exception {
        String word = "";
        while (hasNext() && !atTerminal()) {
            String result;
            boolean jump = false;
            for (ExtractorFunc func : extractorList) {
                result = func.run();
                if (result != "") {
                    word += result;
                    jump = true;
                    break;
                }
            }
            if (jump) {
                continue;
            }
            if (currentIndex < text.length()) {
                word += text.charAt(currentIndex);
                move(1);
            }
        }
        move(1);
        return word;
    }

    private String isEscaped() {
        
        if (text.charAt(currentIndex) == '\\') {
            String res = text.substring(currentIndex, currentIndex + 2);
            // System.out.println("escaping: " +res +": " +currentIndex +": " + currentIndex +2);
            move(2);
            return res;
        }
        return "";
    }

    private String isSingleQuote() throws Exception {
        // System.out.println("got in single quote");
        if (!inLimit(1))
            return "";
        char a = text.charAt(currentIndex);
        if (a == '\'' || a == '"') {
            // System.out.println("in single quote");
            switch (ctx) {
            case inQuotes:
                if (a == quoteBreak) {
                    ctx = Context.outside;
                }
                break;
            case outside:
                if (currentIndex > 0) {
                    char prev = text.charAt(currentIndex - 1);
                    if (prev != '\n' && prev != ' ') {
                        throw new Exception("unexpected quote at: " + currentIndex);
                    }
                }
                quoteBreak = a;
                ctx = Context.inQuotes;
                break;
            case inTripleQuotes:
                break;
            }
            move(1);
            return a + "";
        }
        return "";
    }

    private String isTrippleQuotes(String quote) throws Exception {
        // System.out.println("got in tripple quote");
        if (!inLimit(2))
            return "";
        String a = text.substring(currentIndex, currentIndex + 3);
        // System.out.println("got :" + a + "expected :" + quote);
        if (a.equals(quote)) {
            // System.out.println("is double quotes");
            switch (ctx) {
            case inQuotes: {
                if (quoteBreak == a.charAt(0)) {
                    throw new Exception("unexpected tripple quotes " + a + " at " + currentIndex);
                } else {
                    move(3);
                    return a;
                }
            }
            case inTripleQuotes: {
                if (quoteBreak == a.charAt(0)) {
                    ctx = Context.outside;
                    move(3);
                    return a;
                } else {
                    move(3);
                    return a;
                }
            }
            case outside: {
                quoteBreak = a.charAt(0);
                ctx = Context.inTripleQuotes;
                move(3);
                return a;
            }
            default:
                break;
            }
        }
        return "";
    }

    private void move(int steps) {
        // System.out.println("moved by " + steps);
        // System.out.println("context :" + ctx );
        currentIndex += steps;
    }

    private boolean inLimit(int steps) {
        return steps + currentIndex < text.length();
    }

    private boolean atTerminal() {
        char a = text.charAt(currentIndex);
        return ((a == '\s' || a == '\n') && ctx == Context.outside);
    }

    private enum Context {
        outside, inQuotes, inTripleQuotes
    }

}
