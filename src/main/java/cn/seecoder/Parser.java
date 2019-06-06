package cn.seecoder;

import java.util.ArrayList;

public class Parser {
    Lexer lexer;
    ArrayList<String> ctx=new ArrayList<String>();
    public Parser(Lexer l){
        lexer = l;
    }
    public AST parse(){
        AST ast = term(ctx);
        //System.out.println(lexer.match(TokenType.EOF));
        return ast;
    }
    private AST term(ArrayList<String> ctx){
        // write your code here
        String param;
        String paramValue;
        AST inner_term;
        if(lexer.skip(TokenType.LAMBDA)) {
            param = lexer.tokenvalue;
            lexer.match(TokenType.LCID);
            //赋值，跳到下一个TOKEN，下一个Token必然是DOT
            lexer.match(TokenType.DOT);
            ctx.add(0, param);//后加入的在前
            paramValue = "" + ctx.indexOf(param);//这里的赋值其实不算数，后面的Atom方法会重新为paramValue赋予正确的index值
            inner_term = term(ctx);
            ctx.remove(param);
            return new Abstraction(new Identifier(param, paramValue), inner_term);
        }else{
            return application(ctx);
        }
    }
    private AST application(ArrayList<String> ctx){
        // write your code here
        AST lhs=atom(ctx);
        AST rhs;
        while(true){
            rhs=atom(ctx);
            if(rhs==null){
                return lhs;
            }else{
                lhs=new Application(lhs,rhs);
            }
        }
    }//application方法更像一个传声筒，它的作用在于联结term方法和atom方法
    private AST atom(ArrayList<String> ctx){
        // write your code here
        String param;
        String paramValue;
        AST inner_term;
        if(lexer.skip(TokenType.LPAREN)){
            inner_term=term(ctx);
            if(lexer.skip(TokenType.RPAREN))
                return inner_term;
        }else if(lexer.next(TokenType.LCID)){
            param=lexer.tokenvalue;
            if(ctx.contains(param))
                paramValue=""+ctx.indexOf(param);
            else
                paramValue=""+ctx.size();//判断是否绑定，分别赋予index值
            lexer.match(TokenType.LCID);//跳过LCID
            return new Identifier(param,paramValue);
        }
        return  null;
        //返回一个term或者一个Identifier
    }
}
