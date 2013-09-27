package com.playtika.springframework.remoting.protobuf.http;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

import com.google.protobuf.Message;
import com.playtika.remoting.protobuf.MessageUnwrapper;
import com.playtika.remoting.protobuf.MessageWithClass;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.RemoteInvocation;
import com.playtika.springframework.remoting.protobuf.ProtobufMessageUnwrapper;
import com.playtika.springframework.remoting.protobuf.ProtobufRemoteInvocationException;
import com.playtika.springframework.remoting.protobuf.support.DefaultRemoteInvocationFactory;
import com.playtika.springframework.remoting.protobuf.support.RemoteInvocationFactory;

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
public class ProtobufHttpClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor, HttpInvokerClientConfiguration {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private RemoteInvocationFactory remoteInvocationFactory = 
			new DefaultRemoteInvocationFactory();
	private ProtobufHttpInvokerRequestExecutor requestExecutor = 
			new CommonsProtobufHttpInvokerRequestExecutor();
	
	private	MessageUnwrapper messageUnwrapper = new ProtobufMessageUnwrapper();
			
	private String codebaseUrl;
	
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		
		if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
			return "HTTP invoker proxy for service URL [" + getServiceUrl() + "]";
		}

		RemoteInvocation invocationMessage = createRemoteInvocation(methodInvocation);
		MessageWrapper resultWrapper;
		try {
			resultWrapper = executeRequest(invocationMessage);
		}
		catch (Throwable ex) {
			throw new ProtobufRemoteInvocationException("Execption on method invocation " + methodInvocation.getMethod(), ex);
		}
		
		Message result;
		MutableBoolean hasRemoteException = new MutableBoolean(false);
		try {
			result = unwrapMessage(resultWrapper, hasRemoteException);
		}
		catch (Throwable ex) {
			if (hasRemoteException.booleanValue()) {
				throw ex;
			}
			throw new ProtobufRemoteInvocationException("Execption on message unwrapping." + methodInvocation.getMethod(), ex);
		}
		
		return result;
	}
	
	public void setCodebaseUrl(String codebaseUrl) {
		this.codebaseUrl = codebaseUrl;
	}

	public String getCodebaseUrl() {
		return this.codebaseUrl;
	}	
	
	private RemoteInvocation createRemoteInvocation(MethodInvocation invocation) {
		return remoteInvocationFactory.create(invocation);
	}

	private MessageWrapper executeRequest(
			RemoteInvocation invocation) throws Exception {

		return requestExecutor.executeRequest(this, invocation);
	}
	
	private Message unwrapMessage(MessageWrapper resultWrapper, MutableBoolean hasRemoteException) throws Throwable {
		MessageWithClass unwrapped = messageUnwrapper.fromWrapper(resultWrapper, true);
		if (unwrapped.getThrowable() != null) {
			hasRemoteException.setValue(true);
			throw unwrapped.getThrowable();
		}
		return unwrapped.getMessage();
	}

	public void setMessageUnwrapper(MessageUnwrapper messageUnwrapper) {
		this.messageUnwrapper = messageUnwrapper;
	}

}