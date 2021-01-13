package vm2gol_v2

import vm2gol_v2.type.Names
import vm2gol_v2.type.Node
import vm2gol_v2.type.NodeKind
import vm2gol_v2.type.NodeList
import vm2gol_v2.util.Json
import vm2gol_v2.util.Utils
import vm2gol_v2.util.RegexWrapper

class CodeGenerator {

    companion object {
        fun run() {
            CodeGenerator().main()
        }
    }

    fun main() {
        val src = Utils.readStdinAll()
        val topStmts = Json.parse(src)
        codegen(topStmts)
    }

    // --------------------------------
    
    var labelId = 0

    fun nextLabelId(): Int {
        labelId++
        return labelId
    }

    fun toFnArgRef(fnArgNames: Names, fnArgName: String): String {
        val i = fnArgNames.indexOf(fnArgName)
        return "[bp+${i + 2}]"
    }

    fun toLvarRef(lvarNames: Names, lvarName: String): String {
        val i = lvarNames.indexOf(lvarName)
        return "[bp-${i + 1}]"
    }

    fun simpleDeref(fnArgNames: Names, lvarNames: Names, node: Node): String? {
        when (node.kind) {
            NodeKind.INT -> {
                return "" + node.getIntVal()
            }
            NodeKind.STR -> {
                val str = node.getStrVal()
                if (fnArgNames.contains(str)) {
                    return toFnArgRef(fnArgNames, str)
                } else if (lvarNames.contains(str)) {
                    return toLvarRef(lvarNames, str)
                } else {
                    return null
                }
            }
            else -> {
                return null
            }
        }
    }
    
    fun matchVram(str: String): Boolean {
        return RegexWrapper().match("""^vram\[(.+)\]""", str)
    }
    
    fun extractVramParam(str: String): String {
        val re = RegexWrapper()
        re.match("""^vram\[(.+)\]""", str)
        return re.group(1)
    }
    
    fun matchVramImm(str: String): String? {
        if (matchVram(str)) {
            val param = extractVramParam(str)
            if (Utils.isNumber(param)) {
                return param
            } else {
                return null
            }
        } else {
            return null
        }
    }
    
    fun matchVramVar(str: String): String? {
        if (matchVram(str)) {
            val param = extractVramParam(str)
            if (Utils.isNumber(param)) {
                return null
            } else {
                return param
            }
        } else {
            return null
        }
    }
    
    // --------------------------------

    fun genVar(fnArgNames: Names, lvarNames: Names, stmtRest: NodeList) {
        puts("  sub_sp 1")
    
        if (stmtRest.size() == 2) {
            genSet(fnArgNames, lvarNames, stmtRest)
        }
    }
    
    fun genVar_array(fnArgNames: Names, lvarNames: Names, stmtRest: NodeList) {
        val lvarName = stmtRest.get(0).getStrVal()
        val size     = stmtRest.get(1).getIntVal()
        puts("  sub_sp ${size}")
    }
    
    fun genExpr_push(fnArgNames: Names, lvarNames: Names, value: Node) {
        var pushArg: String

        val temp = simpleDeref(fnArgNames, lvarNames, value)
        if (temp != null) {
            pushArg = temp
        } else {
            when (value.kind) {
                NodeKind.LIST -> {
                    genExpr(fnArgNames, lvarNames, value)
                    pushArg = "reg_a"
                }
                else -> {
                    throw Utils.panic("114 not_yet_impl")
                }
            }
        }

        puts("  push " + pushArg)
    }

    fun genExpr_add() {
        puts("  pop reg_b")
        puts("  pop reg_a")
        puts("  add_ab")
    }
    
    fun genExpr_mult() {
        puts("  pop reg_b")
        puts("  pop reg_a")
        puts("  mult_ab")
    }
    
    fun genExpr_eq() {
        val labelId = nextLabelId()
        val thenLabel = "then_${labelId}"
        val endLabel  = "end_eq_${labelId}"
    
        puts("  pop reg_b")
        puts("  pop reg_a")
    
        puts("  compare")
        puts("  jump_eq %s", thenLabel)
    
        puts("  set_reg_a 0")
        puts("  jump %s", endLabel)
    
        puts("label %s", thenLabel)
        puts("  set_reg_a 1")
    
        puts("label %s", endLabel)
    }
    
    fun genExpr_neq() {
        val labelId = nextLabelId()
        val thenLabel = "then_${labelId}"
        val endLabel  = "end_neq_${labelId}"
    
        puts("  pop reg_b")
        puts("  pop reg_a")
    
        puts("  compare")
        puts("  jump_eq %s", thenLabel)
    
        puts("  set_reg_a 1")
        puts("  jump %s", endLabel)
    
        puts("label %s", thenLabel)
        puts("  set_reg_a 0")
    
        puts("label %s", endLabel)
    }
    
    fun genExpr(fnArgNames: Names, lvarNames: Names, exp: Node) {
        val operator = exp.getItems().first()
        val args = exp.getItems().rest()
    
        val termL = args.get(0)
        val termR = args.get(1)
    
        genExpr_push(fnArgNames, lvarNames, termL)
        genExpr_push(fnArgNames, lvarNames, termR)

        when (operator.getStrVal()) {
            "+"   -> { genExpr_add()  }
            "*"   -> { genExpr_mult() }
            "eq"  -> { genExpr_eq()   }
            "neq" -> { genExpr_neq()  }
            else -> {
                throw Utils.panic("not_yet_impl (${operator})")
            }
        }
    }
    
    fun genCall_pushFnArg(fnArgNames: Names, lvarNames: Names, fnArg: Node) {
        val temp = simpleDeref(fnArgNames, lvarNames, fnArg)
        if (temp != null) {
            puts("  push " + temp)
        } else {
            throw Utils.panic("not_yet_impl")
        }
    }
    
    fun genCall(fnArgNames: Names, lvarNames: Names, stmtRest: NodeList) {
        val fnName = stmtRest.first().getStrVal()
        val fnArgs = stmtRest.rest()
    
        for (fnArg: Node in fnArgs.reverse().getList()) {
            genCall_pushFnArg(fnArgNames, lvarNames, fnArg)
        }
    
        genVmComment("call  " + fnName)
        puts("  call ${fnName}")
    
        puts("  add_sp ${fnArgs.size()}")
    }
    
    fun genCallSet(fnArgNames: Names, lvarNames: Names, stmtRest: NodeList) {
        val lvarName = stmtRest.first().getStrVal()
        val fnTemp = stmtRest.get(1).getItems()

        val fnName = fnTemp.first().getStrVal()
        val fnArgs = fnTemp.rest()

        for (fnArg in fnArgs.reverse().getList()) {
            genCall_pushFnArg(fnArgNames, lvarNames, fnArg)
        }

        genVmComment("call_set  " + fnName)
        puts("  call ${fnName}")
        puts("  add_sp ${fnArgs.size()}")

        val ref = toLvarRef(lvarNames, lvarName)
        puts("  cp reg_a ${ref}")
    }

    fun genSet(fnArgNames: Names, lvarNames: Names, rest: NodeList) {
        val dest = rest.get(0)
        val expr = rest.get(1)

        var srcVal = ""
        val temp = simpleDeref(fnArgNames, lvarNames, expr)
        if (temp != null) {
            srcVal = temp
        } else {
            when (expr.kind) {
                NodeKind.STR -> {
                    val str = expr.getStrVal()
                    if (matchVramImm(str) != null) {
                        val vramAddr = matchVramImm(str)!!
                        puts("  get_vram ${vramAddr} reg_a")
                        srcVal = "reg_a"
                    } else if (matchVramVar(str) != null) {
                        val vramParam = matchVramVar(str)!!
                        var temp3 = simpleDeref(fnArgNames, lvarNames, Node.of(vramParam))
                        if (temp3 != null) {
                            puts("  get_vram ${temp3} reg_a")
                        } else {
                            throw Utils.panic("not_yet_impl")
                        }
                        srcVal = "reg_a"
                    }
                }
                NodeKind.LIST -> {
                    genExpr(fnArgNames, lvarNames, expr)
                    srcVal = "reg_a"
                }
                else -> {
                    throw Utils.panic("not_yet_impl")
                }
            }
        }

        val temp2 = simpleDeref(fnArgNames, lvarNames, dest)
        if (temp2 != null) {
            puts("  cp ${srcVal} ${temp2}")
        } else {
            when (dest.kind) {
                NodeKind.STR -> {
                    val str = dest.getStrVal()
                    if (matchVramImm(str) != null) {
                        val vramAddr = matchVramImm(str)!!
                        puts("  set_vram ${vramAddr} ${srcVal}")
                    } else if (matchVramVar(str) != null) {
                        val param = matchVramVar(str)!!
                        val temp4 = simpleDeref(fnArgNames, lvarNames, Node.of(param))
                        if (temp4 != null) {
                            puts("  set_vram ${temp4} ${srcVal}")
                        } else {
                            throw Utils.panic("not_yet_impl")
                        }
                    } else {
                        throw Utils.panic("not_yet_impl")
                    }
                }
                else -> {
                    throw Utils.panic("not_yet_impl")
                }
            }
        }
    }

    fun genReturn(lvarNames: Names, stmtRest: NodeList) {
        val retval = stmtRest.first()
        val temp = simpleDeref(Names.empty(), lvarNames, retval)
        if (temp != null) {
            puts("  cp ${temp} reg_a")
        } else {
            when (retval.kind) {
                NodeKind.STR -> {
                    val str = retval.getStrVal()
                    if (matchVramVar(str) != null) {
                        val param = matchVramVar(str)!!
                        val temp2 = simpleDeref(Names.empty(), lvarNames, Node.of(param))
                        if (temp2 != null) {
                            puts("  get_vram ${temp2} reg_a")
                        } else {
                            throw Utils.panic("not_yet_impl", retval)
                        }
                    } else {
                        throw Utils.panic("not_yet_impl", retval)
                    }

                }
                else -> {
                    throw Utils.panic("not_yet_impl", retval)
                }
            }
        }
    }
    
    fun genVmComment(comment: String) {
        puts("  _cmt " + comment.replace(" ", "~"))
    }
    
    fun genWhile(fnArgNames: Names, lvarNames: Names, rest: NodeList) {
        val condExp = rest.first()
        val body = rest.rest().first().getItems()

        val labelId = nextLabelId()

        puts("")

        puts("label while_%d", labelId)

        // 条件の評価
        genExpr(fnArgNames, lvarNames, condExp)
        puts("  set_reg_b 1")
        puts("  compare")

        puts("  jump_eq true_%d", labelId)

        puts("  jump end_while_%d", labelId)

        puts("label true_%d", labelId)
        genStmts(fnArgNames, lvarNames, body)

        puts("  jump while_%d", labelId)

        puts("label end_while_%d", labelId)
        puts("")
    }

    fun genCase(fnArgNames: Names, lvarNames: Names, whenBlocks: NodeList) {
        var whenIdx = -1

        val labelId = nextLabelId()
        val labelEnd = "end_case_${labelId}"
        val labelWhenHead = "when_${labelId}"
        val labelEndWhenHead = "end_when_${labelId}"

        puts("")
        puts("  # -->> case_${labelId}")

        for (_whenBlock in whenBlocks.getList()) {
            val whenBlock = _whenBlock.getItems()
            whenIdx++

            val cond = whenBlock.first()
            val rest = whenBlock.rest()

            val condHead = cond.getItems().first()

            puts("  # 条件 ${labelId}_${whenIdx}: ${ Utils.inspect(cond) }")

            if (condHead.strEq("eq")) {
                genExpr(fnArgNames, lvarNames, cond)
                puts("  set_reg_b 1")

                puts("  compare")
                puts("  jump_eq ${labelWhenHead}_${whenIdx}")
                puts("  jump ${labelEndWhenHead}_${whenIdx}")

                puts("label ${labelWhenHead}_${whenIdx}")

                genStmts(fnArgNames, lvarNames, rest)

                puts("  jump ${labelEnd}")
                puts("label ${labelEndWhenHead}_${whenIdx}")
            } else {
                throw Utils.panic("not_yet_impl", condHead)
            }
        }

        puts("label end_case_%d", labelId)
        puts("  # <<-- case_${labelId}")
    }
    
    fun codegenStmt(fnArgNames: Names, lvarNames: Names, stmt: NodeList) {
        val stmtHead = stmt.first().getStrVal()
        val stmtRest = stmt.rest()
    
        when (stmtHead) {
            "set"      -> return genSet(    fnArgNames, lvarNames, stmtRest)
            "call"     -> return genCall(   fnArgNames, lvarNames, stmtRest)
            "call_set" -> return genCallSet(fnArgNames, lvarNames, stmtRest)
            "return"   -> return genReturn(             lvarNames, stmtRest)
            "while"    -> return genWhile(  fnArgNames, lvarNames, stmtRest)
            "case"     -> return genCase(   fnArgNames, lvarNames, stmtRest)
            "_cmt"     -> return genVmComment(stmtRest.get(0).getStrVal())
            else -> {
                throw Utils.panic("not_yet_impl", stmtHead)
            }
        }
    }
    
    fun genStmts(fnArgNames: Names, lvarNames: Names, stmts: NodeList) {
        for (_stmt in stmts.getList()) {
            val stmt = _stmt.getItems()
            codegenStmt(fnArgNames, lvarNames, stmt)
        }
    }
    
    fun genFuncDef(rest: NodeList) {
        val fnName = rest.get(0).getStrVal()
        val fnArgNames = Names.fromNodeList(rest.get(1).getItems())
        val body = rest.get(2).getItems()

        puts("")
        puts("label ${fnName}")
        puts("  push bp")
        puts("  cp sp bp")

        puts("")
        puts("  # 関数の処理本体")

        val lvarNames = Names.empty()

        for (stmt in body.getList()) {
            val _stmt = stmt.getItems()

            if (_stmt.first().strEq("var")) {
                val stmtRest = _stmt.rest() 
                lvarNames.add(stmtRest.first().getStrVal())
                genVar(fnArgNames, lvarNames, stmtRest)
            } else if (_stmt.first().strEq("var_array")) {
                val stmtRest = _stmt.rest() 
                lvarNames.add("array:" + stmtRest.first().getStrVal())
                genVar_array(fnArgNames, lvarNames, stmtRest)
            } else {
                codegenStmt(fnArgNames, lvarNames, _stmt)
            }
        }

        puts("")
        puts("  cp bp sp")
        puts("  pop bp")
        puts("  ret")
    }
    
    fun genTopStmts(rest: NodeList) {
        for (stmt in rest.getList()) {
            val stmtHead = stmt.getItems().first()
            val stmtRest = stmt.getItems().rest()

            when (stmtHead.getStrVal()) {
                "func" -> {
                    genFuncDef(stmtRest)
                }
                "_cmt" -> {
                    throw Utils.panic("not_yet_impl")
                }
                else -> {
                    throw Utils.panic("not_yet_impl")
                }
            }
        }
    }
    
    fun codegen(nl: NodeList) {
        puts("  call main")
        puts("  exit")

        // val head = nl.first()
        val rest = nl.rest()

        genTopStmts(rest)
    }
    
    fun puts(str: String) {
        Utils.puts(str)
    }

    fun puts(template: String, param: String) {
        Utils.puts(String.format(template, param))
    }

    fun puts(template: String, param: Int) {
        Utils.puts(String.format(template, param))
    }

}
