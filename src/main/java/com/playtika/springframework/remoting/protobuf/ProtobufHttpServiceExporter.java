package com.playtika.springframework.remoting.protobuf;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.Assert;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.InvocationResult;

/**
 * @author Alex Borisov
 * 
 */
public class ProtobufHttpServiceExporter extends HttpInvokerServiceExporter {

	@Override
	protected RemoteInvocationResult invokeAndCreateResult(
			RemoteInvocation invocation, Object targetObject) {
		try {
			Object value = invoke(invocation, targetObject);
			return create(value);
		} catch (Throwable ex) {
			return new RemoteInvocationResult(ex);
		}
	}

	private RemoteInvocationResult create(Object value) {
		if (value != null) {
			byte[] result = createByteArray(value);
			return new RemoteInvocationResult(result);
		} else {
			return new RemoteInvocationResult(value);
		}
	}

	private byte[] createByteArray(Object value) {
		Assert.isAssignable(Message.class, value.getClass(),
				"Result must be a subclass of " + Message.class);

		String className = value.getClass().getName();
		ByteString bytes = ((Message) value).toByteString();

		return InvocationResult.newBuilder()
				.setClassName(className)
				.setData(bytes)
				.build().toByteArray();
	}
}
