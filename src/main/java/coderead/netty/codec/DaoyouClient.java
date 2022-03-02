package coderead.netty.codec;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author 鲁班大叔
 * @date 2020/7/259:53 PM
 */
public class DaoyouClient {
    private Bootstrap bootstrap;// ServerBootstrap
    private Channel channel;


    public void start() throws InterruptedException {
        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1));
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(new DaoyouProtocol());//出站处理
            }
        });
        ChannelFuture future = bootstrap.connect("127.0.0.1",8080);
         channel = future.sync().channel();

    }


    public static void main(String[] args) throws IOException, InterruptedException {
        DaoyouClient client=new DaoyouClient();
        client.start();
        BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String line = reader.readLine();
            client.channel.writeAndFlush(line);
        }
    }

}
