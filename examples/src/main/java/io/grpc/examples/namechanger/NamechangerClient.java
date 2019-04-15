/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.examples.namechanger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class NamechangerClient {
  private static final Logger logger = Logger.getLogger(NamechangerClient.class.getName());

  private final ManagedChannel channel;
  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  /** Construct client connecting to HelloWorld server at {@code host:port}. */
  public NamechangerClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port)
        // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
        // needing certificates.
        .usePlaintext()
        .build());
  }

  /** Construct client for accessing HelloWorld server using the existing channel. */
  NamechangerClient(ManagedChannel channel) {
    this.channel = channel;
    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public void greet(String name, String ch) {

    logger.info("Will try to reverse " + name + " ...");
    InputRequest request = InputRequest.newBuilder().setName(name).setChara(ch).build();
    ResultReply response;

    try {
      response = blockingStub.reverse(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Greeting: " + response.getMessage());

    try {
      response = blockingStub.charReplace(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Greeting: " + response.getMessage());

  }

  /**
   * Greet server. If provided, the first element of {@code args} is the name to use in the
   * greeting.
   */
  public static void main(String[] args) throws Exception {
    NamechangerClient client = new NamechangerClient("localhost", 50051);
    try {
      /* Access a service running on the local machine on port 50051 */
      String user = "Machichi";
      String ch = "*";
      client.greet(user, ch);
    } finally {
      client.shutdown();
    }
  }
}
