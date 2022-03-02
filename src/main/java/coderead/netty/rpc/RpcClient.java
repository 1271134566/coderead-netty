package coderead.netty.rpc;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 鲁班大叔
 * @date 2020/7/305:26 PM
 */
public class RpcClient {
    static AtomicLong atomicLong = new AtomicLong(100);
    private Channel channel;
    private Map<Long, Promise<Response>> results = new HashMap<>();

    public static long getNextId() {
        return atomicLong.getAndIncrement();
    }

    public void init(String address, int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast("codec", new RpcCodec());
                ch.pipeline().addLast("resultSet", new ResultFill());// 结果集填充
            }
        });
        ChannelFuture connect = bootstrap.connect(address, port);
        channel = connect.sync().channel();
        System.out.println("连接成功");
        //
//        // 每隔 两秒发送心跳
//        channel.eventLoop().scheduleWithFixedDelay(() -> {
//            Transfer transfer=new Transfer(getNextId());
//            transfer.heartbeat=true;
//            channel.writeAndFlush(transfer);
//        },2000,2000,TimeUnit.MILLISECONDS);
    }

    public Response invokerRemote(Class serverInterface,
                                  String methodDesc,
                                  Object[] args) throws InterruptedException, ExecutionException, TimeoutException {
        Request request = new Request(serverInterface.getName(), methodDesc);
        request.setArgs(args);
        Transfer transfer = new Transfer(getNextId());
        transfer.request=true;
        transfer.serializableId=Transfer.SERIALIZABLE_JAVA;
        transfer.target = request;
        DefaultPromise<Response> resultPromise = new DefaultPromise(channel.eventLoop());
        // 写入成功后添加 结果
        channel.writeAndFlush(transfer).addListener(future ->

                {// IO线程
                    if (future.cause() != null) {// 写入失败
                        resultPromise.setFailure(future.cause()); //写入失败必须处理
                    } else {    // 写入成功
                        results.put(transfer.id, resultPromise);
                    }
                }
        );

        return resultPromise.get(10000, TimeUnit.MILLISECONDS);
    }

    private class ResultFill extends SimpleChannelInboundHandler<Transfer> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Transfer msg) {
            if (msg.heartbeat) {
                System.out.println(String.format("服务端心跳返回：%s",
                        ctx.channel().remoteAddress()));
            } else {
                Promise<Response> promise = results.remove(msg.id);
                promise.setSuccess((Response) msg.target); // 填充结果
            }
        }
    }

    public <T> T getRemoteService(Class<T> serviceInterface) {
        assert serviceInterface.isInterface();
        Object o = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                if (Object.class.equals(method.getDeclaringClass())) {
                    return method.invoke(this, args);
                }

                String methodDescriptor = method.getName()+Type.getMethodDescriptor(method);
                Response response = invokerRemote(serviceInterface, methodDescriptor, args);
                if (response.getError() != null) {
                    throw new RuntimeException("远程服务调用异常：", response.getError());
                }
                return response.getResult();
            }
        });
        return (T) o;
    }


}
