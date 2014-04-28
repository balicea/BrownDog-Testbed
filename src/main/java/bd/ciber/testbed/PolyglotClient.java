package bd.ciber.testbed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

public class PolyglotClient {
	private static final Logger LOG = LoggerFactory
			.getLogger(PolyglotClient.class);
	private String hostname;
	private Integer port;
	private boolean sslEnabled;
	private String username;
	private String password;

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isSslEnabled() {
		return sslEnabled;
	}

	public void setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private CredentialsProvider credentialsProvider;

	private HttpClientBuilder httpClientBuilder;

	@PostConstruct
	public void init() {
		credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider
				.setCredentials(new AuthScope(this.hostname, this.port),
						new UsernamePasswordCredentials(username, password));
		httpClientBuilder = HttpClients.custom().setDefaultCredentialsProvider(
				credentialsProvider);
	}

	private String url(String endpoint) {
		String protocol = isSslEnabled() ? "https" : "http";
		String result = MessageFormat.format("{0}://{1}:{2,number,######}/{3}/", protocol,
				hostname, port, endpoint);
		return result;
	}

	public List<String> getConversionsForInput(String extension) {
		List<String> result = new ArrayList<String>();
		HttpGet get = new HttpGet(url("inputs/" + extension));
		try (CloseableHttpClient httpclient = httpClientBuilder.build()) {
			LOG.info("request: {}", get.getURI());
			HttpResponse response = httpclient.execute(get);
			String formats = null;
			LOG.info(get.getRequestLine() + " got response: "
					+ response.getStatusLine());
			formats = EntityUtils.toString(response.getEntity());
			result = Arrays.asList(StringUtils.tokenizeToStringArray(formats, "\n "));
		} catch (ParseException | IOException e) {
			// TODO interpret exceptions
			LOG.error("error", e);
		}
		return result;
	}

	/**
	 * Convert an input into the requested format
	 * @param path the file path, informative of original format
	 * @param in input stream of original data
	 * @param destinationFormat requested destination format (by extension)
	 * @return path to converted data
	 */
	public String convert(String path, InputStream in,
			String destinationFormat) {
		String result = null;
		try (CloseableHttpClient httpclient = httpClientBuilder.build()) {
			HttpPost post = new HttpPost(
					url("convert/" + destinationFormat));
			InputStreamBody bin = new InputStreamBody(in, path);
			HttpEntity reqEntity = MultipartEntityBuilder.create()
					.addPart("uploadfile", bin).build();
			post.setEntity(reqEntity);
			CloseableHttpResponse response = httpclient.execute(post);
			if(response.getEntity().isStreaming()) {
				LOG.info("result is streaming");
				File temp = File.createTempFile("output", "."+destinationFormat);
				OutputStream out = FileUtils.openOutputStream(temp);
				StreamUtils.copy(response.getEntity().getContent(), out);
				result = temp.getAbsolutePath();
			} else {
				LOG.info("result is NOT streaming");
				result = response.getEntity().toString();
			}
			LOG.info(post.getRequestLine() + " got response: "
						+ response.getStatusLine());
			//LOG.info("response body: {}", result);
		} catch (ParseException | IOException e) {
			// TODO interpret exception for caller
			LOG.error("Error", e);
		}
		return result;
	}
}
