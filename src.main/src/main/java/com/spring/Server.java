package com.spring;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;

public class Server {
    public Consumer<Socket> consumer(){
        return  (clientSocket)->{
            try {
                System.out.println("New thread created");
                PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(),true);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String line;
                StringBuilder request = new StringBuilder();

                while (!(line = bufferedReader.readLine()).isBlank()) {
                    request.append(line).append("\n");
                }
                String URL = "";
                String[] requestParts = request.toString().split("\\s+");
                if (requestParts.length > 1) {
                    URL = requestParts[1];
                    System.out.println("URL: " + URL);
                }

                StringBuilder requestedPath = new StringBuilder();
                if (Objects.equals(URL, "/")) {
                    requestedPath.append("views").append("/index.html");
                } else {
                    requestedPath.append("views").append(URL).append(".html");
                }

                List<String> res = fileReader(String.valueOf(requestedPath));
                if (res != null) {
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
                    printWriter.printf("<html><body><h1>ERROR 404 Not Found</h1><p>Requested file: %s</p></body></html>",String.valueOf(requestedPath));
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
            Path filePath = Paths.get(System.getProperty("user.dir"), "src.main","src", "main","java","com","spring", path).normalize();
            File myObj = filePath.toFile();
            List<String> result = new ArrayList<>();
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                result.add(data);
            }
            myReader.close();
            return result;
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }
    }
    public static void main(String[] args) {
        Server server = new Server();

        int port = 3000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number provided, using default port 3000.");
                port = 3000;
            }
        } else {
            System.out.println("No port number provided, using default port 3000.");
        }
        try {
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