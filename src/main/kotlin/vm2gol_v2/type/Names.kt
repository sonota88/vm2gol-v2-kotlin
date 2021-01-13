package vm2gol_v2.type

import vm2gol_v2.util.Utils

class Names(names: MutableList<String>) {

    val names = names

    companion object {
        fun fromNodeList(nl: NodeList): Names {
            val names = nl.getList()
                .map { item -> item.getStrVal() }
                .toMutableList()

            return Names(names)
        }

        fun empty(): Names {
            return fromNodeList(NodeList())
        }

    }

    fun add(name: String) {
        this.names.add(name)
    }

    fun contains(name: String): Boolean {
        return this.names.contains(name)
    }

    fun indexOf(name: String): Int {
        var i = 0
        while (i < names.size) {
            if (names.get(i) == name) {
                return i
            }
            i++
        }
        return -1
    }

    override
    fun toString(): String {
        return "(Names " + Utils.inspect(names) + ")"
    }

}
