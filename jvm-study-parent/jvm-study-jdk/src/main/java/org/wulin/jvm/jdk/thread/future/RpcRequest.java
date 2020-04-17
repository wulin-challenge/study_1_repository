package org.wulin.jvm.jdk.thread.future;

import java.io.Serializable;

/**
 * 封装 RPC 请求
 *
 * @author huangyong
 * @since 1.0.0
 */
/**
 * @author ThinkPad
 *
 */
public class RpcRequest implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String requestId;
    private String interfaceName;
    private String serviceVersion;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private Integer timeout = 60000; //默认一分钟
    private String host;
    private Integer port;
    
    
    /**
     * 客户端进程Id
     */
    private Integer clientPid;
    
    /**
     * 客户端Id
     */
    private String clientId;
    
    /**
     * 客户端Name
     */
    private String ClientName;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String className) {
        this.interfaceName = className;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getClientPid() {
		return clientPid;
	}

	public void setClientPid(Integer clientPid) {
		this.clientPid = clientPid;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return ClientName;
	}

	public void setClientName(String clientName) {
		ClientName = clientName;
	}

	@Override
	public String toString() {
		String msg = "{interfaceName:"+interfaceName+",methodName:"+methodName+",host:"+host+",port:"+port+"}";
		return msg;
	}
}
