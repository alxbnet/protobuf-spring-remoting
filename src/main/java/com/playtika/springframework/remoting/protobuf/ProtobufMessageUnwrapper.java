package com.playtika.springframework.remoting.protobuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.ClassUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.playtika.remoting.protobuf.MessageUnwrapper;
import com.playtika.remoting.protobuf.MessageWithClass;
import com.playtika.remoting.protobuf.MessageWithClass.Builder;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;

/**
 * 
 * @author Alex Borisov
 *
 */
public class ProtobufMessageUnwrapper implements MessageUnwrapper {

	@Override
	public MessageWithClass fromWrapper(MessageWrapper wrapper,
			boolean unwrapThrowable) throws ProtobufRemoteInvocationException {
		String className = wrapper.getClassName();
		Class<?> resultClass;
		try {
			resultClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new ProtobufRemoteInvocationException("Message class not found.", e);
		}
		
		Builder builder = new Builder().ofClass(resultClass);
		
		if (ClassUtils.isAssignable(resultClass, Message.class)) {
			builder = unwrapMessage(resultClass, wrapper.getData(), builder);
		} else if (shouldUnwrapThrowable(unwrapThrowable, resultClass)){
			builder = unwrapThrowable(wrapper.getData(), builder);	
		} else {		
			throw new ProtobufRemoteInvocationException("Message should be a subclass of Message.class. " + resultClass);
		}
		
		return builder.build();
	}

	private boolean shouldUnwrapThrowable(boolean unwrapThrowable,
			Class<?> resultClass) {
		return unwrapThrowable && ClassUtils.isAssignable(resultClass, Message.class);
	}

	private Builder unwrapMessage(Class<?> resultClass, ByteString data, Builder builder) {
		try {
			return unwrap(resultClass, data, builder);
		} catch (Exception ex) {
			throw new ProtobufRemoteInvocationException("Exception on message unwrapping.", ex);
		}		
	}

	private Builder unwrap(Class<?> resultClass, ByteString data, Builder builder)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InvalidProtocolBufferException {
		
		Method builderMethod = resultClass.getMethod("newBuilder");
		Message.Builder messageBuilder = (Message.Builder)builderMethod.invoke(resultClass);
		Message message = messageBuilder.mergeFrom(data).build();
		builder.withMessage(message);
		return builder;
	}

	private Builder unwrapThrowable(ByteString data, Builder builder) {
		Object object = deserializeObject(data);		
		return setThrowable(builder, object);
	}

	private Builder setThrowable(Builder builder, Object object) {
		if (ClassUtils.isAssignable(object.getClass(), Throwable.class)) {
			builder.withThrowable((Throwable)object);
			return builder;
		} else {
			throw new ProtobufRemoteInvocationException("Throwable should be a subclass of Throwable.class. " + object);
		}
	}

	private Object deserializeObject(ByteString data) {
		Object object;
		try {
			object = deserialize(data);
		} catch (Exception e) {
			throw new ProtobufRemoteInvocationException("Exception on deserialization.", e);
		}
		return object;
	}

	private Object deserialize(ByteString data) throws IOException, ClassNotFoundException {
    	ByteArrayInputStream bis = new ByteArrayInputStream(data.toByteArray());
    	ObjectInput in = null;
    	try {
    	  in = new ObjectInputStream(bis);
    	  Object object = in.readObject(); 
    	  return object;
    	} finally {
    	  bis.close();
    	  if (in != null) {
    		  in.close();
    	  }
    	}
    }	
}