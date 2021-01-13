package sample

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

public class ModelTest {

    @Test
    fun add(){
        val sut = Model()
        assertEquals(3, sut.add(1, 2))
    }

}
