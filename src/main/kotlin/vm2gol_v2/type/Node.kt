package vm2gol_v2.type

import vm2gol_v2.util.Utils

class Node(kind: NodeKind, intVal: Int?, strVal: String?, items: NodeList?) {
    val kind = kind
    private var intVal = intVal
    private var strVal = strVal
    private var items = items

    fun getIntVal(): Int {
        if (this.intVal == null) {
            throw RuntimeException("must not happen")
        } else {
            return this.intVal!!
        }
    }

    fun getStrVal(): String {
        if (this.strVal == null) {
            throw RuntimeException("must not happen")
        } else {
            return this.strVal!!
        }
    }

    fun getItems(): NodeList {
        if (this.items == null) {
            throw RuntimeException("must not happen")
        } else {
            return this.items!!
        }
    }

    fun strEq(str: String): Boolean {
        return Utils.strEq(this.strVal, str)
    }

    companion object {
        fun of(n: Int): Node {
            return Node(NodeKind.INT, n, null, null)
        }

        fun of(s: String): Node {
            return Node(NodeKind.STR, null, s, null)
        }

        fun of(list: NodeList): Node {
            return Node(NodeKind.LIST, null, null, list)
        }
    }

    override
    fun toString(): String {
        var s = "(Node ${kind}"
        when (kind) {
            NodeKind.INT  -> { s += " ${intVal}" }
            NodeKind.STR  -> { s += " " + Utils.inspect(strVal) }
            NodeKind.LIST -> { s += " " + Utils.inspect(items) }
            else -> { throw Utils.panic("invalid kind") }
        }
        return s
    }

}
