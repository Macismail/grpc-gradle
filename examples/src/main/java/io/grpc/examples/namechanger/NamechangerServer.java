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

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class NamechangerServer {
  private static final Logger logger = Logger.getLogger(NamechangerServer.class.getName());

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    server = ServerBuilder.forPort(port)
        .addService(new GreeterImpl())
        .build()
        .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        NamechangerServer.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final NamechangerServer server = new NamechangerServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    @Override
    public void reverse(InputRequest req, StreamObserver<ResultReply> responseObserver) {
      String name = req.getName();
      String reversed="";
      for(int i = name.length() - 1; i >= 0; i--)
        {
            reversed = reversed + name.charAt(i);
        }
      ResultReply reply = ResultReply.newBuilder().setMessage("the name reversed is: " + reversed).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }

      @Override
      public void charReplace(InputRequest req, StreamObserver<ResultReply> responseObserver) {
        String name = req.getName();
        String rep = name.replace('i',req.getChara().charAt(0));
        ResultReply reply = ResultReply.newBuilder().setMessage("the character changed to *: " + rep).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      }
  }
}
