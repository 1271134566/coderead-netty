package coderead.netty.rpc;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

/**
 * @author 鲁班大叔
 * @date 2020/7/30 8:37 PM
 */
public class Transfer {
   public static final byte STATUS_ERROR = 0;
   public static final byte STATUS_OK = 1;
   public static final byte STATUS_ILLEGAL = 2;
   public static final byte SERIALIZABLE_JAVA=1;
   public static final byte SERIALIZABLE_HESSIAN2=2;
   public static final byte SERIALIZABLE_JSON=3;

    boolean request;
    byte serializableId; // 1:java 2:hessian2 3:json
    boolean twoWay;
    boolean heartbeat;
    long id;
    byte status;    // 1正常 0失败 2请求非法
    Object target;

    public Transfer(long id) {
        this.id = id;
    }

    void copy(Transfer from) {
        this.request = from.request;
        this.serializableId = from.serializableId;
        this.twoWay = from.twoWay;
        this.heartbeat = from.heartbeat;
        this.id = from.id;
        this.status = from.status;
        this.target = from.target;
    }
}
