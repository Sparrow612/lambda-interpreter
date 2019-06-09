简单总结Lambda中类的职责以及思路

#逻辑主体为lexer、parser、interpreter，建议按照此顺序编写代码
+Lexer
 -数据成员有source、token、tokenvalue（仅在token为LCID时起作用）
 -包含私有nextChar()方法
 -nextToken() 调用nextChar()方法为数据成员Token赋值
 -next() 返回值为布尔类型，参数为TokenType,判断当前token类型是否与参数的类型一致
 -skip() 返回值同样为布尔，首先调用next()方法，如果True，则调用nextToken()
 -match()思路和skip一样，只不过匹配失败的话，直接报错（意思一下就行，不一定非得抛出异常）

 +Parser
  -是lexer工具的使用者
  -将lambda表达式转化为德布鲁因形式
  -ctx代表上下文，java版本中它的数据类型是个ArrayList，每次读到一个参数，将它的name存入ctx的0号位，这样每当读到新的参数，我们总能保证内部参数的index小
  -在将一个参数的value设置为德布鲁因index值后，我们需要将它remove(以免影响后续计算)
  -如果出现了\x.y这样的情况，德布鲁因形式为\.1，赋值手段为value=""+ctx.size()

 +Interpreter
  -最难的部分，实际进行运算操作的类
  -具体怎么写shift()和subst()代码详见67老师注释，或者照着JS写即可，下面说说evalAST方法的编写思路 
        /**思路
         * 首先判断整体是不是一个application
         * 如果是，那么先讨论左，分别按左树是application、abstraction、identifier的情况讨论
         *     其中，左abstraction的情况又需要考虑右树木的情况，再进行substitute操作
               左application的情况，需要先计算application,如果计算完成后左还是个application，意味着左树是（LCID LCID），直接计算右树木，然后return
         *     再说左identifier的情况，无需计算，进行分叉，讨论右树是application、abstraction、identifier的情况，每种情况处理完，return
         * 整体是abstraction的话，对body部分求值后return
         * 整体是identifier，直接return
         */

#AST为抽象类，它有三个子类，分别代表Lambda演算中的三个节点
+AST
 -Abastraction 构造器有两个参数，一个为参数（Identifier），一个为函数体（函数体可以是Abstraction，可以是Application，也可以是Identifier）
 -Application 具有左树和与右树，求值方法已在67老师提供的Readme中给出
 -Identifier 就是个标识符，有name和value（德布鲁因Index值），在运算以及打印中起作用的是value

 #结果
 有效Lambda程式最后返回的结果一定是一个Abstration，但Abstraction却不一定是最终值（考虑函数体是Application的情况）如\f.\x.((\f.\x.x) x)就可进一步规约