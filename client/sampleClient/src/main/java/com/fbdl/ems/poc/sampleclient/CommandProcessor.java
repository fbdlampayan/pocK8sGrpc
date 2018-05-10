/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fbdl.ems.poc.sampleclient;

import com.fbdl.ems.poc.sampleclient.client.SampleGrpcClient;
import javax.net.ssl.SSLException;

/**
 *
 * @author fbdl
 */
public class CommandProcessor {
    
    Main client;
    SampleGrpcClient grpcClient;
    
    public CommandProcessor(Main client) throws SSLException {
        this.client = client;
        grpcClient = new SampleGrpcClient(client.getServiceName(), client.getServicePort());
    }
    
    public void execute() throws InterruptedException {
        if (client.getExecute().equalsIgnoreCase("simpleService")) {
            grpcClient.simpleService();
        }
        else if (client.getExecute().equalsIgnoreCase("simpleServiceBreak")) {
            grpcClient.simpleServiceBreak();
        }
        else if (client.getExecute().equalsIgnoreCase("simpleServiceAsync")) { //result still ends up here in the same module
            grpcClient.simpleServiceAsync();
            Thread.sleep(5000);
        }
        else if (client.getExecute().equalsIgnoreCase("bidiService")) {
            grpcClient.bidiService();
            Thread.sleep(5000);//this client is not alive forever so give time here for bidi
        }
        else if (client.getExecute().equalsIgnoreCase("bidiServiceComplex")) {
            grpcClient.bidiServiceComplex();
            Thread.sleep(10000);//this client is not alive forever so give time here for bidi
        }
        else if (client.getExecute().equalsIgnoreCase("bidiServiceBreak")) {
            grpcClient.bidiServiceBreak();
            Thread.sleep(10000);//this client is not alive forever so give time here for bidi
        }
        else {
            System.out.println("invalid service request: " + client.getExecute());
        }
        
        
    }
    
}
