package com.playtika.springframework.remoting.protobuf.http;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * 
 * @author Alex Borisov
 * 
 */
public class ProtobufHttpProxyFactoryBean extends ProtobufHttpClientInterceptor
		implements FactoryBean<Object> {

	private Object serviceProxy;

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (getServiceInterface() == null) {
			throw new IllegalArgumentException(
					"Property 'serviceInterface' is required");
		}
		this.serviceProxy = new ProxyFactory(getServiceInterface(), this)
				.getProxy(getBeanClassLoader());
	}

	public Object getObject() {
		return this.serviceProxy;
	}

	public Class<?> getObjectType() {
		return getServiceInterface();
	}

	public boolean isSingleton() {
		return true;
	}

}