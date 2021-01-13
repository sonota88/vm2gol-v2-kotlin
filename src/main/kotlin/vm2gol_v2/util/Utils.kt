package vm2gol_v2.util

class Utils {

    companion object {

        const val DQ = "\""
        const val LF = "\n"

        fun puts(x: Any?) {
            println(x)
        }

        fun puts_e(x: Any?) {
            System.err.println(x)
        }

        fun putskv_e(k: String, v: Any?) {
            val vstr = if (v == null) "null" else v.toString()
            puts_e("${k} (${vstr})")
        }

        fun slice(
            xs: List<String>, from: Int, until: Int
        ): List<String>
        {
            val size = until - from
            val newXs = MutableList<String>(size) { "" }

            for ( (i, x) in xs.withIndex() ) {
                if (from <= i && i < until) {
                    newXs.set(i - from, x)
                }
            }

            return newXs
        }

        fun inspect(obj: Any?): String {
            if (obj == null) {
                return "null"
            } else {
                if (obj is Int) {
                    return obj.toString()
                } else if (obj is String) {
                    return inspectString(obj)
                } else if (obj is Boolean) {
                    return obj.toString()
                } else if (isList(obj)) {
                    return inspectList(obj)
                } else {
                    // val className = obj::class.toString()
                    // throw RuntimeException("not yet impl (${className})")
                    return obj.toString()
                }
            }
        }

        fun inspectString(s: String): String {
            return (
                DQ +
                s
                    .replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\"", "\\\"")
                    .replace("\t", "\\t") +
                DQ
            )
        }

        fun isList(obj: Any): Boolean {
            val className = (obj::class).toString()
            return (
                className == "class java.util.ArrayList" ||
                className == "class java.util.Arrays\$ArrayList" ||
                className == "class java.util.Collections\$SingletonList"
            )
        }

        fun inspectList(obj: Any): String {
            val xs = obj as List<Any>
            var s = "["

            for ( (i, x) in xs.withIndex() ) {
                if (1 <= i) {
                    s += ", "
                }
                s += inspect(x)
            }

            return s + "]"
        }

        fun strEq(s1: String?, s2: String?): Boolean {
            return s1 == s2
        }

        fun isNumber(str: String): Boolean {
            return RegexWrapper().match("""^[0-9]+""", str)
        }

        fun readStdinAll(): String {
            var s: String = ""

            while (true) {
                val line : String? = readLine()
                if (line == null) {
                    break
                }
                s += line + LF
            }

            return s
        }

        fun panic(msg: String): RuntimeException {
            return RuntimeException("PANIC " + msg)
        }

        fun panic(msg: String, x: Any?): RuntimeException {
            return RuntimeException("PANIC " + msg + " (${ inspect(x) })")
        }

    }

}
