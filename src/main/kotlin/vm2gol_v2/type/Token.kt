package vm2gol_v2.type

import vm2gol_v2.util.Utils

enum class TokenKind(val str: String) {
    INT("int"),
    STR("str"),
    KW("kw"),
    SYM("sym"),
    IDENT("ident"),
    NONE("none");

    companion object {
        fun of(value: String): TokenKind {
            when (value) {
                "int"   -> { return INT   }
                "str"   -> { return STR   }
                "kw"    -> { return KW    }
                "sym"   -> { return SYM   }
                "ident" -> { return IDENT }
                else  -> {
                    Utils.panic("not yet impl")
                    return NONE
                }
            }
        }
    }
}

class Token(kind: TokenKind, str: String) {

    companion object {

        fun fromLine(line: String): Token {
            val i = line.indexOf(":")
            val typeStr = line.substring(0, i)
            val value = line.substring(i + 1)

            return Token(TokenKind.of(typeStr), value)
        }

    }

    private val kind = kind
    private val str = str

    fun getKind(): TokenKind {
        return this.kind
    }

    fun getIntVal(): Int {
        if (this.kind != TokenKind.INT) {
            throw Utils.panic("invalid type")
        }

        return this.str.toInt()
    }

    fun getStr(): String {
        return this.str
    }
    
    fun strEq(str: String): Boolean {
        return Utils.strEq(this.str, str)
    }

    fun is_(kind: TokenKind, str: String): Boolean {
        return this.kind == kind && Utils.strEq(this.str, str)
    }

    fun toLine(): String {
        return this.kind.str + ":" + getStr()
    }

    fun kindEq(kind: TokenKind): Boolean {
        return this.kind == kind
    }

    override
    fun toString(): String {
        return "(Token " + kind + " " + Utils.inspect(str) + ")"
    }

}
