package com.playtika.springframework.remoting.protobuf.support;

import org.aopalliance.intercept.MethodInvocation;

import com.playtika.remoting.protobuf.RemoteInvocationProtoc.RemoteInvocation;

/**
 * @author Alex Borisov
 *
 */
public interface RemoteInvocationFactory {

	RemoteInvocation create(MethodInvocation invocation);
	
}
