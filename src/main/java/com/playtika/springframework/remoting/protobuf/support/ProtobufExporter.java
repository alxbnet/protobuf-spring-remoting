package com.playtika.springframework.remoting.protobuf.support;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.google.protobuf.Message;
import com.playtika.remoting.protobuf.DefaultInvocationResultFactory;
import com.playtika.remoting.protobuf.InvocationResultFactory;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.RemoteInvocation;
import com.playtika.springframework.remoting.protobuf.ProtobufRemoteInvocationException;

/**
 * @author Alex Borisov
 *
 */
public class ProtobufExporter extends RemoteExporter implements InitializingBean {

	public static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-protobuf";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private InvocationResultFactory factory = new DefaultInvocationResultFactory();
	private RemoteInvocationExecutor remoteInvocationExecutor = new DefaultRemoteInvocationExecutor();
	
	private String contentType = CONTENT_TYPE_SERIALIZED_OBJECT;

	private Object proxy;

	public void setContentType(String contentType) {
		Assert.notNull(contentType, "'contentType' must not be null");
		this.contentType = contentType;
	}

	public String getContentType() {
		return this.contentType;
	}
	
	/**
	 * Initialize this service exporter.
	 */
	public void prepare() {
		this.proxy = getProxyForService();
	}

	protected final Object getProxy() {
		Assert.notNull(this.proxy, ClassUtils.getShortName(getClass()) + " has not been initialized");
		return this.proxy;
	}	
	
	protected Message invoke(RemoteInvocation invocation, Object targetObject)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		logger.trace("Executing {}", invocation);
		try {
			return remoteInvocationExecutor.invoke(invocation, targetObject);
		}
		catch (NoSuchMethodException ex) {
			if (logger.isDebugEnabled()) {
				logger.warn("Could not find target method for " + invocation, ex);
			}
			throw ex;
		}
		catch (IllegalAccessException ex) {
			if (logger.isDebugEnabled()) {
				logger.warn("Could not access target method for " + invocation, ex);
			}
			throw ex;
		}
		catch (InvocationTargetException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Target method failed for " + invocation, ex.getTargetException());
			}
			throw ex;
		}
		catch (ProtobufRemoteInvocationException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("ProtobufRemoteInvocationException not found for " + invocation, ex);
			}
			throw ex;
		}
		
	}

	protected MessageWrapper invokeAndCreateResult(RemoteInvocation invocation, Object targetObject) throws IOException {
		try {
			Message message = invoke(invocation, targetObject);
			return factory.createFromMessage(message);
		}
		catch (Throwable ex) {
			return factory.createFromThrowable(ex);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		prepare();
	}	
	
}
