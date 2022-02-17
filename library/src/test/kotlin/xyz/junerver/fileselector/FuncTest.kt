package xyz.junerver.fileselector

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 * @author Junerver
 * date: 2022/2/17-8:15
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class FuncTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testFuncGetParent() {
        val path = "xxxx/xxxx/xxxx/abc.jpg"
        println(getFileParentPath(path))
        val map = mapOf("1" to 1, "2" to 2)
        println(map["3"])
    }
}