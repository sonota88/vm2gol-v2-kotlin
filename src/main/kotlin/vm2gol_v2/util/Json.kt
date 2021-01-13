package vm2gol_v2.util

import vm2gol_v2.type.NodeList
import vm2gol_v2.type.Node
import vm2gol_v2.type.NodeKind
import vm2gol_v2.util.Utils

class ParseResult(nl: NodeList, size: Int) {
    val nl = nl
    val size = size
}

class Json {

    companion object {

        const val LF = Utils.LF
        const val DQ = Utils.DQ

        fun parse(json: String): NodeList {
            return _parse(json).nl
        }

        fun _parse(json: String): ParseResult {
            var pos = 1
            val xs = NodeList()

            val re = RegexWrapper()

            while (pos <= json.length) {
                val rest = json.substring(pos)
                
                if (rest.startsWith("[")) {
                        val pr = _parse(rest)
                        xs.add(pr.nl)
                        pos += pr.size
                } else if (rest.startsWith("]")) {
                    pos++
                    break
                } else if (
                    rest.startsWith(" ") ||
                    rest.startsWith(LF) ||
                    rest.startsWith(",")
                )
                {
                    pos++
                } else if (re.match("^(-?[0-9]+)", rest)) {
                    val str = re.group(1)
                    val n = str.toInt()
                    xs.add(n)
                    pos += str.length
                } else if (re.match("^\"(.*?)\"", rest)) {
                    val str = re.group(1)
                    xs.add(str)
                    pos += str.length + 2
                } else {
                    throw Utils.panic("must not happen")
                }
            }

            return ParseResult(xs, pos)
        }

        fun print(list: NodeList) {
            printList(list, 0)
        }

        fun printList(list: NodeList, lv: Int) {
            val nextLv = lv + 1
            var s = ""
            printIndent(lv)
            print("[" + LF)

            var cnt = -1
            list.getList().forEach { item ->
                cnt++

                printNode(item, nextLv)

                if (cnt < list.size() - 1) {
                    print("," + LF)
                }
            }

            print(LF)
            printIndent(lv)
            print("]")
        }

        fun printNode(node: Node, lv: Int) {
            val nextLv = lv + 0

            if (node.kind == NodeKind.STR) {
                printIndent(lv)
                print(DQ + node.getStrVal() + DQ)
            } else if (node.kind == NodeKind.INT) {
                printIndent(lv)
                print(node.getIntVal().toInt())
            } else if (node.kind == NodeKind.LIST) {
                printList(node.getItems(), nextLv)
            } else {
                throw Utils.panic("invalid node kind")
            }
        }

        fun printIndent(lv: Int) {
            var i = 0
            while (i < lv) {
                print("  ")
                i++
            }
        }

    }

}
