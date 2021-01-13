package vm2gol_v2

import vm2gol_v2.type.Node
import vm2gol_v2.type.NodeList
import vm2gol_v2.type.Token
import vm2gol_v2.type.TokenKind
import vm2gol_v2.util.RegexWrapper
import vm2gol_v2.util.Json
import vm2gol_v2.util.Utils

class Tokenizer {

    companion object {
        fun run() {
            Tokenizer().main()
        }
    }

    fun main() {
        val src = Utils.readStdinAll()
        val tokens = tokenize(src)
        printTokens(tokens)
    }

    // --------------------------------

    fun tokenize(src: String): MutableList<Token> {
        val tokens: MutableList<Token> = mutableListOf()
        var pos = 0

        val re = RegexWrapper()

        while (pos < src.length) {
            val rest = src.substring(pos)

            if (re.match("^([ \n]+)", rest)) {
                val s = re.group(1)
                pos += s.length

            } else if (re.match("^(//.*)", rest)) {
                val s = re.group(1)
                pos += s.length

            } else if (re.match("^\"(.*)\"", rest)) {
                val s = re.group(1)
                tokens.add(Token(TokenKind.STR, s))
                pos += s.length + 2

            } else if (re.match("^(func|set|var|call_set|call|return|case|while|_cmt)[^a-z_]", rest)) {
                val s = re.group(1)
                tokens.add(Token(TokenKind.KW, s))
                pos += s.length

            } else if (re.match("^(-?[0-9]+)", rest)) {
                val s = re.group(1)
                tokens.add(Token(TokenKind.INT, s))
                pos += s.length

            } else if (re.match("^(==|!=|[(){}=+*,;])", rest)) {
                val s = re.group(1)
                tokens.add(Token(TokenKind.SYM, s))
                pos += s.length

            } else if (re.match("^([a-z_][a-z0-9_\\[\\]]*)", rest)) {
                val s = re.group(1)
                tokens.add(Token(TokenKind.IDENT, s))
                pos += s.length

            } else {
                val pre = src.substring(0, pos)
                val post = src.substring(pos)

                Utils.putskv_e("pre", pre)
                Utils.putskv_e("post", post)

                throw Utils.panic("Unexpected pattern")
            }
        }

        return tokens
    }

    fun printTokens(tokens: List<Token>) {
        tokens.forEach { t ->
            Utils.puts(t.toLine())
        }
    }

}
