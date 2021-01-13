package vm2gol_v2

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val cmd = args[0]

    when (cmd) {
        "test-json" -> {
            TestJson().run()
        }
        "tokenize" -> {
            Tokenizer.run()
        }
        "parse" -> {
            Parser.run()
        }
        "codegen" -> {
            CodeGenerator.run()
        }
        else -> {
            println("invalid command (${cmd})")
            exitProcess(1)
        }
    }
}
