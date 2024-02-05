import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLOutput;
import java.util.ArrayList;

public class DNSserver {
        private static final int SERVER_PORT = 8053;
        private static final String GOOGLE_DNS_ADDRESS = "8.8.8.8";
        private static final int BUFFER_SIZE = 1024;
        private static final DNSCache cache = new DNSCache();

        public static void main(String[] args) {
            System.out.println("Listening on port: " + SERVER_PORT);

            try{
                DatagramSocket socket = new DatagramSocket(SERVER_PORT);
                DatagramSocket gSocket = new DatagramSocket();
                while (true) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    //Receive request from client
                    socket.receive(packet);

                    DNSMessage requestMessage = DNSMessage.decodeMessage(packet.getData());
                    DNSMessage responseMessage = new DNSMessage();

                    for (DNSQuestion question : requestMessage.getQuestions()) {
                        DNSRecord record = DNSCache.query(question);
                        if (record != null && !record.isExpired()) {
                            //If cached answer is found and not expired, use it
                             responseMessage = DNSMessage.buildResponse(requestMessage, new DNSRecord[]{record});
                            System.out.println("Using cache!");
                        } else {
                            //Else, forward the request to Google's DNS
                            InetAddress googleDNS = InetAddress.getByName(GOOGLE_DNS_ADDRESS);
                            System.out.println("Sent!");
                            DatagramPacket forwardPacket = new DatagramPacket(buffer, packet.getLength(), googleDNS, 53);
                            //Send request to Google
                            gSocket.send(forwardPacket);

                            byte[] responseBuffer = new byte[BUFFER_SIZE];
                            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                            //Receive response from Google
                            gSocket.receive(responsePacket);
                            System.out.println("Received!");

                            DNSMessage googleResponse = DNSMessage.decodeMessage(responsePacket.getData());
                            for (DNSRecord answer : googleResponse.getAnswer()) {
                                //Cache the new answer
                                DNSCache.insert(question, answer);
                            }
                            //Use Google's response as the response message
                            responseMessage = googleResponse;
                            System.out.println("Asking Google.");
                        }
                    }

                    System.out.println(responseMessage);
                    byte[] responseData = responseMessage.toBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, packet.getAddress(), packet.getPort());
                    socket.send(responsePacket);
                }
            } catch (IOException e) {
                System.err.println("Server exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
