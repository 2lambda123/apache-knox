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

import java.io.IOException;

import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.Session;

/**
 * A simple Echo socket
 */
public class ServerInitiatingMessageSocket extends WebSocketAdapter {

  @Override
  public void onWebSocketError(Throwable cause) {
    throw new RuntimeException(cause);
  }

  @Override
  public void onWebSocketConnect(Session sess) {
    super.onWebSocketConnect(sess);

    try {
      RemoteEndpoint remote = getRemote();
      remote.sendString("echo", null);
      if (remote.getBatchMode() == BatchMode.ON) {
        remote.flush();
      }
    } catch (IOException x) {
      throw new RuntimeIOException(x);
    }
  }
}
