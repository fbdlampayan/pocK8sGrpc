/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fbdl.ems.poc.sampleclient;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import javax.net.ssl.SSLException;

/**
 *
 * @author fbdl
 */
public class Main {
    
    @Parameter(names ={"-h", "--help"}, description = "displays options available", help = true)
    private boolean help = false;
    
    @Parameter(names ={"-s", "--service"}, description = "target service to send request.")
    private String serviceName = "daas-ems-daas-prototypes.espoo-apps.ilab.cloud";
    
    @Parameter(names ={"-p", "--port"}, description = "target port of the service to send request.")
    private int servicePort = 7777;
    
    @Parameter(names ={"-e", "--execute"}, description = "Mandatory option. GRPC service to execute" , required = true)
    private String execute;
    
    private JCommander jCommander;
    
    public JCommander getJCommander() {
        return jCommander;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public int getServicePort() {
        return servicePort;
    }
    
    public String getExecute() {
        return execute;
    }
    
    public static void main(String[] args) throws SSLException, InterruptedException {
        Main client = new Main();
        client.start(args);
    }

    private void start(String[] args) throws SSLException, InterruptedException {
        jCommander = JCommander.newBuilder().addObject(this).build();
        jCommander.parse(args);
        jCommander.setProgramName("SampleClient");
        
        if(this.help) {
            jCommander.usage();
            return;
        }
        
        CommandProcessor processor = new CommandProcessor(this);
        processor.execute();
    }
    
}
