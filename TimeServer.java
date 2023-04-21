package multiSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimeServer {

    public static void main(String[] args) {
        if (args.length < 1) return;

        int portNumber = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

            System.out.println("Multi-threaded server is running on " + portNumber);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Accepted connection from client " + socket.getInetAddress());
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ClientHandler implements Runnable {
        private Socket socket;
        private static List<String> connectedClients = new CopyOnWriteArrayList<>();
        public ClientHandler(Socket socket) {
            this.socket = socket;
            connectedClients.add(socket.getInetAddress().toString());
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream output = socket.getOutputStream();
                PrintWriter out = new PrintWriter(output, true);

                String input;
                while ((input = in.readLine()) != null) {
                    switch (input) {
                        case "1":
                            out.println(new Date());
                            break;
                        case "2":
                            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
                            long uptimeInMillis = runtimeBean.getUptime();
                            String uptimeString = "Uptime: " + uptimeInMillis + " milliseconds";
                            out.println(uptimeString);
                            break;
                        case "3":
                            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
                            MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
                            long usedHeapMemory = heapMemoryUsage.getUsed();
                            out.println("Used heap memory: " + usedHeapMemory + " bytes");
                            break;
                        case "4":
                            try {
                                Process process = Runtime.getRuntime().exec("netstat");
                                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    out.println(line);
                                    System.out.println(line);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "5":
                            out.println("Current Users - list of users currently connected to the server:");
                            for (String client : connectedClients) {
                                out.println(client);
                            }
                            break;
                        case "6":
                            //print all the Running Processes - list of programs currently running on the server
                            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
                            java.util.List<String> arguments = runtimeMXBean.getInputArguments();
                            for (String arg : arguments) {
                                System.out.println(arg);
                                out.println(arg);
                            }
                            break;
                        case "7":
                            out.println("END");
                            socket.close();
                            return;
                        default:
                            out.println("Invalid input");
                            out.println("END");
                            break;
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
