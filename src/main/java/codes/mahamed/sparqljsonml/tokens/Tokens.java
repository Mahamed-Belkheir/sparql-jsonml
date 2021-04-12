package codes.mahamed.sparqljsonml.tokens;

import java.util.ArrayList;

public class Tokens {
    public class Token {
        String value;
        Type type;
        ArrayList<Token> children;
        public Token(String value, Type type, ArrayList<Token> children) {
            this.value = value;
            this.type = type;
            this.children = children;
        }
        public Token(String value, Type type) {
            this.value = value;
            this.type = type;
            this.children = new ArrayList<Token>();
        }
    }

    public enum Type {
        IRI,

    }

    public interface Function<T, S> {
        public S run(T t);
    }
}
