package com.playtika.springframework.remoting.protobuf;

import static com.playtika.remoting.protobuf.RemoteInvocationProtoc.InvocationResult.newBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.httpinvoker.HttpInvokerClientInterceptor;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.util.Assert;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.InvocationResult;

/**
 * 
 * {@link org.aopalliance.intercept.MethodInterceptor} for accessing an HTTP
 * invoker service with Protobuf messages.
 * 
 * <p>
 * Serializes remote invocation objects and deserializes remote invocation
 * result objects using Protobuf.
 * 
 * @author Alex Borisov
 * 
 */
public class ProtobufHttpClientInterceptor extends HttpInvokerClientInterceptor {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	protected Message recreateRemoteInvocationResult(
			RemoteInvocationResult result) throws Throwable {

		Object invocationResult = result.recreate();
		Assert.isAssignable(byte[].class, invocationResult.getClass(),
				"Invocation result must be byte array.");

		InvocationResult invocationResultMsg = readInvocationResult(invocationResult);
		return readMessageFromInvocationResult(invocationResultMsg);
	}

	private InvocationResult readInvocationResult(Object invocationResult)
			throws InvalidProtocolBufferException {
		return newBuilder().mergeFrom((byte[]) invocationResult).build();
	}

	private Message readMessageFromInvocationResult(
			InvocationResult invocationResultMsg)
			throws ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException,
			InvalidProtocolBufferException {
		
		if (invocationResultMsg.hasData()) {
			String className = invocationResultMsg.getClassName();

			// TODO NoClassDefFound
			// TODO cache
			Class<?> resultClass = Class.forName(className);
			Method newBuilder = resultClass.getMethod("newBuilder");
			GeneratedMessage.Builder<?> builder = (GeneratedMessage.Builder<?>) newBuilder
					.invoke(resultClass);
			Message msg = builder.mergeFrom(invocationResultMsg.getData())
					.build();

			logger.trace("Invocation result: class {} message {}.", className,
					msg);
			return msg;
		} else {
			logger.trace("Invocation result has no data.");
			return null;
		}
	}

}