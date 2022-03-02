package coderead.netty.zero_copy;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author 鲁班大叔
 * @date 2020/8/15 11:52 AM
 */
public class ZeroTest {
    // 直接声明一个 堆外内存缓冲
    @Test
    public void test1() {
        //mmap 声明堆外内存

        //优点：数据传输到外部时不需要进行用户空间到内核空间拷贝
        //缺点：堆外内存声明和访问速度会快慢一些
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(10);// 内核空间

        //优点：堆内存 声明和访问速度更快
        //缺点：数据传输到外部时 要进行一次 用户空间 到内核空间的拷贝
        ByteBuffer heapBuffer = ByteBuffer.allocate(10);// 用户空间 堆内存
    }

    // 内存映射
    @Test
    public void mmapTest() throws IOException {
        String file_name = "/Users/tommy/git/coderead-netty/target/2.4.二级缓存定义与需求分析.mp4";
        String copy_name = "/Users/tommy/git/coderead-netty/target/copy.mp4";
        File file = new File(copy_name);
        file.delete();
        file.createNewFile();
        long begin = System.nanoTime();
        FileChannel channel = new RandomAccessFile(file_name, "rw").getChannel();
        FileChannel copyChannel = new RandomAccessFile(copy_name, "rw").getChannel();

        //1. 建立一个 映射
        MappedByteBuffer mapped = channel
                .map(FileChannel.MapMode.READ_WRITE, 0, channel.size());
        copyChannel.write(mapped);//2. file内核缓冲区1--》cpu拷贝到 file内核缓冲区2
        System.out.println((System.nanoTime() - begin) / 1.0e6);
        copyChannel.close();
        channel.close();
    }

    // 直接传输
    @Test
    public void transferFromTest() throws IOException {
        String file_name = "/Users/tommy/git/coderead-netty/target/2.4.二级缓存定义与需求分析.mp4";
        String copy_name = "/Users/tommy/git/coderead-netty/target/copy.mp4";
        File file = new File(copy_name);
        file.delete();
        file.createNewFile();
        FileChannel channel = new RandomAccessFile(file_name, "rw").getChannel();
        FileChannel copyChannel = new RandomAccessFile(copy_name, "rw").getChannel();
        long begin = System.nanoTime();
        // sendFile
        // 2次切换 2次拷贝
        copyChannel.transferFrom(channel, 0, channel.size());//从批定目标拷贝到当前管道
        System.out.println((System.nanoTime() - begin) / 1.0e6);
        channel.close();
        copyChannel.close();
//        System.in.read();
    }
    // netty
    @Test
    public void testByNetty() {
        //DirectBuffer
        ByteBuf directBuf = Unpooled.directBuffer(1024);// 声明堆外内存 mmap
        ByteBuf heapBuf = Unpooled.buffer(1024);//声明堆内存
    }

    @Test
    public void testUploadNyNetty() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(8));
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                // directBuf
                ch.pipeline().addLast(new Upload());
                // 编码器
                // 编解码 headBuf
            }
        });
        ChannelFuture future = bootstrap.bind(8080);
        System.out.println("启动成功");
        future.sync().channel().closeFuture().sync();
    }


    private class Upload extends SimpleChannelInboundHandler {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println(msg);
            String fileName="/Users/tommy/git/coderead-netty/src/main/java/coderead/netty/zero_copy/hello.txt";
            RandomAccessFile file=new RandomAccessFile(fileName,"r");
            // sendFile 2次切换 2次拷贝  当向ctx中写入FileRegion时，是不会走编码解码等一系列Pipeline操作的，只会从内核空间直接运输到网卡中
            FileRegion fileRegion=new DefaultFileRegion(file.getChannel(),0,file.length());
            ctx.writeAndFlush(fileRegion);

            // mmap 4次切换 3次拷贝
            MappedByteBuffer map = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            ByteBuf buf = Unpooled.wrappedBuffer(map);
            ctx.writeAndFlush(buf);
        }
    }
}
