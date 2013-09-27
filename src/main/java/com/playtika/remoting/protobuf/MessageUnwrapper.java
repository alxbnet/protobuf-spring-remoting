package com.playtika.remoting.protobuf;

import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;

/**
 * 
 * @author Alex Borisov
 *
 */
public interface MessageUnwrapper {

	MessageWithClass fromWrapper(MessageWrapper wrapper, boolean unwrapThrowable);
	
}