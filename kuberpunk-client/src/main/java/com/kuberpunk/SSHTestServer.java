package com.kuberpunk;


import org.apache.commons.io.FileUtils;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SSHTestServer {
    public int getSSHPort(){
        return sshd.getPort();
    }

    public String getVirtualFileSystemPath() {
        return virtualFileSystemPath;
    }

    public void setVirtualFileSystemPath(String virtualFileSystemPath) {
        this.virtualFileSystemPath = virtualFileSystemPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private static SshServer sshd;
    private String virtualFileSystemPath = "target/ssh_vfs/";

    private String username = "nifiuser";
    private String password = "nifipassword";

    public void SSHTestServer(){

    }

    public void startServer() throws IOException {
        sshd = SshServer.setUpDefaultServer();
        sshd.setHost("localhost");
        sshd.setPort(7878);

        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        //Accept all keys for authentication
        sshd.setPublickeyAuthenticator((s, publicKey, serverSession) -> true);

        //Allow username/password authentication using pre-defined credentials
        sshd.setPasswordAuthenticator((username, password, serverSession) ->  this.username.equals(username) && this.password.equals(password));

        //Setup Virtual File System (VFS)
        //Ensure VFS folder exists
        Path dir = Paths.get(getVirtualFileSystemPath());
        Files.createDirectories(dir);
        sshd.setFileSystemFactory(new VirtualFileSystemFactory(dir.toAbsolutePath()));

        sshd.start();
    }

    public void stopServer() throws IOException {
        if(sshd == null) return;
        sshd.stop(true);

        //Delete Virtual File System folder
        Path dir = Paths.get(getVirtualFileSystemPath());
        FileUtils.deleteDirectory(dir.toFile());
    }
}