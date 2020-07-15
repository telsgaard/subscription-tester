package com.ims.tool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.io.IOUtils;

public final class SOAPClient {

	private static final Logger logger = Logger.getLogger(SOAPClient.class.getName());

	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static final String CONTENT_TYPE = "text/xml";
	private static final int TIMEOUT = 2000;
	private static final String USER = "user";
	private static final String PASS = "psw";
	private static final String URL = "http://127.0.0.1:8080/CAI3G1.2/services/CAI3G1.2";


	public Response executeRequest(String resourcePath, Map<String, String> replacements) throws Exception {
		logger.log(Level.INFO, "Execute Request is called");
		Response logInResponse = login();

		if (!logInResponse.getSessionId().isPresent()) {
			logger.log(Level.SEVERE, "Failed to login.");
			return logInResponse;
		}

		String sessionId = logInResponse.getSessionId().get();
		logger.log(Level.INFO, "Retrieved session id : " + sessionId);
		String envelope = fileToString(resourcePath);
		envelope = amendSessionId(envelope, sessionId);

		for (Entry<String, String> entry : replacements.entrySet()) {
			envelope = envelope.replace(entry.getKey(), entry.getValue());
		}


		logger.log(Level.INFO, "Request envelope : \n" + envelope);
		logger.log(Level.FINE, "Request initiated for resource " + resourcePath);
		logger.log(Level.FINE, "Request : " + envelope);
		Response response = call(envelope);
		logger.log(Level.FINE, "Response received for resource " + resourcePath);
		logger.log(Level.FINE, "Response : " + response);
		Response logOutResponse = logout(sessionId);

		if (logOutResponse.getHttpStatus() != HttpStatus.SC_OK) {
			return logOutResponse;
		}
		return response;
	}

	private Response call(String envelope) throws Exception {
		HttpClient httpClient = new HttpClient();
		HttpParams params = httpClient.getParams();
		params.setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, TIMEOUT);

		PostMethod methodPost = new PostMethod(URL);
		Response response = new Response();
		try {
			StringRequestEntity stringRequestEntity = new StringRequestEntity(envelope, CONTENT_TYPE,
					CHARSET.displayName());
			methodPost.setRequestEntity(stringRequestEntity);
			int httpStatusCode = httpClient.executeMethod(methodPost);
			logger.log(Level.INFO, "HTTP method return code : " + httpStatusCode);
			response.setResponseBody(methodPost.getResponseBodyAsString());
			response.setHttpStatus(httpStatusCode);

			if (httpStatusCode != HttpStatus.SC_OK) {
				String errorResponse = methodPost.getResponseBodyAsString();
				logger.log(Level.INFO, "Error in SOAP Response :\n" + errorResponse);
			}
		} catch (ConnectTimeoutException e) {
			logger.log(Level.SEVERE, "Connection timeout.", e);
			throw e;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Problem in calling SOAP service.", e);
			throw e;
		} finally {
			methodPost.releaseConnection();

		}
		return response;
	}

	private Response login() throws Exception {
		logger.log(Level.INFO, "Calling login service to get session id");
		String loginEnvelope = fileToString(ResourcePaths.LOGIN_ENVELOP_XML);

		logger.log(Level.INFO, "Logging in as a user : " + USER);
		loginEnvelope = loginEnvelope.replace("${USER_ID}", USER);
		loginEnvelope = loginEnvelope.replace("${PASSWORD}", PASS);

		logger.log(Level.INFO, "Login envelope : \n" + loginEnvelope);
		Response response = call(loginEnvelope);

		if (response.getHttpStatus() == HttpStatus.SC_OK) {
			Pattern pattern = Pattern.compile("<sessionId>(.*?)</sessionId>");
			Matcher matcher = pattern.matcher(response.getResponseBody());
			if (matcher.find()) {
				String sessionId = matcher.group(1);
				response.setSessionId(sessionId);
			}
		}

		return response;
	}

	private Response logout(String sessionId) throws Exception {
		logger.log(Level.INFO, "Calling Logout service for session id.." + sessionId);
		String logoutEnvelope = fileToString(ResourcePaths.LOGOUT_ENVELOPE_XML);
		logger.log(Level.INFO, "Logout envelope : \n" + logoutEnvelope);
		return call(amendSessionId(logoutEnvelope, sessionId));
	}

	private String fileToString(String fileName) {
		InputStream stream = SOAPClient.class.getClassLoader().getResourceAsStream(fileName);
		try {
			return IOUtils.toString(stream, CHARSET);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problem in reading file :" + fileName, e);
			logger.log(Level.INFO, "Failed to logout.");
		}
		return null;
	}

	private String amendSessionId(String envelop, String sessionId) {
		envelop = envelop.replace("${SessionId}", sessionId);
		return envelop;
	}
}
