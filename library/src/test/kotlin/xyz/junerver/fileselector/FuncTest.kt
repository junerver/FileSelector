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
        val path = String("xxxx/xxxx/xxxx/abc.jpg".toByteArray())
        val path2 = String("xxxx/xxxx/xxxx/abc.jpg".toByteArray())
        //‘==‘表示比较值，‘===‘表示比较两个对象的地址是否相等
        println(path == path2)
        println(path === path2)
        val list = mutableListOf("1", "2")
        list += "3"
        println(list)
//        println(getFileParentPath(path))
//        val map = mapOf("1" to 1, "2" to 2)
//        println(map["3"])
        println( 8 x 9)
    }

    private infix fun Int.x(a: Int): Int {
        return this * a
    }
}