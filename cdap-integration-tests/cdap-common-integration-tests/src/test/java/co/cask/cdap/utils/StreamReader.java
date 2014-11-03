/*
 * Copyright © 2014 Cask Data, Inc.
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

import co.cask.cdap.security.authentication.client.AccessToken;
import co.cask.cdap.security.authentication.client.AuthenticationClient;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for getting delivered events
 */
public class StreamReader {

  private final String cdapHost;
  private final String cdapPort;
  private final Boolean ssl;
  private final String streamName;
  private final Properties properties;
  private final String defaultAuthClient;
  private final String authClientPropertiesPath;

  private StreamReader(Properties properties, String cdapHost, String cdapPort, Boolean ssl, String streamName,
                       String defaultAuthClient, String authClientPropertiesPath) {
    this.properties = properties;
    this.cdapHost = cdapHost;
    this.cdapPort = cdapPort;
    this.ssl = ssl;
    this.streamName = streamName;
    this.defaultAuthClient = defaultAuthClient;
    this.authClientPropertiesPath = authClientPropertiesPath;
  }

  public String getCdapHost() {
    return cdapHost;
  }

  public String getCdapPort() {
    return cdapPort;
  }

  public Boolean getSsl() {
    return ssl;
  }

  public String getStreamName() {
    return streamName;
  }

  public Properties getProperties() {
    return properties;
  }

  public String getDefaultAuthClient() {
    return defaultAuthClient;
  }

  public String getAuthClientPropertiesPath() {
    return authClientPropertiesPath;
  }

  /**
   * Retrieves events from specified Stream in Cdap Server that were delivered in time
   *  between {@code startTime} and {@code endTime}
   *
   * @param startTime the start time
   * @param endTime the end time
   * @return delivered events
   * @throws Exception
   */
  public List<String> getDeliveredEvents(long startTime, long endTime) throws Exception {
    String eventsStr = readFromStream(startTime, endTime);
    Type listType = new TypeToken<List<StreamEvent>>() { }.getType();
    List<StreamEvent> eventList = new Gson().fromJson(eventsStr, listType);
    List<String> events = new ArrayList<String>(eventList.size());
    for (StreamEvent event : eventList) {
      events.add(event.getBody());
    }
    return events;
  }

  private String readFromStream(long startTime, long endTime) throws Exception {
    URI baseUrl = URI.create(String.format("%s://%s:%s", ssl ? "https" : "http",
                                           cdapHost, cdapPort));
    HttpGet getRequest = new HttpGet(baseUrl.resolve(String.format("/v2/streams/%s/events?start=%s&end=%s",
                                                                   streamName, startTime, endTime)));
    if (authClientPropertiesPath != null) {
      AuthenticationClient authClient = configureAuthClient();
      AccessToken token = authClient.getAccessToken();
      getRequest.setHeader("Authorization", token.getTokenType() + " " + token.getValue());
    }
    CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(
      new BasicHttpClientConnectionManager()).build();
    HttpResponse response = httpClient.execute(getRequest);
    String result = EntityUtils.toString(response.getEntity());
    Closeables.close(httpClient, true);
    return result;
  }

  /**
   * Configure {@link AuthenticationClient} for specified Cdap Server
   *
   * @return configured {@link AuthenticationClient}
   * @throws Exception
   */
  public AuthenticationClient configureAuthClient() throws Exception {
    String authClientClassName = getProperty("pipes.pipe1.sink.auth_client", defaultAuthClient);
    AuthenticationClient authClient = (AuthenticationClient) Class.forName(authClientClassName).newInstance();
    InputStream inStream = null;
    try {
      URL resource = StreamReader.class.getClassLoader().getResource(authClientPropertiesPath);
      inStream = new FileInputStream(new File(resource.toURI()));
      Properties properties = new Properties();
      properties.load(inStream);
      authClient.configure(properties);
      authClient.setConnectionInfo(cdapHost, Integer.parseInt(cdapPort), ssl);
    } finally {
      Closeables.closeQuietly(inStream);
    }
    return authClient;
  }

  private String getProperty(String key, String defaultValue) {
    String value = properties.getProperty(key);
    return value != null && !value.equals("") ? value : defaultValue;
  }

  public static Builder builder() {
    return new Builder();
  }

  private class StreamEvent {
    private Map<String, String> headers;
    private String body;
    long timestamp;

    public String getBody() {
      return body;
    }
  }

  /**
   * Builder for StreamReader
   */
  public static class Builder {

    private String cdapHost;
    private String cdapPort;
    private Boolean ssl;
    private String streamName;
    private Properties properties;
    private String defaultAuthClient;
    private String authClientPropertiesPath;

    private Builder() {
    }

    public Builder setProperties(Properties properties) {
      this.properties = properties;
      return this;
    }

    public Builder setCdapHost(String cdapHost) {
      this.cdapHost = cdapHost;
      return this;
    }

    public Builder setCdapPort(String cdapPort) {
      this.cdapPort = cdapPort;
      return this;
    }

    public Builder setSSL(Boolean ssl) {
      this.ssl = ssl;
      return this;
    }

    public Builder setStreamName(String streamName) {
      this.streamName = streamName;
      return this;
    }

    public Builder setDefaultAuthClient(String defaultAuthClient) {
      this.defaultAuthClient = defaultAuthClient;
      return this;
    }

    public Builder setAuthClientPropertiesPath(String authClientPropertiesPath) {
      this.authClientPropertiesPath = authClientPropertiesPath;
      return this;
    }

    public StreamReader build() {
      return new StreamReader(properties, cdapHost, cdapPort, ssl, streamName, defaultAuthClient,
                              authClientPropertiesPath);
    }
  }
}
