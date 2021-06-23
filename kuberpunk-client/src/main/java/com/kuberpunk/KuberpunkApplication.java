package com.kuberpunk;

import com.kuberpunk.input.ArgumentParser;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
@Import(SpringConfig.class)
public class KuberpunkApplication implements CommandLineRunner {

    @Autowired
    private ApplicationContext context;

    public static void main(String... args) {
        SpringApplication.run(KuberpunkApplication.class, args);
    }

    @SneakyThrows
    @Override
    public void run(String... args) {
        ArgumentParser argumentParser = context.getBean(ArgumentParser.class);
        argumentParser.parse(args);
        System.out.println('\n');
/*        String[] args2 = new String[] {"sudo","-S", "sshuttle", "-r", "python-sshd", "-e",
                "/Users/a18851548/learning/Learning_MIPT_NEW/diploma2021/tryFeb/kuberpunk/kuberpunk-client/kuttle",
                "employee"};


      [
 */
       /* SSHTestServer sshTestServer = new SSHTestServer();
        try {
            sshTestServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @GetMapping("/")
    public String redirectToCloud(){
        return "redirected";
    }

    @GetMapping("/actuator/health")
    public String actuator(){
        return "redirected";
    }



}
