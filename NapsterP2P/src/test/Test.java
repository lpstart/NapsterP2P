package test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.napster.rpc.RPC;
import com.napster.service.IndexServerService;
import com.napster.service.IndexServerServiceImpl;
import com.napster.util.RPCBean;

public class Test {

	public static void main(String[] args) throws Exception {
		new Test().start();
	}

	public void start() throws Exception {
//		test1();
		InetAddress dd = InetAddress.getByName("sunrise.cis.unimelb.edu.au");
		System.out.println(dd.isReachable(200));
	}

	public void test1() {
		InvocationHandler invocationHandler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				RPCBean rb = new RPCBean();
				rb.setAskServiceMethod(method.getName());
				rb.setAskServiceName(IndexServerService.class.getName());
				rb.setAskServiceParameters(args);
				rb.setAskServiceParametersClass(method.getParameterTypes());
				Class<?>[] dd = rb.getAskServiceParametersClass();
				for(Class<?> d:dd)
				System.out.println(d.getClass().getName());
				
				return null;
			}
		};

		IndexServerService i = (IndexServerService) Proxy.newProxyInstance(RPC.class.getClassLoader(),
				new Class[] { IndexServerService.class }, invocationHandler);
		i.search("asdf");
	}

}
