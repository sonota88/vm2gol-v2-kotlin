package vm2gol_v2

import vm2gol_v2.type.Node
import vm2gol_v2.type.NodeKind
import vm2gol_v2.type.NodeList
import vm2gol_v2.type.Token
import vm2gol_v2.type.TokenKind
import vm2gol_v2.util.Json
import vm2gol_v2.util.Utils

class Parser {

    companion object {
        fun run() {
            Parser().main()
        }
    }

    var pos = 0
    var tokens: MutableList<Token> = mutableListOf()

    fun main() {
        val src = Utils.readStdinAll()
        this.tokens = toTokens(src)
        val topStmts = parse()

        Json.print(topStmts.getItems())
    }

    // --------------------------------

    fun toTokens(src: String): MutableList<Token> {
        val lines = src.split(Utils.LF)

        return lines
            .filter { it != "" }
            .map { Token.fromLine(it) }
            .toMutableList()
    }

    // --------------------------------
    
    fun isEnd(): Boolean {
        return this.tokens.size <= this.pos
    }

    fun peek(n: Int): Token {
        return this.tokens.get(this.pos + n)
    }

    fun peek(): Token {
        return peek(0)
    }

    fun assertValue(t: Token, type: TokenKind, expected: String) {
        if (! t.kindEq(type)) {
            throw Utils.panic("invalid token kind: expected(${type} ${expected}) actual(${t})")
        }

        if (Utils.strEq(t.getStr(), expected)) {
            // OK
        } else {
            val msg = String.format(
                    "Assertion failed: exp(%s) act(%s)",
                    expected,
                    t
                    )
            throw Utils.panic(msg)
        }
    }

    fun assertValue_sym(pos: Int, exp: String) {
        val t = this.tokens.get(pos)
        assertValue(t, TokenKind.SYM, exp)
    }

    fun assertValue_kw(pos: Int, exp: String) {
        val t = this.tokens.get(pos)
        assertValue(t, TokenKind.KW, exp)
    }

    fun consumeKw(s: String) {
        assertValue_kw(this.pos, s)
        this.pos++
    }

    fun consumeSym(s: String) {
        assertValue_sym(this.pos, s)
        this.pos++
    }

    // --------------------------------

    fun parseArg(): Node {
        // puts_e("parseArg")

        val t = peek()

        when (t.getKind()) {
            TokenKind.IDENT -> {
                pos++
                return Node.of(t.getStr())
            }
            TokenKind.INT -> {
                pos++
                return Node.of(t.getIntVal())
            }
            else -> {
                throw Utils.panic("invalid type")
            }
        }
    }
    
    fun parseArgs_first(): Node? {
        // puts_fn("parseArgs_first")

        if (peek().is_(TokenKind.SYM, ")")) {
            return null
        }

        return parseArg()
    }

    fun parseArgs_rest(): Node? {
        // puts_fn("parseArgs_rest")

        if (peek().is_(TokenKind.SYM, ")")) {
            return null
        }

        consumeSym(",")

        return parseArg()
    }

    fun parseArgs(): NodeList {
        // puts_fn("parseArgs")

        val args = NodeList()

        val firstArg = parseArgs_first()
        if (firstArg == null) {
            return args
        }
        args.add(firstArg)

        while (true) {
            val restArg = parseArgs_rest()
            if (restArg == null) {
                break
            }
            args.add(restArg)
        }

        return args
    }

    fun parseFunc(): NodeList {
        consumeKw("func")

        val fnName = peek().getStr()
        this.pos++

        consumeSym("(")
        val args = parseArgs()
        consumeSym(")")

        consumeSym("{")
        val stmts = parseStmts()
        consumeSym("}")

        return NodeList()
            .add("func")
            .add(fnName)
            .add(args)
            .add(stmts)
    }

    fun parseVar_declare(): NodeList {
        val t = peek()
        this.pos++
        val varName = t.getStr()

        consumeSym(";")

        return NodeList()
            .add("var")
            .add(varName)
    }

    fun parseVar_init(): NodeList {
        val t = peek()
        this.pos++
        val varName = t.getStr()

        consumeSym("=")

        val expr = parseExpr()

        consumeSym(";")

        return NodeList()
                .add("var")
                .add(varName)
                .add(expr)
                
    }

    fun parseVar(): NodeList {
        // Utils.puts_e("    -->> parseVar")

        consumeKw("var")

        val t = peek(1)
        if (t.strEq(";")) {
            return parseVar_declare()
        } else if (t.strEq("=")) {
            return parseVar_init()
        } else {
            throw Utils.panic("unexpected token")
        }
    }

    fun parseExprRight(exprL: Node): Node {
        val t = peek()

        if (t.is_(TokenKind.SYM, ";") || t.is_(TokenKind.SYM, ")")) {
            return exprL
        }

        var expr: NodeList

        if (t.is_(TokenKind.SYM, "+")) {
            consumeSym("+")
            val exprR = parseExpr()
            expr = NodeList()
                .add("+")
                .add(exprL)
                .add(exprR)
        } else if (t.is_(TokenKind.SYM, "*")) {
            consumeSym("*")
            val exprR = parseExpr()
            expr = NodeList()
                .add("*")
                .add(exprL)
                .add(exprR)
        } else if (t.is_(TokenKind.SYM, "==")) {
            consumeSym("==")
            val exprR = parseExpr()
            expr = NodeList()
                .add("eq")
                .add(exprL)
                .add(exprR)
        } else if (t.is_(TokenKind.SYM, "!=")) {
            consumeSym("!=")
            val exprR = parseExpr()
            expr = NodeList()
                .add("neq")
                .add(exprL)
                .add(exprR)
        } else {
            throw Utils.panic("unsupported")
        }

        return Node.of(expr)
    }

    fun parseExpr(): Node {
        val tl = peek()

        if (tl.is_(TokenKind.SYM, "(")) {
            consumeSym("(")
            val exprL = parseExpr()
            consumeSym(")")

            return parseExprRight(exprL)
        }

        var exprL: Node

        when (tl.getKind()) {
            TokenKind.INT -> {
                pos++
                exprL = Node.of(tl.getIntVal())
                return parseExprRight(exprL)
            }
            TokenKind.IDENT -> {
                pos++
                exprL = Node.of(tl.getStr())
                return parseExprRight(exprL)
            }
            else -> {
                throw Utils.panic("invalid type")
            }
        }
    }

    fun parseSet(): NodeList {
        consumeKw("set")

        val t = peek()
        pos++
        val varName = t.getStr()
        
        consumeSym("=")

        val expr = parseExpr()

        consumeSym(";")

        return NodeList()
                .add("set")
                .add(varName)
                .add(expr)
    }

    fun parseFuncall(): NodeList {
        val t = peek()
        pos++
        val fnName = t.getStr()

        consumeSym("(")
        val args = parseArgs()
        consumeSym(")")

        return NodeList()
            .add(fnName)
            .addAll(args)
    }

    fun parseCall(): NodeList {
        consumeKw("call")

        val funcall = parseFuncall()

        consumeSym(";")

        return NodeList()
            .add("call")
            .addAll(funcall)
    }

    fun parseCallSet(): NodeList {
        consumeKw("call_set")

        val t = peek()
        pos++
        val varName = t.getStr()
        
        consumeSym("=")

        val funcall = parseFuncall()

        consumeSym(";")

        return NodeList()
            .add("call_set")
            .add(varName)
            .add(funcall)
    }

    fun parseReturn(): NodeList {
        consumeKw("return")

        if (peek().is_(TokenKind.SYM, ";")) {
            // 引数なしの return
            throw Utils.panic("not_yet_impl")
        } else {
            val expr = parseExpr()
            consumeSym(";")

            return NodeList()
                .add("return")
                .add(expr)
        }
    }

    fun parseWhile(): NodeList {
        consumeKw("while")

        consumeSym("(")
        val expr = parseExpr()
        consumeSym(")")

        consumeSym("{")
        val stmts = parseStmts()
        consumeSym("}")

        return NodeList()
            .add("while")
            .add(expr)
            .add(stmts)
    }

    fun parseWhenClause(): NodeList {
        val t = peek()
        if (t.is_(TokenKind.SYM, "}")) {
            return NodeList.empty()
        }

        consumeSym("(")
        val expr = parseExpr()
        consumeSym(")")

        consumeSym("{")
        val stmts = parseStmts()
        consumeSym("}")

        return NodeList()
            .add(expr)
            .addAll(stmts)
    }

    fun parseCase(): NodeList {
        consumeKw("case")

        consumeSym("{")

        val whenClauses = NodeList()

        while (true) {
            val whenClause = parseWhenClause()
            if (whenClause.isEmpty()) {
                break
            }

            whenClauses.add(whenClause)
        }
        
        consumeSym("}")

        return NodeList()
            .add("case")
            .addAll(whenClauses)
    }

    fun parseVmComment(): NodeList {
        consumeKw("_cmt")
        consumeSym("(")

        val t = peek()
        pos++
        val comment = t.getStr()
        
        consumeSym(")")
        consumeSym(";")

        return NodeList()
            .add("_cmt")
            .add(comment)
    }
    
    fun parseStmt(): NodeList {
        var t = peek()

        if (t.is_(TokenKind.SYM, "}")) {
            return NodeList.empty()
        }

        when (t.getStr()) {
            "func"     -> { return parseFunc()      }
            "var"      -> { return parseVar()       }
            "set"      -> { return parseSet()       }
            "call"     -> { return parseCall()      }
            "call_set" -> { return parseCallSet()   }
            "return"   -> { return parseReturn()    }
            "while"    -> { return parseWhile()     }
            "case"     -> { return parseCase()      }
            "_cmt"     -> { return parseVmComment() }
            else -> {
                throw Utils.panic("Unexpected token", t)
            }
        }
    }

    fun parseStmts(): NodeList {
        val stmts = NodeList()

        while (true) {
            if (isEnd()) {
                break
            }

            val stmt = parseStmt()
            if (stmt.isEmpty()) {
                break
            }

            stmts.add(stmt)
        }

        return stmts
    }

    fun parseTopStmt(): NodeList {
        val t = peek()

        if (t.is_(TokenKind.KW, "func")) {
            return parseFunc()
        } else {
            throw Utils.panic("Unexpected token", t)
        }
    }

    fun parseTopStmts(): Node {
        val stmts = NodeList()

        while (true) {
            if (isEnd()) {
                break
            }

            stmts.add(parseTopStmt())
        }

        return Node.of(
            NodeList()
                .add("top_stmts")
                .addAll(stmts)
        )
    }

    fun parse(): Node = parseTopStmts()

    fun puts_fn(fnName: String) {
        Utils.puts_e("    -->> " + fnName)
    }

}
