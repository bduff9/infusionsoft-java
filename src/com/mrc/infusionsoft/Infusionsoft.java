package com.mrc.infusionsoft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Infusionsoft {

	public static final String	POST = "POST";
	public static final String	GET = "GET";
	/**
	 * URL all XML-RPC requests are sent to
	 */
	@SuppressWarnings("unused")
	private static final String	XML_RPC_URL = "https://api.infusionsoft.com/crm/xmlrpc/v1";
	/**
	 * URL a user visits to authorize an access token
	 */
	private static final String	AUTH_URL = "https://signin.infusionsoft.com/app/oauth/authorize";
	/**
	 * Base URL of all API requests
	 */
	private static final String	BASE_URI = "https://api.infusionsoft.com/crm";
	/**
	 * URL used to request an access token
	 */
	private static final String	TOKEN_URI = "https://api.infusionsoft.com/token";

	@SerializedName("clientId")
	@Expose
	private String clientId;
	@SerializedName("clientSecret")
	@Expose
	private String clientSecret;
	@SerializedName("redirectUri")
	@Expose
	private String	redirectUri;
	@SerializedName("debug")
	@Expose
	private boolean	debug = false;
	@SerializedName("needsEmptyKey")
	@Expose
	private boolean	needsEmptyKey = true;
	@SerializedName("token")
	@Expose
	private Token	token = null;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public Infusionsoft() {}

	/**
	 * 
	 * @param clientId
	 * @param clientSecret
	 * @param redirectUri
	 */
	public Infusionsoft(String clientId, String clientSecret, String redirectUri) {
		super();

		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.redirectUri = redirectUri;
		this.debug = false;
	}

	/**
	 * 
	 * @param clientId
	 * @param clientSecret
	 * @param redirectUri
	 * @param debug
	 */
	public Infusionsoft(String clientId, String clientSecret, String redirectUri, boolean debug) {
		super();

		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.redirectUri = redirectUri;
		this.debug = debug;
	}

	public String httpBuildQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
		StringBuilder	queryString = new StringBuilder();

		for (NameValuePair pair : params) {
			queryString.append(URLEncoder.encode((String) pair.getName(), "UTF-8") + "=");
			queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8") + "&");
		}

		if (queryString.length() > 0) queryString.deleteCharAt(queryString.length() - 1);

		return queryString.toString();
	}

	/**
	 * 
	 * @return string
	 * @throws UnsupportedEncodingException 
	 */
	public String getAuthorizationUrl(String state) throws UnsupportedEncodingException {
		List<NameValuePair> params = Arrays.asList(
				new BasicNameValuePair("client_id", this.clientId),
				new BasicNameValuePair("redirect_uri", this.redirectUri),
				new BasicNameValuePair("response_type", "code"),
				new BasicNameValuePair("scope", "full")
				);

		if (state != null) params.add(new BasicNameValuePair("state", state));

		return AUTH_URL + "?" + httpBuildQuery(params);
	}

	/**
	 * 
	 * @param String code
	 *
	 * @return Token
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 */
	@SuppressWarnings("unchecked")
	public Token requestAccessToken(String code) throws IOException, ParseException, URISyntaxException {
		List<NameValuePair> params = Arrays.asList(
				new BasicNameValuePair("client_id", this.clientId),
				new BasicNameValuePair("client_secret", this.clientSecret),
				new BasicNameValuePair("code", code),
				new BasicNameValuePair("grant_type", "authorization_code"),
				new BasicNameValuePair("redirect_uri", this.redirectUri)
				);
		Map<String, String>	headers = new HashMap<>();
		JSONObject	json;
		Token	aToken;

		headers.put("Content-Type", "application/x-www-form-urlencoded");

		json = callInfusionsoftAPI(POST, TOKEN_URI, headers, params);
		aToken = new Token((Map<String, Object>) json);

		setToken(aToken);

		return aToken;
	}

	/**
	 * 
	 * @param method
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws URISyntaxException
	 */
	@SuppressWarnings("resource")
	private JSONObject callInfusionsoftAPI(String method, String url, Map<String, String> headers, List<NameValuePair> params) throws IOException, ParseException, URISyntaxException {
		HttpClient	client = HttpClientBuilder.create().build();
		HttpResponse	response;
		JSONObject	json;
		BufferedReader	br;
		StringBuilder	result = new StringBuilder();
		String	line;
		int	responseCode;

		if (POST.equals(method)) {
			HttpPost	post = new HttpPost(url);

			for (String key : headers.keySet()) post.setHeader(key, headers.get(key));

			post.setEntity(new UrlEncodedFormEntity(params));

			response = client.execute(post);
		} else if (GET.equals(method)) {
			URIBuilder	uriBuilder = new URIBuilder(url);
			HttpGet	get;

			uriBuilder.addParameters(params);

			get = new HttpGet(uriBuilder.build());

			for (String key : headers.keySet()) get.setHeader(key, headers.get(key));

			response = client.execute(get);
		} else {
			throw new IOException("Invalid method passed: " + method);
		}

		responseCode = response.getStatusLine().getStatusCode();

		if (responseCode >= 400) throw new IOException("Call to Infusionsoft failed with reason " + responseCode + " - " + response.getStatusLine().getReasonPhrase());

		br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		while ((line = br.readLine()) != null) result.append(line);

		json = (JSONObject) new JSONParser().parse(result.toString());

		return json;
	}

	/**
	 * 
	 * @return array
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 */
	@SuppressWarnings("unchecked")
	public Token refreshAccessToken() throws IOException, ParseException, URISyntaxException {
		List<NameValuePair>	params = Arrays.asList(
				new BasicNameValuePair("grant_type", "refresh_token"),
				new BasicNameValuePair("refresh_token", token.getRefreshToken())
				);
		Map<String, String>	headers = new HashMap<>();
		JSONObject	json;
		Token	aToken;
		String	creds = Base64.encodeBase64String((getClientId() + ":" + getClientSecret()).getBytes());

		headers.put("Authorization", "Basic " + creds);
		headers.put("Content-Type", "application/x-www-form-urlencoded");

		json = callInfusionsoftAPI(POST, TOKEN_URI, headers, params);
		aToken = new Token((Map<String, Object>) json);

		setToken(aToken);

		return aToken;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws URISyntaxException
	 */
	public JSONObject getUserInfo() throws IOException, ParseException, URISyntaxException {
		List<NameValuePair>	params = Arrays.asList(
				new BasicNameValuePair("access_token", getToken().getAccessToken())
				);
		Map<String, String>	headers = new HashMap<>();
		JSONObject	json;
		String	aUrl = BASE_URI + "/rest/v1/oauth/connect/userinfo";

		json = callInfusionsoftAPI(GET, aUrl, headers, params);

		return json;
	}

	/**
	 * Checks if the current token is null or expired
	 *
	 * @return boolean
	 */
	public boolean isTokenExpired() {
		if (token == null) return true;

		return token.isExpired();
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isNeedsEmptyKey() {
		return needsEmptyKey;
	}

	public void setNeedsEmptyKey(boolean needsEmptyKey) {
		this.needsEmptyKey = needsEmptyKey;
	}

	/**
	 * 
	 * @return Token
	 * @throws URISyntaxException 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public Token getToken() throws IOException, ParseException, URISyntaxException {
		if (this.token == null) return this.token;

		if (isTokenExpired()) return refreshAccessToken();

		return this.token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("clientId", clientId).append("clientSecret", clientSecret).append("redirectUri", redirectUri).append("debug", debug).append("needsEmptyKey", needsEmptyKey).append("token", token).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(clientId).append(clientSecret).append(redirectUri).append(debug).append(needsEmptyKey).append(token).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) return true;

		if (!(other instanceof Infusionsoft)) return false;

		Infusionsoft	rhs = ((Infusionsoft) other);

		return new EqualsBuilder().append(clientId, rhs.clientId).append(clientSecret, rhs.clientSecret).append(redirectUri, rhs.redirectUri).append(debug, rhs.debug).append(needsEmptyKey, rhs.needsEmptyKey).append(token, rhs.token).isEquals();
	}
}