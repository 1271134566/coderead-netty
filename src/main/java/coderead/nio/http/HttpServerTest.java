package coderead.nio.http;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author 鲁班大叔
 * @date 2020/7/1811:39 AM
 */
public class HttpServerTest {
    //keep-alive 问题不启作用
    @Test
    public void simpleHttpTest() throws IOException, InterruptedException {
        SimpleHttpServer simpleHttpServer = new SimpleHttpServer(8080, new SimpleHttpServer.HttpServlet() {
            @Override
            void doGet(SimpleHttpServer.Request request, SimpleHttpServer.Response response) {
                System.out.println(request.url);
                response.body="hello word";
                response.code=200;
                response.headers=new HashMap<>();
                if (request.params.containsKey("short")) {
                    response.headers.put("Connection", "close");
                }else if(request.params.containsKey("long")){
                    response.headers.put("Connection", "keep-alive");
                    response.headers.put("Keep-Alive", "timeout=30,max=300");
                }
            }

            @Override
            void doPost(SimpleHttpServer.Request request, SimpleHttpServer.Response response) {

            }
        });
        simpleHttpServer.start().join();
    }
}
