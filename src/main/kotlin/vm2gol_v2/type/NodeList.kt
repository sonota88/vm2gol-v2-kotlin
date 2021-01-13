package vm2gol_v2.type

class NodeList {

    companion object {
        fun empty(): NodeList {
            return NodeList()
        }
    }

    private var list = mutableListOf<Node>()

    fun getList(): MutableList<Node> {
        return this.list
    }

    fun size(): Int {
        return this.list.size
    }

    fun add(n: Int): NodeList {
        this.list.add(Node.of(n))
        return this
    }

    fun add(s: String): NodeList {
        this.list.add(Node.of(s))
        return this
    }

    fun add(list: NodeList): NodeList {
        this.list.add(Node.of(list))
        return this
    }

    fun add(node: Node): NodeList {
        this.list.add(node)
        return this
    }

    fun addAll(list: NodeList): NodeList {
        list.getList().forEach {
            this.add(it)
        }
        return this
    }

    fun isEmpty(): Boolean {
        return this.list.size == 0
    }

    fun get(i: Int): Node {
        return this.list.get(i)
    }

    fun first(): Node {
        return this.get(0)
    }

    fun rest(): NodeList {
        val newlist = NodeList()
        var i = 1
        while (i < this.list.size) {
            newlist.add(this.get(i))
            i++
        }
        return newlist
    }

    fun reverse(): NodeList {
        val newlist = NodeList()

        var i = this.list.size - 1
        while (0 <= i) {
            newlist.add(this.get(i))
            i--
        }

        return newlist
    }
}
