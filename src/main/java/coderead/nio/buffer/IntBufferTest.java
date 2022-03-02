package coderead.nio.buffer;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.junit.Test;

import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * @author 鲁班大叔
 * @date 2020/7/109:40 PM
 */
public class IntBufferTest {

    @Test
    public void test1(){
        IntBuffer allocate = IntBuffer.allocate(6);// position=0、limit=6、capacity=6
        allocate.put(1);
        allocate.put(2); // position=2、limit=6、capacity=6
        allocate.flip(); // position=0、limit=2、capacity=6
        allocate.get();  // position=1、limit=2、capacity=6
        allocate.get();  //  position=2、limit=2、capacity=6
//        allocate.put(3); //  超出limit 限制 ==》 BufferOverflowException
        //allocate.get(); // 超出limit 限制 ==》 BufferUnderflowException

        // Buffer 没有删除数据这个说法
        // 循环利用空间
        // 回到缓冲的初始状态
        allocate.clear(); //// position=0、limit=6、capacity=6
        for (int i = 0; i < 6; i++) {
            allocate.put(i+1);
        }
        allocate.flip();
        allocate.get();
        allocate.get();
        allocate.mark();
        int i = allocate.get()+100;
        int i1 = allocate.get()+100;
        allocate.reset();
        allocate.put(i);
        allocate.put(i1);
        System.out.println(Arrays.toString(allocate.array()));
    }
}
