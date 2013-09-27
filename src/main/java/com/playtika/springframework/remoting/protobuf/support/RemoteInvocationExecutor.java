package com.playtika.springframework.remoting.protobuf.support;

import java.lang.reflect.InvocationTargetException;

import com.google.protobuf.Message;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.RemoteInvocation;
import com.playtika.springframework.remoting.protobuf.ProtobufRemoteInvocationException;

/**
 * 
 * @author Alex Borisov
 *
 */
public interface RemoteInvocationExecutor {
	
	Message invoke(RemoteInvocation invocation, Object targetObject)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ProtobufRemoteInvocationException;
}
