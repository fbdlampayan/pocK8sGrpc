/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fbdl.ems.poc.services;

import fbdl.poc.grpc.PocServiceGrpc;
import fbdl.poc.grpc.ServiceRequest;
import fbdl.poc.grpc.ServiceResponse;
import io.grpc.stub.StreamObserver;

/**
 *
 * @author fbdl
 */
public class PocServices extends PocServiceGrpc.PocServiceImplBase {
    
    @Override
    public void simpleService(ServiceRequest request, StreamObserver<ServiceResponse> responseObserver) {
        System.out.println("simpleService triggered " + request.toString());
        ServiceResponse response = ServiceResponse.newBuilder().setResponseMessage("Message from Server " + System.getenv("HOSTNAME")).build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    @Override
    public void simpleServiceBreak(ServiceRequest request, StreamObserver<ServiceResponse> responseObserver) {
        System.out.println("simple service break triggered");
        try {
            Thread.sleep(5000);
        }
        catch (Exception ex) {
            System.out.println("exception in server " + ex.getMessage());
        }
        System.out.println("dying...");
        System.exit(1);
    }
    
    @Override
    public StreamObserver<ServiceRequest> bidiService(StreamObserver<ServiceResponse> responseObserver) {
        System.out.println("bidi triggered in server");
        
        return new StreamObserver<ServiceRequest>() { //returning the stream that the client on the other end can use.
            @Override
            public void onNext(ServiceRequest v) {
                //here we do what we can when we receive from the client.
                System.out.println("non oncompleted. received request from client: " + v.toString());
                
                //then we reply
                ServiceResponse response = ServiceResponse.newBuilder().setResponseMessage("bidiService message from Server " + System.getenv("HOSTNAME")).build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable thrwbl) {
                System.out.println("error recieved by server: " + thrwbl.getMessage());
                thrwbl.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("on completed triggered in server");
                responseObserver.onCompleted();
            }  
        };
    }
    
    @Override
    public StreamObserver<ServiceRequest> bidiServiceBreak(StreamObserver<ServiceResponse> responseObserver) {
        System.out.println("bidi break triggered in server");
        
        return new StreamObserver<ServiceRequest>() { //returning the stream that the client on the other end can use.
            @Override
            public void onNext(ServiceRequest v) {
                //here we do what we can when we receive from the client.
                System.out.println("received request from client now to die: " + v.toString());
                
                System.exit(1);
            }

            @Override
            public void onError(Throwable thrwbl) {
                System.out.println("error break recieved by server: " + thrwbl.getMessage());
                thrwbl.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("on completed break triggered in server");
                responseObserver.onCompleted();
            }  
        };
    }
}
