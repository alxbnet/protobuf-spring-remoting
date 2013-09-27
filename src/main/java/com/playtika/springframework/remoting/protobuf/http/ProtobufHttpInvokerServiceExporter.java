package com.playtika.springframework.remoting.protobuf.http;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;
import org.springframework.web.util.NestedServletException;

import com.playtika.remoting.protobuf.RemoteInvocationProtoc.MessageWrapper;
import com.playtika.remoting.protobuf.RemoteInvocationProtoc.RemoteInvocation;
import com.playtika.springframework.remoting.protobuf.support.ProtobufExporter;

/**
 * 
 * @author Alex Borisov
 * 
 */
public class ProtobufHttpInvokerServiceExporter extends ProtobufExporter
		implements HttpRequestHandler {

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		try {
			RemoteInvocation invocation = readRemoteInvocation(request, request.getInputStream());
			MessageWrapper result = invokeAndCreateResult(invocation, getProxy());
			writeRemoteInvocationResult(request, response, result);
		}
		catch (ClassNotFoundException ex) {
			throw new NestedServletException("Class not found during deserialization", ex);
		}
	}

	private RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is)
			throws IOException, ClassNotFoundException {
		return RemoteInvocation.parseFrom(is);
	}	

	private void writeRemoteInvocationResult(
			HttpServletRequest request, HttpServletResponse response, MessageWrapper result)
			throws IOException {
		
		response.setContentType(getContentType());
		result.writeTo(response.getOutputStream());
	}	
}