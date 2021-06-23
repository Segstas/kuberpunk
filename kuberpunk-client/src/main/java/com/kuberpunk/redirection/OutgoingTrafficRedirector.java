package com.kuberpunk.redirection;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class OutgoingTrafficRedirector {

    @SneakyThrows
    public void enableRedirectRule(){
        String cmd1 = "sudo sysctl -w net.inet.ip.forwarding=1";
        String cmd2 = "sudo pfctl -vnf /etc/pf.conf";
        String cmd3 = "sudo pfctl -e -f /etc/pf.conf";
        Runtime run = Runtime.getRuntime();
        String cmd = cmd1 + '\n' + cmd2 + '\n' + cmd3 + '\n';
        Process pr = run.exec(cmd);
        pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";
        while ((line=buf.readLine())!=null) {
            System.out.println(line);
        }
    }
}
