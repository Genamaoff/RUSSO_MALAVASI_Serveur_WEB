import com.sun.tools.javac.Main;
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
import java.util.Objects;

import static java.lang.Integer.toBinaryString;

public class MainHTTP {

    public static String port = "";
    public static String link = "";
    public static String index = "";
    public static String accept = null;
    public static String reject = null;

    public static void run(ServerSocket serverSocket, String link) throws IOException {
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
                System.out.println("Request: " + request);
                if (request == null || !new File(link + request).exists()) {
                    request = "GET " + MainHTTP.link + "/error404.html HTTP/1.1";
                } else {
                    System.out.println(request.split(" ")[1]);
                    System.out.println(MainHTTP.link);
                    if (request.split(" ")[1].equals(MainHTTP.link) || request.split(" ")[1].equals("/")) {
                        request = "GET " + MainHTTP.link + "/technique.html HTTP/1.1";
                        System.out.println("ok");
                    } else {
                        request = request.split(" ")[0] + " " + MainHTTP.link + request.split(" ")[1] + " " + request.split(" ")[2];
                    }
                }
                System.out.println("Request: " + request);
                link = request.split(" ")[1].substring(1);

                //Ecriture de la réponse au client avec un fichier index existant
                printWriter.println("HTTP/1.1 200 OK");
                // set the content type
                String contentType = "";
                if (link.endsWith(".html")) {
                    contentType = "text/html";
                }
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
                printWriter.print("");
                printWriter.println("responseType:'arraybuffer'");
                printWriter.println("");

                File file = new File(link);
                System.out.println("Link : " + link);
                DataInputStream bf = new DataInputStream(new FileInputStream(file));
                StringBuilder response = new StringBuilder();
                int dataRead = bf.read();

                while (dataRead != -1) {
                    if (contentType.equals("text/html")) {
                        response.append((char) dataRead);
                    } else {
                        //convert the int to an gif image for HTTP
                        response.append((char) dataRead);
                    }
                    dataRead = bf.read();
                }
                System.out.println(response);
                bf.close();
                printWriter.print(response);
                System.out.println("response = " + response);
                printWriter.flush();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void prendreElements(String filename) throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(filename));
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
    }


    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String filename = "src/protocol.xml";
        prendreElements(filename);
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
        run(serverSocket, link);
    }
}
