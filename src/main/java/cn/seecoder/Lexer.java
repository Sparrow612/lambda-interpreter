package cn.seecoder;



import java.util.regex.Pattern;

public class Lexer{

    public String source;
    public int index;
    public TokenType token;//当前的Token类型
    public String tokenvalue="";//用于保存当前token的标识符

    private String pattern="[a-zA-Z]";

    public Lexer(String s){
        index = 0;
        source=s;
        nextToken();
    }
    //get next token
    private void nextToken(){
        char c;
        String inner_pattern="\\s";
        StringBuffer stringBuffer;
        do {
            c = nextChar();
        }while(Pattern.matches(inner_pattern,Character.toString(c)));
        switch (c) {
            case '\\':
                token = TokenType.LAMBDA;
                tokenvalue = "\\";
                break;
            case '(':
                token = TokenType.LPAREN;
                tokenvalue = "(";
                break;
            case ')':
                token = TokenType.RPAREN;
                tokenvalue = ")";
                break;
            case '.':
                token = TokenType.DOT;
                tokenvalue = ".";
                break;
            case '\0':
                token = TokenType.EOF;
                tokenvalue = "\0";
                break;

                default: if (Pattern.matches(pattern, Character.toString(c))) {
                    stringBuffer=new StringBuffer();
                    do {
                        stringBuffer.append(c);
                        c = nextChar();
                    }while (Pattern.matches(pattern, Character.toString(c)));
                    index--;
                    tokenvalue = stringBuffer.toString();
                    token = TokenType.LCID;

                }
        }
        System.out.println(token);
    }

    // get next char
    private char nextChar(){
        if(index>=source.length()){
            return '\0';
        }
        return source.charAt(index++);
    }
    //check token == t
    public boolean next(TokenType t){
        return token.equals(t);
    }

    //assert matching the token type, and move next token
    public void match(TokenType t){
        if(next(t)){
            nextToken();
            return;
        }else{
            System.out.println("Fail to match!");
            return;
        }
    }

    //skip token and move next token
    public boolean skip(TokenType t){
        if(next(t)){
            nextToken();
            return true;
        }
        return false;
    }//skip方法首先会调用next方法


}
