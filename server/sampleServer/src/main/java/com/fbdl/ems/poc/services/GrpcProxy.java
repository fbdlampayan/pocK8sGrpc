/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fbdl.ems.poc.services;

import com.google.common.io.ByteStreams;
import io.grpc.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

//https://github.com/ejona86/grpc-java/blob/grpc-proxy/examples/src/main/java/io/grpc/examples/grpcproxy/GrpcProxy.java
//https://groups.google.com/forum/#!topic/grpc-io/DhqklrZ03fw
//grpc-proxy bookmark dir
//https://www.nexthink.com/blog/grpc-java-generic-gatewayreverse-proxy/
/** A grpc-level proxy. */
public class GrpcProxy<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
  private static final Logger logger = Logger.getLogger(GrpcProxy.class.getName());

  private final Channel channel;

  public GrpcProxy(Channel channel) {
    this.channel = channel;
  }

  @Override
  public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> serverCall, Metadata headers) {
    System.out.println("startcall " + headers.toString());
    ClientCall<ReqT, RespT> clientCall = channel.newCall(serverCall.getMethodDescriptor(), CallOptions.DEFAULT);
    CallProxy<ReqT, RespT> proxy = new CallProxy<ReqT, RespT>(serverCall, clientCall);
    clientCall.start(proxy.clientCallListener, headers);
    serverCall.request(1);
    clientCall.request(1);
    return proxy.serverCallListener;
  }

  private static class CallProxy<ReqT, RespT> {
    final RequestProxy serverCallListener;
    final ResponseProxy clientCallListener;

    public CallProxy(ServerCall<ReqT, RespT> serverCall, ClientCall<ReqT, RespT> clientCall) {
      serverCallListener = new RequestProxy(clientCall);
      clientCallListener = new ResponseProxy(serverCall);
    }

    private class RequestProxy extends ServerCall.Listener<ReqT> {
      private final ClientCall<ReqT, ?> clientCall;
      // Hold 'this' lock when accessing
      private boolean needToRequest;

      public RequestProxy(ClientCall<ReqT, ?> clientCall) {
        this.clientCall = clientCall;
      }

      @Override public void onCancel() {
        clientCall.cancel("Server cancelled", null);
      }

      @Override public void onHalfClose() {
        clientCall.halfClose();
      }

      @Override public void onMessage(ReqT message) {
        clientCall.sendMessage(message);
        synchronized (this) {
          if (clientCall.isReady()) {
            clientCallListener.serverCall.request(1);
          } else {
            needToRequest = true;
          }
        }
      }

      @Override public void onReady() {
        clientCallListener.onServerReady();
      }

      synchronized void onClientReady() {
        if (needToRequest) {
          clientCallListener.serverCall.request(1);
          needToRequest = false;
        }
      }
    }

    private class ResponseProxy extends ClientCall.Listener<RespT> {
      private final ServerCall<?, RespT> serverCall;
      // Hold 'this' lock when accessing
      private boolean needToRequest;

      public ResponseProxy(ServerCall<?, RespT> serverCall) {
        this.serverCall = serverCall;
      }

      @Override public void onClose(Status status, Metadata trailers) {
        serverCall.close(status, trailers);
      }

      @Override public void onHeaders(Metadata headers) {
        serverCall.sendHeaders(headers);
      }

      @Override public void onMessage(RespT message) {
        serverCall.sendMessage(message);
        synchronized (this) {
          if (serverCall.isReady()) {
            serverCallListener.clientCall.request(1);
          } else {
            needToRequest = true;
          }
        }
      }

      @Override public void onReady() {
        serverCallListener.onClientReady();
      }

      void onServerReady() {
        if (needToRequest) {
          serverCallListener.clientCall.request(1);
          needToRequest = false;
        }
      }
    }
  }

  private static class ByteMarshaller implements MethodDescriptor.Marshaller<byte[]> {
    @Override public byte[] parse(InputStream stream) {
      try {
        return ByteStreams.toByteArray(stream);
      } catch (IOException ex) {
        throw new RuntimeException();
      }
    }

    @Override public InputStream stream(byte[] value) {
      return new ByteArrayInputStream(value);
    }
  };

  public static class Registry extends HandlerRegistry {
    private final MethodDescriptor.Marshaller<byte[]> byteMarshaller = new ByteMarshaller();
    private final ServerCallHandler<byte[], byte[]> handler;

    public Registry(ServerCallHandler<byte[], byte[]> handler) {
        System.out.println("handler");
      this.handler = handler;
    }

    @Override
    public ServerMethodDefinition<?,?> lookupMethod(String methodName, String authority) {
        System.out.println("lookupMethod " + methodName);
      MethodDescriptor<byte[], byte[]> methodDescriptor
          = MethodDescriptor.newBuilder(byteMarshaller, byteMarshaller)
          .setFullMethodName(methodName)
          .setType(MethodDescriptor.MethodType.UNKNOWN)
          .build();
      return ServerMethodDefinition.create(methodDescriptor, handler);
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    String target = "daas-ems-daas-prototypes.espoo-apps.ilab.cloud:7777";
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
        .usePlaintext(true)
        .build();
    logger.info("Proxy will connect to " + target);
    GrpcProxy<byte[], byte[]> proxy = new GrpcProxy<byte[], byte[]>(channel);
    int port = 8981;
    Server server = ServerBuilder.forPort(port)
        .fallbackHandlerRegistry(new Registry(proxy))
        .build()
        .start();
    logger.info("Proxy started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        server.shutdown();
        try {
          server.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
        if (!server.isTerminated()) {
          server.shutdownNow();
        }
        channel.shutdownNow();
      }
    });
    server.awaitTermination();
    if (!channel.awaitTermination(1, TimeUnit.SECONDS)) {
      System.out.println("Channel didn't shut down promptly");
    }
  }
}
