package com.playtika.springframework.remoting.protobuf;

/**
 * 
 * @author Alex Borisov
 *
 */
public class ProtobufRemoteInvocationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6770884624518722274L;

	public ProtobufRemoteInvocationException(String message) {
		super(message);
	}

	public ProtobufRemoteInvocationException(String message, Throwable cause) {
		super(message, cause);
	}

}
