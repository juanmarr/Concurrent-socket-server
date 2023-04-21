package multiSocketServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

//this client class accepts multiple clients simultaneously
public class Client {
    private static final List<Long> responseTimes = new ArrayList<>();
    private static CountDownLatch latch;

    public static void main(String[] args) throws InterruptedException {
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Scanner scanner = new Scanner(System.in);
        int input;

        System.out.println("Enter the number of clients you wish to create:");
        int numClients = scanner.nextInt();

        do {
            System.out.println("Enter one of the following commands \n"
                    + "1- Host Date/Time. \n"
                    + "2- Host uptime. \n"
                    + "3- Host memory.\n"
                    + "4- Host Netstat.\n"
                    + "5- Host current user.\n"
                    + "6- Host Running process.\n"
                    + "7- To Exit");
            input = scanner.nextInt();

            latch = new CountDownLatch(numClients);

            for (int i = 0; i < numClients; i++) {
		    System.out.println("creating and executing thread " + i);
                new Thread(new ClientRunnable(hostName, portNumber, input)).start();
            }
            latch.await();
            double averageResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            long totalTurnaroundTime =  responseTimes.stream().mapToLong(Long::longValue).sum();
            System.out.println("Average time of response: " + averageResponseTime + "ms");
            System.out.println("Total turn around time: " + totalTurnaroundTime + "ms");
        } while (input != 7);
    }

    static class ClientRunnable implements Runnable {
        private String hostName;
        private int portNumber;
        private int input;

        public ClientRunnable(String hostName, int portNumber, int input) {
            this.hostName = hostName;
            this.portNumber = portNumber;
            this.input = input;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("sending command " + " to server");
                out.println(input);
            System.out.println("command sent");

                long startTime = System.currentTimeMillis();
                //List<Long> responseTimes = new ArrayList<>();
            System.out.println("fetching response from the server");
                String response;
                StringWriter responseBuilder = null;
                while ((response = in.readLine()) != null) {
                    if ("END".equals(response)) {
                        break;
                    }
                    responseBuilder.append(response).append("\n");
                }
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;

                synchronized (responseTimes) {
                    responseTimes.add(responseTime);
                }

                System.out.println(responseBuilder.toString());
                //System.out.println("Server response: " + response);
                /*long totalTurnaroundTime = responseTimes.stream().mapToLong(Long::longValue).sum();
                double averageResponseTime = totalTurnaroundTime / (double) responseTimes.size();

                System.out.println("Average time of response: " + averageResponseTime + "ms");
                System.out.println("Total turn around time: " + totalTurnaroundTime + "ms");
*/
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                latch.countDown();
            }
        }
    }
}

