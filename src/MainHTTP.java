import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MainHTTP {

    public static String port = "";
    public static String link = "";
    public static String index = "";
    public static String accept = null;
    public static String reject = null;

    public static void run(ServerSocket serverSocket, String link) throws IOException {
        System.out.println("🟢 Le serveur est fonctionnel. En l'attente d'une connexion...");
        while (true) {
            try {
                // Création d'un serveur socket pour le client dans le but de l'accepter
                Socket clientSocket = serverSocket.accept();
                // Création d'un inputstream et d'un BufferedReader pour le client (réception de ses demandes)
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                System.out.println("💡 Client connecté");

                // Création d'un outputstream et d'un PrintWriter pour le client (envoi de ses réponses)
                OutputStream outputStream = clientSocket.getOutputStream();
                PrintWriter printWriter;
                printWriter = new PrintWriter(outputStream, true);

                // Lecture de la requête du client non nulle
                String request = "";
                request = bufferedReader.readLine();
                System.out.println("non mais !");
                System.out.println(request);
                System.out.println("💡 Requête reçue : " + request);
                String[] parts = request.split(" ");

                String method = parts[0];
                String path = parts[1];
                String version = parts[2];

                File fileTest = new File(MainHTTP.link + path);
                System.out.println(MainHTTP.link + path);
                System.out.println(request.split(" ")[1]);
                System.out.println(MainHTTP.link);
                if (path.equals(MainHTTP.link) || path.equals("/")) {
                    request = method + " " + MainHTTP.link + "/technique.html " + version;
                    System.out.println("ok");
                } else {
                    request = method + " " + MainHTTP.link + path + " " + version;
                }

                System.out.println("Request: " + request);
                path = request.split(" ")[1];
                link = path.substring(1);

                System.out.println(link);

                if (new File(link).exists()) {
                    System.out.println("ok");
                } else {
                    System.out.println("ko");
                    link = MainHTTP.link.substring(1) + "/error404.html";
                }
                //Ecriture de la réponse au client
                printWriter.println("HTTP/1.1 200 OK");

                // Choix du content-type pour le codage de la réponse
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
                    response.append((char) dataRead);
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
                    System.out.println("Port = " + port);
                    link = element.getElementsByTagName("root").item(0).getTextContent();
                    System.out.println("Link = " + link);
                    index = element.getElementsByTagName("index").item(0).getTextContent();
                    System.out.println("Index = " + index);
                    accept = element.getElementsByTagName("accept").item(0).getTextContent();
                    System.out.println("Accept = " + accept);
                    reject = element.getElementsByTagName("reject").item(0).getTextContent();
                    System.out.println("Reject = " + reject);
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
