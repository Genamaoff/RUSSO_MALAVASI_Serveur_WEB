import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainHTTP {
    public static void main(String[] args) throws IOException {
        int port;
        if (args.length == 0) {
            port = 8080;
            System.out.println("Port par défaut : " + port);
        } else {
            port = Integer.parseInt(args[0]);
            System.out.println("Port : " + port);
        }
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            try {
                // Create a server socket
                System.out.println("Server is running...");

                // Create a client socket
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected...");

                // Create an input stream to read from the client socket
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                // Create an output stream to write to the client socket
                OutputStream outputStream = clientSocket.getOutputStream();
                PrintWriter printWriter;
                printWriter = new PrintWriter(outputStream, true);

                // Read the request from the client
                String request = bufferedReader.readLine();
                String link = request.split(" ")[1].substring(1);
                System.out.println("Request: " + request);
                while (request != null && !request.isEmpty()) {
                    request = bufferedReader.readLine();
                    System.out.println(request);
                }

                //Ecriture de la réponse au client avec un fichier index existant
                File file = new File(link);
                BufferedReader bf = new BufferedReader(new FileReader(file));
                byte[] buffer = new byte[(int) file.length()];
                String response = "";
                String currRes = bf.readLine();
                while (currRes != null) {
                    response += currRes + "\n";
                    currRes = bf.readLine();
                }
                bf.close();
                printWriter.println("HTTP/1.1 200 OK");
                printWriter.println("Content-Type: text/html");
                printWriter.println("");
                // set the content type
                String contentType = "text/html";
                if (link.endsWith(".css")) {
                    contentType = "text/css";
                } else if (link.endsWith(".js")) {
                    contentType = "application/javascript";
                } else if (link.endsWith(".png")) {
                    contentType = "image/png";
                } else if (link.endsWith(".jpg")) {
                    contentType = "image/jpg";
                } else if (link.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (link.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (link.endsWith(".ico")) {
                    contentType = "image/x-icon";
                }
                printWriter.println("Content-Type: " + contentType);

                printWriter.println(response);
                System.out.println("response = " + response);
                printWriter.flush();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
