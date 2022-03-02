package coderead.netty.protobuf;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author 鲁班大叔
 * @date 2020/8/15 10:30 AM
 */
public class ProtoServer {

    @Test
    public void test() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(8));
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                //解压缩
                ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                //解码器
                ch.pipeline().addLast(new ProtobufDecoder(TransferMessage.Transfer.getDefaultInstance()));
                ch.pipeline().addLast(new TrackHandler());
            }
        });

        ChannelFuture future = bootstrap.bind(8080);
        System.out.println("启动成功");
        future.sync().channel().closeFuture().sync();
    }

    private static class TrackHandler extends SimpleChannelInboundHandler<TransferMessage.Transfer>{

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TransferMessage.Transfer msg) throws Exception {
            System.out.println(msg);
            byte[] bytes = msg.getBody().toByteArray();
            User user = (User) deserialize(msg.getType(), bytes);
            System.out.println(user);
        }
    }

    // 反序列化
    private static Object deserialize(TransferMessage.SerializableType serializable, byte[] bytes) {
        if (serializable == TransferMessage.SerializableType.java) { //JAVA

            try {
                ObjectInputStream stream =
                        new ObjectInputStream(new ByteArrayInputStream(bytes));
                return stream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
