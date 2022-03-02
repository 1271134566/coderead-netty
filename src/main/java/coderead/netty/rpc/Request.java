package coderead.netty.rpc;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

/**
 * @author 鲁班大叔
 * @date 2020/7/30 5:34 PM
 */
public class Request   implements java.io.Serializable {
    private String methodDesc;
    private String className;
    private Object args[];


    public Request(String className,String methodDesc) {
        this.className=className;
        this.methodDesc=methodDesc;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
