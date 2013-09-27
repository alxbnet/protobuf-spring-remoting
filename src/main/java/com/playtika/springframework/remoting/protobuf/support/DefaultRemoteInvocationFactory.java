package com.playtika.springframework.remoting.protobuf.support;

import static org.apache.commons.lang.ArrayUtils.isEmpty;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ClassUtils;

import com.google.protobuf.Message;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.RemoteInvocation;
import com.playtika.springframework.remoting.protobuf.ProtobufRemoteInvocationException;

/**
 * @author Alex Borisov
 *
 */
public class DefaultRemoteInvocationFactory implements RemoteInvocationFactory {

	@Override
	public RemoteInvocation create(MethodInvocation invocation) {		
		
		Method method = invocation.getMethod();		
		RemoteInvocation.Builder builder = 
				RemoteInvocation.newBuilder().setMethod(method.getName());
		
		setArguments(invocation, method, builder);		
		return builder.build();
	}

	private void setArguments(MethodInvocation invocation, Method method,
			RemoteInvocation.Builder builder) {
		Class<?>[] argTypes = method.getParameterTypes();
		Object[] args = invocation.getArguments();
		if (!isEmpty(argTypes)) {
			for (int index = 0; index < argTypes.length; index++) {
				wrapArgument(builder, argTypes, args, index);	
			}
		}
	}

	private void wrapArgument(RemoteInvocation.Builder builder,
			Class<?>[] argTypes, Object[] args, int index) {
		Class<?> type = argTypes[index];
		Object arg = args[index];
		builder.addArgument(index, wrap(type, arg));
	}

	private MessageWrapper wrap(Class<?> type, Object object) {
		if (!ClassUtils.isAssignable(type, Message.class)) {			
			throw new ProtobufRemoteInvocationException("Arguments must be subclasses of Message. Found " + type);
		}
		
		MessageWrapper.Builder builder = 
				MessageWrapper.newBuilder().setClassName(type.getName());
		if (object != null) {			
			builder.setData(((Message)object).toByteString());
		}
		return builder.build();
	}
	
}
