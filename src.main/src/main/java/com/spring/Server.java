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

                StringBuilder requestedPath = handleURL(bufferedReader);

                serveFile(printWriter, requestedPath);

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

    public static StringBuilder handleURL(BufferedReader bufferedReader) throws IOException {
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
        } else if (URL.charAt(URL.length() - 1) == '/') {
            requestedPath.append("views").append(URL).append("index.html");
        } else if (getLastWordInURL(URL).equals("css")) {
            requestedPath.append("views").append(URL);
        } else if (getLastWordInURL(URL).equals("ico")) {
            requestedPath.append("views").append(URL);
        } else if (getLastWordInURL(URL).equals("svg")) {
            requestedPath.append("views").append(URL);
        } else if (getLastWordInURL(URL).equals("js")) {
            requestedPath.append("views").append(URL);
        } else {
            requestedPath.append("views").append(URL).append(".html");
        }
        return requestedPath;
    }

    public static String getLastWordInURL(String url) {
        String[] segments = url.split("/");
        String lastSegment = "";
        for (int i = segments.length - 1; i >= 0; i--) {
            if (!segments[i].isEmpty()) {
                lastSegment = segments[i];
                break;
            }
        }
        lastSegment = lastSegment.split("\\?")[0];
        lastSegment = lastSegment.split("#")[0];
        try {
            lastSegment = lastSegment.split("\\.")[1];
        } catch (Exception e) {
            lastSegment = lastSegment.split("\\.")[0];
        }
        return lastSegment;
    }

    public static void serveFile(PrintWriter printWriter, StringBuilder requestedPath) {
        List<String> res = fileReader(String.valueOf(requestedPath));
        if (res != null) {
            String contentType = "text/html";
            if (requestedPath.toString().endsWith(".css")) {
                contentType = "text/css";
            } else if (requestedPath.toString().endsWith(".ico")) {
                contentType = "image/x-icon";
            } else if (requestedPath.toString().endsWith(".svg")) {
                contentType = "image/svg";
            }

            printWriter.println("HTTP/1.1 200 OK");
            printWriter.println("Content-Type: " + contentType);
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
