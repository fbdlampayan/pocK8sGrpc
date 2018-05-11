/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fbdl.ems.poc.sampleserver;

import com.fbdl.ems.poc.services.PocServices;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author fbdl
 */
public class Main {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("server start netty with keepalive " + System.getenv("HOSTNAME"));
        Main server = new Main();
        server.start(args);
    }

    private void start(String[] args) throws IOException, InterruptedException {
        initializeServer();
    }

    private void initializeServer() throws IOException, InterruptedException {
        NettyServerBuilder.forPort(7777)
                //.useTransportSecurity(new File("C:\\VBXShared\\pocK8sGrpc\\server\\sampleServer\\certs\\servercert.pem"), new File("C:\\VBXShared\\pocK8sGrpc\\server\\sampleServer\\certs\\serverkey.pem"))//(new File("/home/jboss/servercert.pem"), new File("/home/jboss/serverkey.pem"))
                //.keepAliveTime(30, TimeUnit.SECONDS)
                .addService(new PocServices())
                .executor(Executors.newFixedThreadPool(32))
                .build()
                .start()
                .awaitTermination();
    }
    
}
