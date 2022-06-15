import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class MainHTTP {

    public static String port = "";
    public static String link = "";
    public static String index = "";
    public static String accept = null;
    public static String reject = null;

    /**
     * Méthode permettant de savoir si l'adresse IP mise en paramètre est acceptée par le serveur en fonction du fichier XML
     *
     * @param ip l'adresse IP en question
     * @return boolean true si l'adresse est acceptée, false sinon
     */
    private static boolean estAutorise(String ip) {
        return ip.equals(accept);
    }

    /**
     * Permettant de récupérer les différents éléments du fichier XML (selon son nom)
     *
     * @param filename le nom de l'élément à récupérer
     */
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

    /**
     * Méthode s'occupant de l'excution du serveur
     *
     * @param serverSocket le socket du serveur
     * @param link         le lien du dossier racine
     * @throws IOException
     */
    public static void run(ServerSocket serverSocket, String link) throws IOException {
        try {
            // Création d'un serveur socket pour le client dans le but de l'accepter
            Socket clientSocket = serverSocket.accept();

            //On prend son adresse IP (IPv4)
            String ipString = clientSocket.getLocalAddress().getHostAddress();
            System.out.println("🟢 Connexion d'un client : " + ipString);

            //Si l'adresse IP n'est pas autorisée dans le fichier XML, on refuse la connexion
            if (estAutorise(ipString)) {
                System.out.println("🔴 Connexion refusée : " + ipString);
                clientSocket.close();
            } else {
                // Création d'un flux d'entrée pour le client
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                // Création d'un outputstream et d'un PrintWriter pour le client (envoi de ses réponses)

                OutputStream outputStream = clientSocket.getOutputStream();
                // Création d'un flux de sortie pour le client

                PrintWriter printWriter = new PrintWriter(outputStream, true);
                // Read the request from the client
                String verifReq = bufferedReader.readLine();
                String request = "";
                while (verifReq != null && !verifReq.isEmpty()) {
                    request += "\n" + verifReq;
                    verifReq = bufferedReader.readLine();
                }
                System.out.println("🟢 Connexion acceptée.");


                // Lecture de la requête du client non nulle
                System.out.println("💡 Requête reçue : " + request);
                String[] parts = request.split(" ");

                String method = parts[0];
                System.out.println(method);
                String path = parts[1];
                System.out.println(path);
                String version = parts[2];
                System.out.println(version);

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

                printWriter.flush();
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
                byte[] dataRead = null;
                dataRead = Files.readAllBytes(file.toPath());
                clientSocket.getOutputStream().write(dataRead);
                printWriter.flush();
                clientSocket.close();
                bufferedReader.close();
                bf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        String filename = "src/protocol.xml";
        prendreElements(filename);
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
        System.out.println("🟢 Le serveur est fonctionnel. En l'attente d'une connexion...");
        RuntimeMXBean runMX = ManagementFactory.getRuntimeMXBean();
        String pidCurr = runMX.getName().split("@Ubuntu20")[0];
        File filePID = new File("/var/run/myweb.pid");
        PrintWriter pwID = new PrintWriter(filePID);
        pwID.println(pidCurr);
        pwID.close();
        while (true) {
            run(serverSocket, link);
            System.out.println("e");
        }
    }
}
