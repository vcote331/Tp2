import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Connection;
import java.util.*;
import java.io.IOException;
import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import java.util.concurrent.TimeoutException;
import java.io.StringReader;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

public class P2 {
    static String subscriptionKey = "3efc9b302e904a9da85d87a110b793df";

    static String host = "https://api.microsofttranslator.com";
    static String path = "/V2/Http.svc/Translate";

    static String target = "en";

    public static void main(String[] argv) throws Exception {

        String EXCHANGE_NAME = "echangeur_topic02";
        String NOM_FILE_DATTENTE = "file_d_attente02";
        String cleDeLiaison = "log.message" ; // cle de liaison/routage du message
        String nomUtilisateur = "guest"; // par defaut
        String motDePasse = "guest"; // par defaut
        int numeroPort = 5672;  // par defaut
        String virtualHostName = "/"; // par defaut
        String hostName = "localhost";
        //String hostName = "192.168.183.129";



        boolean autoAck = false;
        boolean durable = true;
        boolean passive = true; // a true, on suppose que l'echangeur existe deja
        boolean autoDelete = false; // ne pas supprimer l'echangeur lorsqu'aucun client n'est connecte
        boolean exclusive = false;
        // se connecter au broker RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();

        // indiquer les parametres de la connexion
        factory.setUsername(nomUtilisateur);
        factory.setPassword(motDePasse);
        factory.setPort(numeroPort);
        factory.setVirtualHost(virtualHostName);
        factory.setHost(hostName);

        // creer une nouvelle connexion
        Connection connexion;
        try {
            connexion = factory.newConnection();

            // ouvrir un canal de communication avec le Broker pour l'envoi et la
            // reception de messages
            Channel canalDeCommunication = connexion.createChannel();
            canalDeCommunication.exchangeDeclare(EXCHANGE_NAME, "topic", passive, durable, autoDelete, null);


            // recuperer le nom d file d'attente associee a
            //String nomFileDAttente = canalDeCommunication.queueDeclare().getQueue();
            canalDeCommunication.queueDeclare(NOM_FILE_DATTENTE , durable, exclusive, autoDelete, null);
            canalDeCommunication.queueDeclare("file_d_attente05" , durable, exclusive, autoDelete, null);
            // lier la file d'attente a l'echangeur
            canalDeCommunication.queueBind(NOM_FILE_DATTENTE, EXCHANGE_NAME, cleDeLiaison);
            canalDeCommunication.queueBind("file_d_attente05", EXCHANGE_NAME, "log.message2");

            // Ne pas delivrer a un consommateur plus qu'un message a la fois: Fair dispatch
            canalDeCommunication.basicQos(0);

            System.out.println(" -* En attente de messages ... pour arreter pressez CTRL+C");

            final Consumer consumer = new DefaultConsumer(canalDeCommunication) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" - Message recu: '" + message + "', cle de routage: '"+envelope.getRoutingKey()+" '");
                    //canalDeCommunication.basicPublish("", "file_d_attente04", null, message.getBytes("UTF-8"));
                    System.out.println(" - Le texte : " + message.toString() +" est envoye avec succes!");
                    try {
                        String reponse = Translate(message.toString());
                        message += " " + reponse;
                        canalDeCommunication.basicPublish("", "file_d_attente04", null, message.getBytes("UTF-8"));
                        System.out.println(" - La traduction : " + reponse.toString() +" est envoye avec succes!");
                    }
                    catch (Exception e) {
                        System.out.println (e);
                    }


                    canalDeCommunication.basicAck(envelope.getDeliveryTag(), false);
                }
            };

            canalDeCommunication.basicConsume(NOM_FILE_DATTENTE, true, consumer);
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String Translate (String text) throws Exception {
        String encoded_query = URLEncoder.encode (text, "UTF-8");
        String params = "?to=" + target + "&text=" + text;
        URL url = new URL (host + path + params);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setDoOutput(true);

        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        JAXBContext jc = JAXBContext.newInstance(String.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        StreamSource xmlSource = new StreamSource(new StringReader(response.toString()));
        JAXBElement<String> je = unmarshaller.unmarshal(xmlSource, String.class);

        return je.getValue();
    }
}


