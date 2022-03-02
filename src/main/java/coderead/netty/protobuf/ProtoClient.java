package coderead.netty.protobuf;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import com.google.protobuf.ByteString;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 鲁班大叔
 * @date 2020/8/15 10:34 AM
 */
public class ProtoClient {

    private Channel channel;

    public void connection() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                // 压缩
                ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                //编码器
                ch.pipeline().addLast("encode", new ProtobufEncoder());
            }
        });
        ChannelFuture future = bootstrap.connect("127.0.0.1", 8080);
        channel = future.channel();
        future.sync();
        System.out.println("连接成功");
    }

    @Test
    public void testByTransfer() throws InterruptedException {
        User user = new User(1, "luban");
        byte[] bytes = serialize(TransferMessage.SerializableType.java, user);
        ByteString body = ByteString.copyFrom(bytes);
        TransferMessage.Transfer message = TransferMessage.Transfer.newBuilder()
                .setType(TransferMessage.SerializableType.java).setBody(body).build();
        ProtoClient client = new ProtoClient();
        client.connection();
        client.channel.writeAndFlush(message).sync();
    }

    private static byte[] serialize(TransferMessage.SerializableType serializable, Object target) {

        if (serializable == TransferMessage.SerializableType.java) { //JAVA
            ByteArrayOutputStream out = null;
            try {
                out = new ByteArrayOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(out);
                stream.writeObject(target);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return out.toByteArray();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testSync() throws InterruptedException, IOException {
        ProtoClient client = new ProtoClient();
        client.connection();
        ExecutorService pool = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 200; i++) {
            int k = i;
            pool.execute(() -> client.channel.writeAndFlush(UserMessage.User.newBuilder()
                    .setId(k).setName(k + "luban").build()));
        }
        System.in.read();
        /*pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);*/
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        ProtoClient client = new ProtoClient();
        client.connection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = reader.readLine();
            Object user = UserMessage.User.newBuilder()
                    .setId(10).setName(line).build();
            client.channel.writeAndFlush(user);
        }

    }
}
