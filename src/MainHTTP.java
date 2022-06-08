import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainHTTP {

    public MainHTTP() {

    }

    public static void main(String[] args) throws IOException {
        String port = "";
        String link = "";
        String index = "";
        String accept = "";
        String reject = "";

        String FILENAME = "src/protocol.xml";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(FILENAME));
            NodeList list = doc.getElementsByTagName("webconf");
            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    port = element.getElementsByTagName("port").item(0).getTextContent();
                    System.out.println(port);
                    link = element.getElementsByTagName("root").item(0).getTextContent();
                    System.out.println(link);
                    index = element.getElementsByTagName("index").item(0).getTextContent();
                    System.out.println(index);
                    accept = element.getElementsByTagName("accept").item(0).getTextContent();
                    System.out.println(accept);
                    reject = element.getElementsByTagName("reject").item(0).getTextContent();
                    System.out.println(reject);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
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
                String request = "GET " + link + " HTTP/1.1\r\n";
                System.out.println("Request: " + request);
                link = request.split(" ")[1];
                System.out.println("Link: " + link);
                while (request != null && !request.isEmpty()) {
                    request = bufferedReader.readLine();
                    System.out.println(request);
                }

                //Ecriture de la réponse au client avec un fichier index existant
                File file = new File(link);
                BufferedReader bf = new BufferedReader(new FileReader(file));
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
