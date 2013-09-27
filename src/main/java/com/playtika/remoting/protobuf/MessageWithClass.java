package com.playtika.remoting.protobuf;

import com.google.protobuf.Message;

/**
 * 
 * @author Alex Borisov
 * 
 */
public class MessageWithClass {

	private final Message message;
	private final Class<?> messageClass;
	private final Throwable throwable;

	private MessageWithClass(Builder builder) {
		this.message = builder.message;
		this.messageClass = builder.messageClass;
		this.throwable = builder.throwable;
	}

	public Message getMessage() {
		return message;
	}

	public Class<?> getMessageClass() {
		return messageClass;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public static class Builder {

		private Message message;
		private Class<?> messageClass;
		private Throwable throwable;

		public Builder withMessage(Message message) {
			this.message = message;
			return this;
		}

		public Builder ofClass(Class<?> messageClass) {
			this.messageClass = messageClass;
			return this;
		}

		public Builder withThrowable(Throwable throwable) {
			this.throwable = throwable;
			return this;
		}		
		
		public MessageWithClass build() {
			return new MessageWithClass(this);
		}

	}

}
