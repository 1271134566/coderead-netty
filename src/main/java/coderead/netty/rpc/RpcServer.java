package coderead.netty.rpc;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 鲁班大叔
 * @date 2020/7/305:26 PM
 */
public class RpcServer {
    ExecutorService threadPool = Executors.newFixedThreadPool(500);
    private Map<String, ServiceBean> register = new HashMap<>();

    public void start(int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup work = new NioEventLoopGroup(8);
        bootstrap.group(boss, work).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast("codec", new RpcCodec());
                ch.pipeline().addLast("dispatch", new Dispatch());
            }
        }).bind(port).sync();
        System.out.println("服务启动成功");
    }

    private class Dispatch extends SimpleChannelInboundHandler<Transfer> {



        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Transfer transfer) {
            if (transfer.heartbeat) { // 心跳处理
                Transfer t = new Transfer(transfer.id);
                t.heartbeat = true;
                t.request = false;
                ctx.writeAndFlush(t);// 返回心跳
            } else {
                threadPool.submit(() -> {
                    Transfer to = doDispatchRequest(transfer);
                    ctx.writeAndFlush(to);// 非IO线程 异步提交到IO
                });
            }
        }
        // 业务请求处理
        Transfer doDispatchRequest(Transfer from) {
            Request request = (Request) from.target;
            Transfer to = new Transfer(from.id);
            to.request = false;
            to.serializableId = from.serializableId;
            Response response = new Response();
            try {
                String serverId = request.getClassName() + request.getMethodDesc();
                ServiceBean serverBean = register.get(serverId);
                if (serverBean == null) {
                    throw new IllegalArgumentException("找不到服务" + serverId);
                }
                Object result = serverBean.invoke(request.getArgs());
                response.setResult(result);
                to.status = Transfer.STATUS_OK;
            } catch (Throwable e) {
                e.printStackTrace();
                response.setError(e);
                to.status = Transfer.STATUS_ERROR;
            }
            to.target = response;
            return to;
        }
    }

    private static class ServiceBean {
        Method method;
        Object target;

        public ServiceBean(Method method, Object target) {
            this.method = method;
            this.target = target;
        }

        public Object invoke(Object[] args) throws Exception {
            return method.invoke(target, args);
        }
    }

    public void registerServer(Class serviceInterface, Object serverBean) {
        assert serviceInterface.isInterface();
        for (Method method : serviceInterface.getMethods()) {
            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isNative(modifiers)) {
                continue;
            }
            String methodDescriptor = Type.getMethodDescriptor(method);
            String key = serviceInterface.getName() +method.getName()+ methodDescriptor;
            register.put(key, new ServiceBean(method, serverBean));
        }
    }
}


