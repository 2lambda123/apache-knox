/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.knox.gateway.websockets;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * A basic test that attempts to proxy websocket connections through Knox
 * gateway.
 * <p>
 * The way the test is set up is as follows: <br>
 * <ul>
 * <li>A Mock Websocket server is setup which simply echos the responses sent by
 * client.
 * <li>Knox Gateway is set up with websocket handler
 * {@link GatewayWebsocketHandler} that can proxy the requests.
 * <li>Appropriate Topology and service definition files are set up with the
 * address of the Websocket server.
 * <li>A mock client is setup to connect to gateway.
 * </ul>
 *
 * The test is to confirm whether the message is sent all the way to the backend
 * Websocket server through Knox and back.
 *
 * @since 0.10
 */
public class WebsocketEchoTest extends WebsocketEchoTestBase {

  public WebsocketEchoTest() {
    super();
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    WebsocketEchoTestBase.setUpBeforeClass("ws");
  }

  @AfterClass
  public static void tearDownAfterClass() {
    WebsocketEchoTestBase.tearDownAfterClass();
  }

  /*
   * Test direct connection to websocket server without gateway
   */
  @Test
  public void testDirectEcho() throws Exception {

    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    WebsocketClient client = new WebsocketClient();

    Session session = container.connectToServer(client, backendServerUri);

    session.getBasicRemote().sendText("Echo");
    client.messageQueue.awaitMessages(1, 1000, TimeUnit.MILLISECONDS);
  }

  /*
   * Test websocket proxying through gateway.
   */
  @Test
  public void testGatewayEcho() throws Exception {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    WebsocketClient client = new WebsocketClient();
    Session session = container.connectToServer(client,
        new URI(serverUri.toString() + "gateway/websocket/ws"));

    session.getBasicRemote().sendText("Echo");
    client.messageQueue.awaitMessages(1, 1000, TimeUnit.MILLISECONDS);

    assertThat(client.messageQueue.get(0), is("Echo"));
  }

  /*
   * Test websocket rewrite rules proxying through gateway.
   */
  @Test
  public void testGatewayRewriteEcho() throws Exception {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();

    WebsocketClient client = new WebsocketClient();
    Session session = container.connectToServer(client,
            new URI(serverUri.toString() + "gateway/websocket/123foo456bar/channels"));

    session.getBasicRemote().sendText("Echo");
    client.messageQueue.awaitMessages(1, 1000, TimeUnit.MILLISECONDS);

    assertThat(client.messageQueue.get(0), is("Echo"));
  }
}
