package org.wulin.jvm.jdk.thread.future;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class RPCFutureTest {
	
	@Test
	public void testRpcFutureBlockingStatus() {
		
		RpcRequest request = new RpcRequest();
		RPCFuture rpcFuture = new RPCFuture(request);
		
		try {
			Object object = rpcFuture.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
