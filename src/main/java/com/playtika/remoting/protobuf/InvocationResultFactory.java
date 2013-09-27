package com.playtika.remoting.protobuf;

import java.io.IOException;

import com.google.protobuf.Message;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;

/**
 * Creates instances of {@link InvocationResult}
 * 
 * @author Alex Borisov
 *
 */
public interface InvocationResultFactory {
	
	<T extends Message> MessageWrapper createFromMessage(T message);
	
	<T extends Throwable> MessageWrapper createFromThrowable(T throwable) throws IOException;
}
