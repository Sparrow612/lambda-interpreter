package cn.seecoder;

public class Interpreter {
    Parser parser;
    AST astAfterParser;
    public Interpreter(Parser p){
        parser = p;
        astAfterParser = p.parse();
        //System.out.println("After parser:"+astAfterParser.toString());
    }
    private  boolean isAbstraction(AST ast){
        return ast instanceof Abstraction;
    }
    private  boolean isApplication(AST ast){
        return ast instanceof Application;
    }
    private boolean isIdentifier(AST ast){
        return ast instanceof Identifier;
    }

    public AST eval(){
        return evalAST(astAfterParser);
    }

    private AST evalAST(AST ast) {
        //write your code here
        /**思路
         * 首先判断整体是不是一个application
         * 如果是，那么先讨论左，分别按左树是application、abstraction、identifier的情况讨论
         * 其中，左abstraction的情况又需要考虑右树木的情况，再进行substitute操作
         * 再说左identifier的情况，此时进一步讨论右树是application、abstraction、identifier的情况
         * 整体是abstraction的话，对body部分求值
         * 整体是identifier，直接return
         */
       while(true){
           if(isApplication(ast)){
               if(isApplication(((Application)ast).lhs)){
                   ((Application)ast).lhs=evalAST(((Application)ast).lhs);
                   //发现是application,则进行计算
                   if(isApplication(((Application)ast).lhs)){
                       ((Application)ast).rhs=evalAST(((Application)ast).rhs);
                       return ast;
                       //如果处理完左树发现它仍然是个Application，我们可以断言是(LCID LCID)的形式，这时候我们只需处理右树，然后返回
                   }
               }else if(isAbstraction(((Application)ast).lhs)){
                    if(isApplication(((Application)ast).rhs)){
                        ((Application)ast).rhs=evalAST(((Application)ast).rhs);
                    }
                    ast=substitute(((Abstraction)((Application)ast).lhs).body,((Application) ast).rhs);
               }else {
                   //左树处理完毕后再处理右树，右树三种情况，处理完毕后即可返回结果了
                   if(isApplication(((Application)ast).rhs)){
                       ((Application)ast).rhs=evalAST(((Application)ast).rhs);
                       return ast;
                   }else if(isAbstraction(((Application)ast).rhs)){
                       ((Application)ast).rhs=evalAST(((Application)ast).rhs);
                       return ast;
                   }else {
                       return ast;//左树右数都是identifier
                   }
               }
           }else if(isAbstraction(ast)){
               //需要return
               return new Abstraction(((Abstraction)ast).param,
                        evalAST(((Abstraction)ast).body));
           }else if(isIdentifier(ast)){
               return ast;
           }else{
               System.out.println("Wrong!");
           }
       }
    }//好好想想怎么写evalAST方法 19/06/05 9:45
    private AST substitute(AST node,AST value){
        return shift(-1,subst(node,shift(1,value,0),0),0);
        //先把value中的自由变量index值抬高，替换完成后再降
    }
    /**
     *  value替换node节点中的变量：
     *  如果节点是Applation，分别对左右树替换；
     *  如果node节点是abstraction，替入node.body时深度得+1；
     *  如果node是identifier，则替换De Bruijn index值等于depth的identifier（替换之后value的值加深depth）

     *@param value 替换成为的value
     *@param node 被替换的整个节点
     *@param depth 外围的深度
     *@return AST
     *@exception  (方法有异常的话加)
     */
    private AST subst(AST node, AST value, int depth){
        //write your code here
        if (isApplication(node)) {
            return new Application(
                    subst(((Application)node).lhs,value,depth),
                    subst(((Application)node).rhs,value,depth)
            );
        } else if (isAbstraction(node)) {
            return new Abstraction(
                    ((Abstraction)node).param,
                    subst(((Abstraction)node).body,value,depth+1)
            );
        } else {
            Identifier identifier = (Identifier) node;
            if (Integer.parseInt(identifier.value) == depth) {
                return shift(depth, value, 0);
            }
            return identifier;
        }
    }
    /**
     *  De Bruijn index值位移
     *  如果节点是Applation，分别对左右树位移；
     *  如果node节点是abstraction，新的body等于旧node.body位移by（from得+1）；
     *  如果node是identifier，则新的identifier的De Bruijn index值如果大于等于from则加by，否则加0（超出内层的范围的外层变量才要shift by位）.
     *@param by 位移的距离
     *@param node 位移的节点
     *@param from 内层的深度
     *@return AST
     *@exception  (方法有异常的话加)
     */
    private AST shift(int by, AST node,int from){
        //write your code here
        if (isApplication(node)) {
           return new Application(
                   shift(by,((Application)node).lhs,from),
                   shift(by,((Application)node).rhs,from)
           );
        } else if (isAbstraction(node)) {
            return new Abstraction(
                    ((Abstraction)node).param,
                    shift(by,((Abstraction)node).body,from+1)
            );
        } else {
            int temp=Integer.parseInt(((Identifier)node).value);
            return new Identifier("",""+ (temp+(temp>=from?by:0)));
        }
    }//该方法用来改变index值

        static String ZERO = "(\\f.\\x.x)";
        static String SUCC = "(\\n.\\f.\\x.f (n f x))";
        static String ONE = app(SUCC, ZERO);
        static String TWO = app(SUCC, ONE);
        static String THREE = app(SUCC, TWO);
        static String FOUR = app(SUCC, THREE);
        static String FIVE = app(SUCC, FOUR);
        static String PLUS = "(\\m.\\n.((m " + SUCC + ") n))";
        static String POW = "(\\b.\\e.e b)";       // POW not ready
        static String PRED = "(\\n.\\f.\\x.n(\\g.\\h.h(g f))(\\u.x)(\\u.u))";
        static String SUB = "(\\m.\\n.n" + PRED + "m)";
        static String TRUE = "(\\x.\\y.x)";
        static String FALSE = "(\\x.\\y.y)";
        static String AND = "(\\p.\\q.p q p)";
        static String OR = "(\\p.\\q.p p q)";
        static String NOT = "(\\p.\\a.\\b.p b a)";
        static String IF = "(\\p.\\a.\\b.p a b)";
        static String ISZERO = "(\\n.n(\\x." + FALSE + ")" + TRUE + ")";
        static String LEQ = "(\\m.\\n." + ISZERO + "(" + SUB + "m n))";
        static String EQ = "(\\m.\\n." + AND + "(" + LEQ + "m n)(" + LEQ + "n m))";
        static String MAX = "(\\m.\\n." + IF + "(" + LEQ + " m n)n m)";
        static String MIN = "(\\m.\\n." + IF + "(" + LEQ + " m n)m n)";

    private static String app(String func, String x){
        return "(" + func + x + ")";
    }
    private static String app(String func, String x, String y){
        return "(" +  "(" + func + x +")"+ y + ")";
    }
    private static String app(String func, String cond, String x, String y){
        return "(" + func + cond + x + y + ")";
    }

    public static void main(String[] args) {
        // write your code here
        String[] sources = {
                ZERO,//0
                ONE,//1
                TWO,//2
                THREE,//3
                app(PLUS, ZERO, ONE),//4
                app(PLUS, TWO, THREE),//5
                app(POW, TWO, TWO),//6
                app(PRED, ONE),//7
                app(PRED, TWO),//8
                app(SUB, FOUR, TWO),//9
                app(AND, TRUE, TRUE),//10
                app(AND, TRUE, FALSE),//11
                app(AND, FALSE, FALSE),//12
                app(OR, TRUE, TRUE),//13
                app(OR, TRUE, FALSE),//14
                app(OR, FALSE, FALSE),//15
                app(NOT, TRUE),//16
                app(NOT, FALSE),//17
                app(IF, TRUE, TRUE, FALSE),//18
                app(IF, FALSE, TRUE, FALSE),//19
                app(IF, app(OR, TRUE, FALSE), ONE, ZERO),//20
                app(IF, app(AND, TRUE, FALSE), FOUR, THREE),//21
                app(ISZERO, ZERO),//22
                app(ISZERO, ONE),//23
                app(LEQ, THREE, TWO),//24
                app(LEQ, TWO, THREE),//25
                app(EQ, TWO, FOUR),//26
                app(EQ, FIVE, FIVE),//27
                app(MAX, ONE, TWO),//28
                app(MAX, FOUR, TWO),//29
                app(MIN, ONE, TWO),//30
                app(MIN, FOUR, TWO),//31
        };

        for(int i=0;i<sources.length; i++) {
            //为了调试bug我修改了i的初值，所以改完记得恢复,另外结束时候记得顺手把After parse那段注释掉
            //目前的问题是这样的：app(PLUS,FIRST,SECOND)只要第一个数超过了1，便会出错，替换没问题，应该是调用顺序的问题 19/06/04 12:16
            String source = sources[i];

            System.out.println(i+":"+source);

            Lexer lexer = new Lexer(source);

            Parser parser = new Parser(lexer);

            Interpreter interpreter = new Interpreter(parser);

            AST result = interpreter.eval();

            System.out.println(i+":" + result.toString());

        }

    }
}
