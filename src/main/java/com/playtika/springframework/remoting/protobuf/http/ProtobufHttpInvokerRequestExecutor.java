package com.playtika.springframework.remoting.protobuf.http;

import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;

import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.RemoteInvocation;

/**
 * @author Alex Borisov
 *
 */
public interface ProtobufHttpInvokerRequestExecutor {
	
	MessageWrapper executeRequest(HttpInvokerClientConfiguration config, RemoteInvocation invocation)
			throws Exception;
}