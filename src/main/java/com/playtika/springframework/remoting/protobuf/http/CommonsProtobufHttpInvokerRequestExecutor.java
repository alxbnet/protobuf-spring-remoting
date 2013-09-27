package com.playtika.springframework.remoting.protobuf.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.RemoteInvocation;

/**
 * @author Alex Borisov
 *
 */
public class CommonsProtobufHttpInvokerRequestExecutor implements
		ProtobufHttpInvokerRequestExecutor {

	
	public static final String CONTENT_TYPE_PROTOBUF_SERIALIZED = "application/x-protobuf-serialized";	
	private static final String HTTP_HEADER_ACCEPT_LANGUAGE = "Accept-Language";
	private static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";
	
	private static final String ENCODING_GZIP = "gzip";	
	private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = 60 * 1000;
	
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String contentType = CONTENT_TYPE_PROTOBUF_SERIALIZED;
	private boolean acceptGzipEncoding = true;
	private HttpClient httpClient;
	
	public CommonsProtobufHttpInvokerRequestExecutor() {
		this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		setReadTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS);
	}
	
	@Override
	public MessageWrapper executeRequest(HttpInvokerClientConfiguration config,
			RemoteInvocation invocation) throws Exception {
		
		logger.debug("Sending HTTP invoker request for service at [{}]: invocation {}", 
				config.getServiceUrl(), invocation);
		
		return doExecuteRequest(config, invocation);
	}

	public final void setReadTimeout(int timeout) {
		Assert.isTrue(timeout >= 0, "Timeout must be a non-negative value");
		this.httpClient.getHttpConnectionManager().getParams().setSoTimeout(timeout);
	}
	
	private MessageWrapper doExecuteRequest(
			HttpInvokerClientConfiguration config, RemoteInvocation message)
			throws IOException, ClassNotFoundException {

		PostMethod postMethod = createPostMethod(config);
		try {
			setRequestBody(config, postMethod, message);
			executePostMethod(config, getHttpClient(), postMethod);
			validateResponse(config, postMethod);
			InputStream responseBody = getResponseBody(config, postMethod);
			return readRemoteInvocationResult(responseBody, config.getCodebaseUrl());
		}
		finally {
			// Need to explicitly release because it might be pooled.
			postMethod.releaseConnection();
		}
	}
	
	private PostMethod createPostMethod(HttpInvokerClientConfiguration config) throws IOException {
		PostMethod postMethod = new PostMethod(config.getServiceUrl());
		LocaleContext locale = LocaleContextHolder.getLocaleContext();
		if (locale != null) {
			postMethod.addRequestHeader(HTTP_HEADER_ACCEPT_LANGUAGE, StringUtils.toLanguageTag(locale.getLocale()));
		}
		if (isAcceptGzipEncoding()) {
			postMethod.addRequestHeader(HTTP_HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
		}
		return postMethod;
	}	
	
	private void setRequestBody(
			HttpInvokerClientConfiguration config, PostMethod postMethod, RemoteInvocation message)
			throws IOException {

		postMethod.setRequestEntity(new ByteArrayRequestEntity(message.toByteArray(), getContentType()));
	}
	
	private void executePostMethod(
			HttpInvokerClientConfiguration config, HttpClient httpClient, PostMethod postMethod)
			throws IOException {
		httpClient.executeMethod(postMethod);
	}
	
	private void validateResponse(HttpInvokerClientConfiguration config, PostMethod postMethod)
			throws IOException {
		if (postMethod.getStatusCode() >= 300) {
			throw new HttpException(
					"Did not receive successful HTTP response: status code = " + postMethod.getStatusCode() +
					", status message = [" + postMethod.getStatusText() + "]");
		}
	}
	
	private InputStream getResponseBody(HttpInvokerClientConfiguration config, PostMethod postMethod)
			throws IOException {

		if (isGzipResponse(postMethod)) {
			return new GZIPInputStream(postMethod.getResponseBodyAsStream());
		}
		else {
			return postMethod.getResponseBodyAsStream();
		}
	}
	
	private boolean isGzipResponse(PostMethod postMethod) {
		Header encodingHeader = postMethod.getResponseHeader(HTTP_HEADER_CONTENT_ENCODING);
		return (encodingHeader != null && encodingHeader.getValue() != null &&
				encodingHeader.getValue().toLowerCase().contains(ENCODING_GZIP));
	}	
	
	private MessageWrapper readRemoteInvocationResult(InputStream is, String codebaseUrl)
			throws IOException, ClassNotFoundException {
		
		BufferedInputStream bufferedStream = new BufferedInputStream(is);
		try {
			return MessageWrapper.parseFrom(bufferedStream);
		}
		finally {
			bufferedStream.close();
		}
	}	
	
	public void setContentType(String contentType) {
		Assert.notNull(contentType, "'contentType' must not be null");
		this.contentType = contentType;
	}

	public String getContentType() {
		return this.contentType;
	}		
	
	/**
	 * Set whether to accept GZIP encoding, that is, whether to
	 * send the HTTP "Accept-Encoding" header with "gzip" as value.
	 * <p>Default is "true". Turn this flag off if you do not want
	 * GZIP response compression even if enabled on the HTTP server.
	 */
	public void setAcceptGzipEncoding(boolean acceptGzipEncoding) {
		this.acceptGzipEncoding = acceptGzipEncoding;
	}

	/**
	 * Return whether to accept GZIP encoding, that is, whether to
	 * send the HTTP "Accept-Encoding" header with "gzip" as value.
	 */
	public boolean isAcceptGzipEncoding() {
		return this.acceptGzipEncoding;
	}
	
	/**
	 * Set the HttpClient instance to use for this request executor.
	 */
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Return the HttpClient instance that this request executor uses.
	 */
	public HttpClient getHttpClient() {
		return this.httpClient;
	}	
	
}
