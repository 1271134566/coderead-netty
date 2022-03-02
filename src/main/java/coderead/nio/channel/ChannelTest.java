package coderead.nio.channel;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.junit.Test;
import sun.misc.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author 鲁班大叔
 * @date 2020/7/1110:38 AM
 */
public class ChannelTest {

    // 1 inputStream outputStream 文件的拷贝
    // 2 Channel 文件拷贝
    String file_name = "/Users/tommy/temp/coderead-netty/target/2.4.二级缓存定义与需求分析.mp4";
    String copy_name = "/Users/tommy/temp/coderead-netty/target/copy.mp4";

    //1.基于普通文件流读写
    //读取：244.511344
    //写入：98.120449
    @Test
    public void streamReadTest() throws IOException {

        FileInputStream inputStream = new FileInputStream(file_name);
        long begin = System.nanoTime();
        byte[] bytes = IOUtils.readFully(inputStream, -1, false);
        System.out.println((System.nanoTime() - begin) / 1.0e6);
        inputStream.close();
        begin = System.nanoTime();
        Files.write(Paths.get(copy_name), bytes);
        System.out.println((System.nanoTime() - begin) / 1.0e6);
    }


    // 2.基于普通管道读写
    //读： 149
    //写： 39
    @Test
    public void channelTest() throws IOException {
        long begin = System.nanoTime();

        File file = new File(file_name);
        FileChannel channel = new RandomAccessFile(file_name,"rw").getChannel();

        ByteBuffer dst=ByteBuffer.allocate((int) file.length());
        channel.read(dst);
        byte[] bytes=new byte[(int) file.length()];
        dst.flip();
        dst.get(bytes);

        System.out.println((System.nanoTime() - begin) / 1.0e6);


        // 写入性能
        begin = System.nanoTime();
        RandomAccessFile copyFile = new RandomAccessFile(copy_name, "rw");
        FileChannel channel1 = copyFile.getChannel();
        dst.rewind();
        channel1.write(dst);
        System.out.println((System.nanoTime() - begin) / 1.0e6);
        channel1.close();
        channel.close();

    }
}
