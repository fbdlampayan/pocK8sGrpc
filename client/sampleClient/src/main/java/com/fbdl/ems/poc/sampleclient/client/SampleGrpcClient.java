/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fbdl.ems.poc.sampleclient.client;

import fbdl.poc.grpc.PocServiceGrpc;
import fbdl.poc.grpc.ServiceRequest;
import fbdl.poc.grpc.ServiceResponse;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLException;

/**
 *
 * @author fbdl
 */
public class SampleGrpcClient {
    
    private PocServiceGrpc.PocServiceBlockingStub serverStub; //why not try async din kay non stream? pwede but usable within the same module alone.
    private PocServiceGrpc.PocServiceStub serverAsyncStub;
    private ManagedChannel channel;

    public SampleGrpcClient(String serviceName, int servicePort) throws SSLException {
        channel = NettyChannelBuilder.forAddress(serviceName, servicePort)
                                    //.sslContext(GrpcSslContexts.forClient().trustManager(new File("C:\\VBXShared\\pocK8sGrpc\\client\\sampleClient\\certs\\servercert.pem")).build())
                                     .usePlaintext(true)
                                     .build();
        serverStub = PocServiceGrpc.newBlockingStub(channel);
        serverAsyncStub = PocServiceGrpc.newStub(channel);
    }
    
    public void simpleService() throws InterruptedException {
        System.out.println("triggering simpleService");
        
        ServiceRequest request = ServiceRequest.newBuilder().setRequestMessage("message from client").setAnotherField("Another Field").setSecondField("Second").build();
        ServiceResponse response = null;
        
        try {
            response = serverStub.simpleService(request);
        }
        catch (StatusRuntimeException ex) {
            System.out.println("error : " + ex.getMessage());
        }
        finally {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS); //interruptedexception
        }
        
        if (response != null) {
            System.out.println("response " + response.toString());
        }
        
        System.out.println("simpeService end");
    }
    
    public void simpleServiceBreak() throws InterruptedException {
        System.out.println("triggering simpleServiceBreak");
        ServiceRequest request = ServiceRequest.newBuilder().setRequestMessage("message from client with break").build();
        ServiceResponse response = null;
        
        try {
            response = serverStub.simpleServiceBreak(request);
        }
        catch (StatusRuntimeException ex) {
            System.out.println("simpleServiceBreak error : " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
        
        if (response != null) {
            System.out.println("response " + response.toString());
        }
        
        System.out.println("simpeService end");
    }
    
    public void bidiService() {
        System.out.println("triggering bidiService");
        
        //we will recieve a requestObserver from calling the grpc service and feed it with responseObserver.
        StreamObserver<ServiceRequest> requestObserver = serverAsyncStub.bidiService(new StreamObserver<ServiceResponse>() {
            @Override
            public void onNext(ServiceResponse v) {
                //what we do when we receive a response here
                System.out.println("bidi response: " + v.getResponseMessage());
            }

            @Override
            public void onError(Throwable thrwbl) {
                System.out.println("error recieved in client " + thrwbl.getMessage());
                thrwbl.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("oncompleted received in client");
            }
        });
        
        System.out.println("sending twice");
        ServiceRequest request = ServiceRequest.newBuilder().setRequestMessage("request from client").build();
        requestObserver.onNext(request);
        requestObserver.onCompleted();
        System.out.println("sent!");
    }
    
    public void simpleServiceAsync() throws InterruptedException {
        
        System.out.println("triggering simpleServiceAsync");
        ServiceRequest request = ServiceRequest.newBuilder().setRequestMessage("message from client").build();
        
        serverAsyncStub.simpleService(request, new StreamObserver<ServiceResponse>() {
            @Override
            public void onNext(ServiceResponse v) {
                System.out.println("simpleServiceAsync onnext " + v.getResponseMessage());
            }

            @Override
            public void onError(Throwable thrwbl) {
                System.out.println("simpleServiceAsync onerror " + thrwbl.getMessage());
                thrwbl.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("simpleServiceAsync oncompleted");
            }
        });
        
        
        System.out.println("simpeServiceAsync end");
    }
    
    public void bidiServiceComplex() {
        System.out.println("triggering bidiService complex");
        
        //we will recieve a requestObserver from calling the grpc service and feed it with responseObserver.
        StreamObserver<ServiceRequest> requestObserver = serverAsyncStub.bidiService(new StreamObserver<ServiceResponse>() {
            @Override
            public void onNext(ServiceResponse v) {
                //what we do when we receive a response here
                System.out.println("bidi complex response: " + v.getResponseMessage());
                System.out.println("now triggering simple service");
                try {
                    simpleService();
                } catch (InterruptedException ex) {
                    System.out.println("interrupted ex " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable thrwbl) {
                System.out.println("error complex recieved in client " + thrwbl.getMessage());
                thrwbl.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("oncompleted complex received in client");
            }
        });
        
        System.out.println("sending complex");
        ServiceRequest request = ServiceRequest.newBuilder().setRequestMessage("request from client").build();
        requestObserver.onNext(request);
        requestObserver.onCompleted();
        System.out.println("sent! complex");
    }
    
    public void bidiServiceBreak() {
        System.out.println("triggering bidiService break");
        
        //we will recieve a requestObserver from calling the grpc service and feed it with responseObserver.
        StreamObserver<ServiceRequest> requestObserver = serverAsyncStub.bidiServiceBreak(new StreamObserver<ServiceResponse>() {
            @Override
            public void onNext(ServiceResponse v) {
                //what we do when we receive a response here
                System.out.println("bidi break response: " + v.getResponseMessage());
            }

            @Override
            public void onError(Throwable thrwbl) {
                System.out.println("error break recieved in client " + thrwbl.getMessage());
                thrwbl.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("oncompleted break received in client");
            }
        });
        
        System.out.println("sending bidi break");
        ServiceRequest request = ServiceRequest.newBuilder().setRequestMessage("request from client").build();
        requestObserver.onNext(request);
        requestObserver.onCompleted();
        System.out.println("sent! bidi break");
    }
}
