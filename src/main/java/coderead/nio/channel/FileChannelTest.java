package coderead.nio.channel;

import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelTest {

    String file_name = "/Users/tommy/temp/coderead-netty/test.txt";

    @Test
    public void test1() throws IOException {
        //1. 打开文件管道
        /*FileInputStream inputStream=new FileInputStream(file_name);
        FileChannel channel = inputStream.getChannel();*/
        FileChannel channel = new RandomAccessFile(file_name, "rw").getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int count = channel.read(buffer);
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        int i = 0;
        while (buffer.hasRemaining()) {
            bytes[i++] = buffer.get();
        }
        System.out.println(new String(bytes));
        // 把缓冲区数据写入到管道
        channel.write(ByteBuffer.wrap("hello 大叔".getBytes()));
        channel.close();
    }

    // 文件映射
    @Test
    public void mapTest() throws IOException {
        String file_name = "/Users/tommy/temp/coderead-netty/target/2.4.二级缓存定义与需求分析.mp4";
        String copy_name = "/Users/tommy/temp/coderead-netty/target/copy.mp4";
        long begin = System.nanoTime();
        FileChannel channel = new RandomAccessFile(file_name, "rw").getChannel();
        MappedByteBuffer mapped = channel
                .map(FileChannel.MapMode.READ_WRITE, 0, channel.size());
        mapped.get(new byte[(int) channel.size()]);
        System.out.println((System.nanoTime() - begin) / 1.0e6);
        begin = System.nanoTime();
        FileChannel copyChannel = new RandomAccessFile(copy_name, "rw").getChannel();
        mapped.rewind();
        copyChannel.write(mapped);
        copyChannel.close();
        channel.close();
        System.out.println((System.nanoTime() - begin) / 1.0e6);
    }

    // 零拷贝
    @Test
    public void testZeroCopy() throws IOException {
        String file_name = "/Users/tommy/temp/coderead-netty/target/2.4.二级缓存定义与需求分析.mp4";
        String copy_name = "/Users/tommy/temp/coderead-netty/target/copy.mp4";
        FileChannel channel = new RandomAccessFile(file_name, "rw").getChannel();
        FileChannel copyChannel = new RandomAccessFile(copy_name, "rw").getChannel();
        long begin = System.nanoTime();
//        channel.transferTo(0, channel.size(), copyChannel); // 拷贝到指定目标
        copyChannel.transferFrom(channel, 0, channel.size());//从批定目标拷贝到当前管道
        System.out.println((System.nanoTime() - begin) / 1.0e6);
        channel.close();
        copyChannel.close();
//        System.in.read();
    }


}
