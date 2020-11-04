package com.sbt.kuberpunk.hostextraction;

import com.sbt.kuberpunk.KuberpunkApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class RedirectAddressMiner implements IRedirectAddressMiner {

    private static final Logger logger = LoggerFactory.getLogger(KuberpunkApplication.class);
    private static void log(String action, Object obj) {
        logger.info("{}: {}", action, obj);
    }

    @Override
    public String getRedirectAddress() {
        String connectionAddress = "";
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("google.com", 80));
            connectionAddress = socket.getLocalAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connectionAddress;
    }

/*      try {
        Enumeration<NetworkInterface> ne =  NetworkInterface.getNetworkInterfaces();
        while (ne.hasMoreElements()) {
            NetworkInterface networkInterface =  ne.nextElement();
            System.out.println("Enumeration " + networkInterface);
            System.out.println("getDisplayName " + networkInterface.getDisplayName());
            System.out.println("Name " + networkInterface.getName());
            System.out.println("getHardwareAddress " + networkInterface.getHardwareAddress());
            System.out.println("getInetAddresses " + networkInterface.getInetAddresses());
            System.out.println("getInterfaceAddresses " + networkInterface.getInterfaceAddresses());
            System.out.println("getInterfaceAddresses " + networkInterface.getMTU());
        }
    } catch (
    SocketException e) {
        e.printStackTrace();
    }


        try {
        System.out.println("Your Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
    } catch (UnknownHostException e) {
        e.printStackTrace();
    }
    Enumeration<NetworkInterface> n = null;
        try {
        n = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
        e.printStackTrace();
    }
        for (; n.hasMoreElements(); ) {
        NetworkInterface e = n.nextElement();

        Enumeration<InetAddress> a = e.getInetAddresses();
        for (; a.hasMoreElements(); ) {
            InetAddress addr = a.nextElement();
            System.out.println("  " + addr.getHostAddress());
        }
    }

                String systemipaddress = "";
            try {
                URL url_name = new URL("http://bot.whatismyipaddress.com");
                BufferedReader sc =
                        new BufferedReader(new InputStreamReader(url_name.openStream()));
                // reads system IPAddress
                systemipaddress = sc.readLine().trim();
            } catch (Exception e) {
                systemipaddress = "Cannot Execute Properly";
            }
            System.out.println("Public IP Address: " + systemipaddress + "\n");


*/
}