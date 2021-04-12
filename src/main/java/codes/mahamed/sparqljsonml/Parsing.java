package codes.mahamed.sparqljsonml;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;

import codes.mahamed.sparqljsonml.tokens.Tokens.Token;

public class Parsing {
    
    public interface Parser {
        public Result parse(Context ctx);
    }

    public class Result {
        public boolean success;
        public ArrayList<Token> value = new ArrayList<Token>();
        public ParseError issue;
        public Context ctx;
        
        public Result(Token value, Context ctx) {
            this.success = true;
            this.value.add(value);
            this.ctx = ctx;
        }

        public Result(ArrayList<Token> value, Context ctx) {
            this.success = true;
            this.value = value;
            this.ctx = ctx;
        }

        public Result(ParseError issue, Context ctx) {
            this.success = false;
            this.ctx = ctx;
            this.issue = issue;
        }
    }

    public class Context {
        public String[] tokens;
        public int index;

        public Context newContext() {
            Context ctx = new Context();
            ctx.index = this.index + 1;
            ctx.tokens = this.tokens;
            return ctx;
        }

        public Context newContext(int move) {
            Context ctx = new Context();
            ctx.index = this.index + move;
            ctx.tokens = this.tokens;
            return ctx;
        }
    }

    public class RegexMatch implements Parser {
        private Pattern rgx;
        private Function<String, Token> func;
        public RegexMatch(String pattern, Function<String, Token> func) {
            this.rgx = Pattern.compile(pattern);
            this.func = func;
        }
        public Result parse(Context ctx) {
            String tokenString = ctx.tokens[ctx.index];
            if (this.rgx.matcher(tokenString).matches()) {
                return new Result(func.apply(tokenString), ctx.newContext());
            }
            return new Result(new ParseError("expected token: " + tokenString, ctx.index), ctx);
        }
    }

    public class Sequence implements Parser {
        private Parser[] parsers;
        public Sequence(Parser ...parsers) {
            this.parsers = parsers;
        }
        public Result parse(Context ctx) {
            ArrayList<Token> tokens = new ArrayList<Token>();
            for (Parser parser: this.parsers) {
                Result res = parser.parse(ctx);
                ctx = res.ctx;
                if (!res.success) {
                    return new Result(res.issue, ctx);
                }
                tokens.addAll(res.value);
            }
            return new Result(tokens, ctx);
        }
    }

    public class Many implements Parser {
        private Parser[] parsers;
        public Many(Parser ...parsers) {
            this.parsers = parsers;
        }
        public Result parse(Context ctx) {
            ArrayList<Token> tokens = new ArrayList<Token>();
            for (Parser parser: this.parsers) {
                Result res = parser.parse(ctx);
                ctx = res.ctx;
                if (!res.success) {
                    break;
                }
                tokens.addAll(res.value);
            }
            return new Result(tokens, ctx);
        }
    }

    public class Any implements Parser {
        private Parser[] parsers;
        public Any(Parser ...parsers) {
            this.parsers = parsers;
        }
        public Result parse(Context ctx) {
            for (Parser parser: this.parsers) {
                Result res = parser.parse(ctx);
                if (res.success) {
                    return new Result(res.value, res.ctx);
                }
            }
            return new Result(new ParseError("No matching tokens", ctx.index), ctx);
        }
    }
    
    public class ParseError extends Exception {
        public String reason;
        public int index;
        public ParseError(String reason, int index) {
            this.reason = reason;
            this.index = index;
        }
    }
}
