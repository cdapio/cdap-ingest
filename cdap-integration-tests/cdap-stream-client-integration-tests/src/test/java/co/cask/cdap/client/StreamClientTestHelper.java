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

package co.cask.cdap.client;

import co.cask.cdap.client.rest.RestClient;
import co.cask.cdap.client.rest.RestClientConnectionConfig;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class StreamClientTestHelper {

  private static final Gson GSON = new Gson();
  private static final Type JSON_OBJECTS_TYPE = new TypeToken<List<JsonObject>>() { }.getType();

  private RestClient restClient;
  private String version;

  public StreamClientTestHelper(RestClientConnectionConfig connectionConfig,
                                Registry<ConnectionSocketFactory> connectionRegistry) {
    PoolingHttpClientConnectionManager clientConnectionManager;
    if (connectionRegistry != null) {
      clientConnectionManager = new PoolingHttpClientConnectionManager(connectionRegistry);
    } else {
      clientConnectionManager = new PoolingHttpClientConnectionManager();
    }
    restClient = new RestClient(connectionConfig, clientConnectionManager);
    version = connectionConfig.getVersion();
  }

  public JsonObject getStream(String name) throws IOException {
    HttpGet httpGet = new HttpGet(restClient.getBaseURL().resolve(String.format("/%s/streams/%s", version, name)));
    CloseableHttpResponse httpResponse = restClient.execute(httpGet);
    JsonObject stream;
    try {
      stream = GSON.fromJson(EntityUtils.toString(httpResponse.getEntity()), JsonObject.class);
    } finally {
      httpResponse.close();
    }
    return stream;
  }

  public List<JsonObject> getStreamEvents(String name) throws IOException {
    HttpGet httpGet =
      new HttpGet(restClient.getBaseURL().resolve(String.format("/%s/streams/%s/events", version, name)));
    CloseableHttpResponse httpResponse = restClient.execute(httpGet);
    List<JsonObject> streamEvents = null;
    try {
      if (httpResponse.getEntity() != null) {
        streamEvents = GSON.fromJson(EntityUtils.toString(httpResponse.getEntity()), JSON_OBJECTS_TYPE);
      }
    } finally {
      httpResponse.close();
    }
    return streamEvents != null ? streamEvents : Lists.<JsonObject>newArrayList();
  }

  public void close() throws IOException {
    restClient.close();
  }
}
