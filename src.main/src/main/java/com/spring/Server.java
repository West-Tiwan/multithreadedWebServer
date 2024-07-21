package com.spring;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;
import java.nio.file.*;

public class Server {
    public Consumer<Socket> consumer(){
        return  (clientSocket)->{
            try {
                System.out.println("New thread created");
                PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(),true);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                List<String> res = fileReader("index.html");
                if (res != null) {
                    // Send HTTP response headers
                    printWriter.println("HTTP/1.1 200 OK");
                    printWriter.println("Content-Type: text/html");
                    printWriter.println("Connection: close");
                    printWriter.println("");

                    for (String s : res) {
                        printWriter.println(s);
                    }
                    System.out.println("File served");
                } else {
                    printWriter.println("HTTP/1.1 404 Not Found");
                    printWriter.println("");
                    printWriter.println("<html><body><h1>404 Not Found</h1></body></html>");
                    printWriter.println("Connection: close");
                }
                printWriter.flush();
            } catch (Exception e) {
                System.out.println(e.toString());
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Connection ended");
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            }
        };
    }

    public static List<String> fileReader(String path) {
        try {
            return Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    public static void main(String[] args) {
        Server server = new Server();

        try {
            int port = 3000;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port: "+port);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection accepted");
                Thread thread = new Thread(()->server.consumer().accept(socket));
                thread.start();
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}