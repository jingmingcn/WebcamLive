/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.github.udplive.client;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;
import com.github.udplive.model.ClientInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class App {

    static Map<String, ClientInfo> subscribes = null;

    public static void main(String[] args) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            System.out.println("Client is started! Port: " + clientSocket.getLocalPort());

            InetAddress serverAddress = InetAddress.getByName("139.129.221.192");
            int serverPort = 5666;

            DatagramPacket sendPacket = null;

            String cmd = "LIVE;"+InetAddress.getLocalHost().getHostAddress()+":"+clientSocket.getLocalPort();
            byte[] sendData = cmd.getBytes();
            sendPacket = new DatagramPacket(sendData,sendData.length,serverAddress,serverPort);
            clientSocket.send(sendPacket);

            Thread receiveThread = new Thread(){
                public void run() {
                    System.out.println("Receive Thread Running!");
                    while(true){
                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        try {
                            clientSocket.receive(receivePacket);
                            ByteArrayInputStream bais = new ByteArrayInputStream(receiveData);
                            ObjectInput oi = new ObjectInputStream(bais);
                            subscribes = (Map<String,ClientInfo>)oi.readObject();
                            //subscribes = new String(receiveData).trim().split(";");
                            System.out.println(new String(receiveData).trim());
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            Thread sendThread = new Thread(){
                public void run(){
                    Webcam webcam = Webcam.getDefault();
                    webcam.open();
                    System.out.println("Send Thread Running!");
                    while(true){
                        if(subscribes!=null&&subscribes.size()>0){
                        	
//                        	BufferedImage bi = webcam.getImage().getSubimage(0, 0, 20, 20);
//                        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        	try {
//								ImageIO.write(bi, ImageUtils.FORMAT_JPG, baos);
//							} catch (IOException e1) {
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}
//
//                        	byte[] sendData = baos.toByteArray();
                            //byte[] sendData = "Hello".getBytes();
                            byte[] sendData = WebcamUtils.getImageBytes(webcam,ImageUtils.FORMAT_JPG);
                            System.out.println("Length of sendData is: "+sendData.length);

                            subscribes.forEach((k,v)->{
                                System.out.println("Send Data to "+v.ip.getHostAddress()+":"+v.port);
                                try {
                                    DatagramPacket p = new DatagramPacket(sendData,sendData.length,v.ip,v.port);
                                    clientSocket.send(p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });

                        }
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            receiveThread.start();
            sendThread.start();


        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
