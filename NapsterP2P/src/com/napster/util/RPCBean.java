package com.napster.util;

/**
 * 
 * This is a soap, which used to convey some information between RPCServer and
 * RPCClient
 * 
 */
public class RPCBean {
	// the name of class
	private String askServiceName;
	// parameters of method
	private Object[] askServiceParameters;
	// classes of parameters of method
	private Class<?>[] askServiceParametersClass;
	// the name of method
	private String askServiceMethod;

	public RPCBean() {
		super();
	}

	public RPCBean(String askServiceName, Object[] askServiceParameters, Class<?>[] askServiceParametersClass,
			String askServiceMethod) {
		super();
		this.askServiceName = askServiceName;
		this.askServiceParameters = askServiceParameters;
		this.askServiceParametersClass = askServiceParametersClass;
		this.askServiceMethod = askServiceMethod;
	}

	public String getAskServiceName() {
		return askServiceName;
	}

	public void setAskServiceName(String askServiceName) {
		this.askServiceName = askServiceName;
	}

	public Object[] getAskServiceParameters() {
		return askServiceParameters;
	}

	public void setAskServiceParameters(Object[] askServiceParameters) {
		this.askServiceParameters = askServiceParameters;
	}

	public Class<?>[] getAskServiceParametersClass() {
		return askServiceParametersClass;
	}

	public void setAskServiceParametersClass(Class[] askServiceParametersClass) {
		this.askServiceParametersClass = askServiceParametersClass;
	}

	public String getAskServiceMethod() {
		return askServiceMethod;
	}

	public void setAskServiceMethod(String askServiceMethod) {
		this.askServiceMethod = askServiceMethod;
	}
}
