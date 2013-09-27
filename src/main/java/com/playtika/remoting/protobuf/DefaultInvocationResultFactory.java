package com.playtika.remoting.protobuf;

import static com.google.protobuf.ByteString.copyFrom;
import static com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper.newBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.google.protobuf.Message;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;

/**
 * @author Alex Borisov
 * 
 */
public class DefaultInvocationResultFactory implements InvocationResultFactory {

	public <T extends Message> MessageWrapper createFromMessage(T message) {
		return newBuilder().setClassName(message.getClass().getName())
				.setData(message.toByteString()).build();
	}

	public <T extends Throwable> MessageWrapper createFromThrowable(
			T throwable) throws IOException {
		return newBuilder().setClassName(throwable.getClass().getName())
				.setData(copyFrom(serialize(throwable))).build();
	}

	public byte[] serialize(Throwable th) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(th);
			return bos.toByteArray();
		} finally {
			out.close();
			bos.close();
		}
	}

}
