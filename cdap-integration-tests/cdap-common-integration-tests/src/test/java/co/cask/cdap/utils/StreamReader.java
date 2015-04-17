/*
 * Copyright © 2014-2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.utils;

import co.cask.cdap.client.rest.RestClient;
import co.cask.cdap.client.rest.RestClientConnectionConfig;
import co.cask.cdap.client.rest.RestUtil;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import co.cask.cdap.security.authentication.client.basic.BasicAuthenticationClient;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for getting delivered events
 */
public class StreamReader implements Closeable {

  private static final String DEFAULT_VERSION = "v3";
  private static final String DEFAULT_NAMESPACE = Constants.DEFAULT_NAMESPACE;
  private static final String DEFAULT_AUTH_CLIENT_CLASS_NAME = BasicAuthenticationClient.class.getName();
  private static final Gson GSON = new Gson();
  private static final Type STREAM_EVENTS_TYPE = new TypeToken<List<StreamEvent>>() { }.getType();

  private final String cdapHost;
  private final int cdapPort;
  private final Boolean ssl;
  private final String authClientClassName;
  private final String authClientPropertiesPath;
  private final RestClient restClient;
  private final boolean verifySSLCert;

  private StreamReader(String cdapHost, int cdapPort, Boolean ssl, String authClientClassName,
                       String authClientPropertiesPath, String version, String namespace, boolean verifySSLCert)
    throws Exception {

    this.cdapHost = cdapHost;
    this.cdapPort = cdapPort;
    this.ssl = ssl;
    this.authClientClassName = authClientClassName;
    this.authClientPropertiesPath = authClientPropertiesPath;
    this.verifySSLCert = verifySSLCert;

    AuthenticationClient authClient = createAuthClient();

    RestClientConnectionConfig connectionConfig =
      new RestClientConnectionConfig(cdapHost, cdapPort, authClient, StringUtils.EMPTY, ssl, version, namespace);

    restClient = new RestClient(connectionConfig, createConnectionManager());
  }

  private PoolingHttpClientConnectionManager createConnectionManager() throws NoSuchAlgorithmException,
    KeyManagementException {

    Registry<ConnectionSocketFactory> connectionRegistry = null;
    if (!verifySSLCert) {
      connectionRegistry = RestUtil.getRegistryWithDisabledCertCheck();
    }
    PoolingHttpClientConnectionManager clientConnectionManager;
    if (connectionRegistry != null) {
      clientConnectionManager = new PoolingHttpClientConnectionManager(connectionRegistry);
    } else {
      clientConnectionManager = new PoolingHttpClientConnectionManager();
    }
    return clientConnectionManager;
  }

  public String getCdapHost() {
    return cdapHost;
  }

  public int getCdapPort() {
    return cdapPort;
  }

  public Boolean getSsl() {
    return ssl;
  }

  public String getAuthClientPropertiesPath() {
    return authClientPropertiesPath;
  }

  /**
   * Retrieves events from specified Stream in Cdap Server that were delivered in time
   * between {@code startTime} and {@code endTime}
   *
   * @param streamName the stream name
   * @param startTime the start time
   * @param endTime the end time
   * @return delivered events
   * @throws Exception
   */
  public List<String> getDeliveredEvents(String streamName, long startTime, long endTime) throws Exception {
    String eventsStr = readFromStream(streamName, startTime, endTime);
    List<StreamEvent> eventList = Lists.newArrayList();
    if (StringUtils.isNotEmpty(eventsStr)) {
      eventList = GSON.fromJson(eventsStr, STREAM_EVENTS_TYPE);
    }
    List<String> events = new ArrayList<String>(eventList.size());
    for (StreamEvent event : eventList) {
      events.add(event.getBody());
    }
    return events;
  }

  /**
   * Retrieves events from specified Stream in Cdap Server
   *
   * @param streamName the stream name
   * @return delivered events
   * @throws Exception
   */
  public List<String> getDeliveredEvents(String streamName) throws Exception {
    return getDeliveredEvents(streamName, 0L, 0L);
  }

  /**
   * Retrieves stream by specified stream name.
   *
   * @param name the stream name
   * @return Json Object with Stream Record
   * @throws IOException in case of a problem or the connection was aborted
   */
  public JsonObject getStream(String name) throws IOException {
    HttpGet httpGet = new HttpGet(restClient.resolve(String.format("/streams/%s", name)));
    HttpResponse httpResponse = restClient.execute(httpGet);
    return GSON.fromJson(EntityUtils.toString(httpResponse.getEntity()), JsonObject.class);
  }

  private String readFromStream(String streamName, long startTime, long endTime) throws Exception {
    String urlPostfix;
    if (startTime == 0 && endTime == 0) {
      urlPostfix = String.format("/streams/%s/events", streamName);
    } else {
      urlPostfix = String.format("/streams/%s/events?start=%s&end=%s", streamName, startTime, endTime);
    }
    HttpGet getRequest =  new HttpGet(restClient.resolve(urlPostfix));
    HttpResponse response = restClient.execute(getRequest);
    return response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : null;
  }

  /**
   * Creates the Authorization client instance and configures with appropriate Gateway server parameters.
   *
   * @return configured {@link co.cask.cdap.security.authentication.client.AuthenticationClient} instance
   * @throws Exception
   */
  public AuthenticationClient createAuthClient() throws Exception {
    AuthenticationClient authClient = (AuthenticationClient) Class.forName(authClientClassName).newInstance();
    if (StringUtils.isNotEmpty(cdapHost)) {
      authClient.setConnectionInfo(cdapHost, cdapPort, ssl);
      if (StringUtils.isNotEmpty(authClientPropertiesPath)) {
        Properties authClientProperties = getProperties(authClientPropertiesPath);
        authClient.configure(authClientProperties);
      }
    }
    return authClient;
  }

  /**
   * Retrieves {@link java.util.Properties} instance by the specified file name.
   *
   * @param fileName the name of property file
   * @return {@link java.util.Properties} instance created from the specified file
   * @throws IOException if an error occurred when reading from the input stream
   */
  public static Properties getProperties(String fileName) throws IOException {
    Properties properties = new Properties();
    properties.load(StreamReader.class.getClassLoader().getResourceAsStream(fileName));
    return properties;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public void close() throws IOException {
    restClient.close();
  }

  /**
   * Builder for StreamReader
   */
  public static class Builder {

    private String cdapHost;

    private int cdapPort;
    private Boolean ssl;
    private String authClientClassName = DEFAULT_AUTH_CLIENT_CLASS_NAME;
    private String authClientPropertiesPath;
    private String version = DEFAULT_VERSION;
    private String namespace = DEFAULT_NAMESPACE;
    private boolean verifySSLCert = true;

    private Builder() {
    }

    public Builder setCdapHost(String cdapHost) {
      this.cdapHost = cdapHost;
      return this;
    }

    public Builder setCdapPort(int cdapPort) {
      this.cdapPort = cdapPort;
      return this;
    }

    public Builder setSSL(Boolean ssl) {
      this.ssl = ssl;
      return this;
    }

    public Builder setAuthClientClassName(String authClientClassName) {
      this.authClientClassName = authClientClassName;
      return this;
    }

    public Builder setAuthClientPropertiesPath(String authClientPropertiesPath) {
      this.authClientPropertiesPath = authClientPropertiesPath;
      return this;
    }

    public Builder setVersion(String version) {
      this.version = version;
      return this;
    }

    public Builder setNamespace(String namespace) {
      this.namespace = namespace;
      return this;
    }

    public Builder setVerifySSLCert(boolean verifySSLCert) {
      this.verifySSLCert = verifySSLCert;
      return this;
    }

    public StreamReader build() throws Exception {
      return new StreamReader(cdapHost, cdapPort, ssl, authClientClassName, authClientPropertiesPath,
                              version, namespace, verifySSLCert);
    }

  }

  private class StreamEvent {
    private Map<String, String> headers;
    private String body;
    long timestamp;

    public String getBody() {
      return body;
    }
  }
}
