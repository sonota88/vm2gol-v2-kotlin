package vm2gol_v2.util

class RegexWrapper {
    var result: MatchResult? = null

    fun match(pattern: String, str: String): Boolean {
        this.result = Regex(pattern).find(str)
        return this.result != null
    }

    fun group(n: Int): String {
        val result = this.result?.groupValues?.get(n)

        if (result == null) {
            throw RuntimeException("must not happen")
        } else {
            return result!!
        }
    }

}
