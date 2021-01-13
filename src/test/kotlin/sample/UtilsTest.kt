package sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

public class UtilsTest {
    @Test
    fun inspectNull(){
        assertEquals(
            "null",
            Utils.inspect(null)
        )
    }

    @Test
    fun inspectInt(){
        assertEquals(
            "123",
            Utils.inspect(123)
        )
    }

    @Test
    fun inspectString(){
        val dq = "\""

        assertEquals(
            dq + "fdsa" + dq,
            Utils.inspect("fdsa")
        )
    }

    @Test
    fun inspectString_escape(){
        val bs = "\\"
        val lf = "\n"
        val cr = "\r"
        val tab = "\t"
        val dq = "\""

        val input = "_${bs}${bs}_${lf}${lf}_${cr}${cr}_${tab}${tab}_${dq}${dq}_"

        val expected =
            dq +
            "_" + bs + bs + bs + bs +
            "_" + bs + "n" + bs + "n" +
            "_" + bs + "r" + bs + "r" +
            "_" + bs + "t" + bs + "t" +
            "_" + bs + dq + bs + dq +
            "_" + dq

        assertEquals(
            expected,
            Utils.inspect(input)
         )
     }

    @Test
    fun inspectList(){
        assertEquals(
            """["a", "b"]""",
            Utils.inspect(
                listOf("a", "b")
            )
        )
    }

    @Test
    fun inspectArray(){
        assertEquals(
            """["a", "b"]""",
            Utils.inspect(
                arrayOf("a", "b")
            )
        )
     }

    @Test
    fun inspectMap(){
        assertEquals(
            """{"a": "A", "b": "B"}""",
            Utils.inspect(
                hashMapOf("a" to "A", "b" to "B")
            )
        )
     }
}
