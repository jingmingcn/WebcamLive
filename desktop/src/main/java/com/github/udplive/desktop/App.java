/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.github.udplive.desktop;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Enumeration;

public class App extends JFrame {

    static boolean updated = false;
    static byte[] imageData = new byte[1024*8];

    public App(){
        getContentPane().setLayout(new BorderLayout());
        JPanel panel = createPanel();
        getContentPane().add(BorderLayout.CENTER, panel);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    public static JPanel createPanel(){
        JPanel panel = new JPanel(){
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    if(updated){
                        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(imageData));
                        g.drawImage(bi, 0, 0, this);
                        repaint();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        };
        return panel;
    }

    public void run(){
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName("139.129.221.192");
            int serverPort = 5666;

            Thread sendThread = new Thread(){
                public void run(){
                    try {
                        while(true){
                            String cmd = "WATCH;"+getHost4Address()+":"+socket.getLocalPort();
                            byte[] sendData = cmd.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,serverAddress,serverPort);
                            socket.send(sendPacket);
                            sleep(1000);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            Thread receiveThread = new Thread(){
              public void run(){
                  while(true){
                      byte[] receiveData = new byte[1024*64];
                      DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
                      try {
                          System.out.println("Waiting for Receiving!");
                          socket.receive(receivePacket);
                      } catch (IOException e) {
                          e.printStackTrace();
                      }

                      if(!receivePacket.getAddress().getHostAddress().equalsIgnoreCase("139.129.221.192")){
                          updated = true;
                          imageData = receiveData;
                          repaint();
                      }
                  }
              }
            };

            sendThread.start();
            receiveThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    /**
     * Returns this host's non-loopback IPv4 addresses.
     *
     * @return
     * @throws SocketException
     */
    private static List<Inet4Address> getInet4Addresses() throws SocketException {
        List<Inet4Address> ret = new ArrayList<Inet4Address>();

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                    ret.add((Inet4Address)inetAddress);
                }
            }
        }

        return ret;
    }

    /**
     * Returns this host's first non-loopback IPv4 address string in textual
     * representation.
     *
     * @return
     * @throws SocketException
     */
    private static String getHost4Address() throws SocketException {
        List<Inet4Address> inet4 = getInet4Addresses();
        return !inet4.isEmpty()
                ? inet4.get(0).getHostAddress()
                : null;
    }

    public static void main(String[] args) {

        App app = new App();
        app.setSize(800,600);
        app.setVisible(true);
        app.run();

    }
}
