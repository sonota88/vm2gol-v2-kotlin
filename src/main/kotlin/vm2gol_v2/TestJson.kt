package vm2gol_v2

import vm2gol_v2.type.Node
import vm2gol_v2.type.NodeList
import vm2gol_v2.util.Json
import vm2gol_v2.util.Utils

class TestJson {
    fun run() {
        // test_01()
        // test_02()
        // test_03()
        // test_04()
        // test_05()
        // test_06()

        test()
    }

    fun test_01() {
        val list = NodeList()

        Json.print(list)
    }

    fun test_02() {
        val list = NodeList()
        list.add(1)

        Json.print(list)
    }

    fun test_03() {
        val root = NodeList()
        root.add("fdsa")
        Json.print(root)
    }

    fun test_04() {
        val root = NodeList()
        root.add(-123)
        root.add("fdsa")
        Json.print(root)
    }

    fun test_05() {
        val root = NodeList()
        root.add(NodeList())
        Json.print(root)
    }

    fun test_06() {
        val inner = NodeList()
        inner.add(2)
        inner.add("b")

        val root = NodeList()
        root.add(1)
        root.add("a")
        root.add(inner)
        root.add(3)
        root.add("c")
        Json.print(root)
    }

    fun test() {
        val json = Utils.readStdinAll()
        val list = Json.parse(json)

        Json.print(list)
    }

}
