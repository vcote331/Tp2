import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.*;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;

public class P1 {
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

        boolean durable = true;
        boolean passive = true; // a true, on suppose que l'echangeur existe
        // deja
        boolean autoDelete = false; // ne pas supprimer l'echangeur lorsqu'aucun
        // client n'est connecte

        boolean exclusive = false;

        // se connecter au broker RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();

        // indiquer les parametres de la connexion
        factory.setUsername(nomUtilisateur);
        factory.setPassword(motDePasse);
        factory.setPort(numeroPort);
        factory.setVirtualHost(virtualHostName);
        factory.setHost(hostName);
        // autre alternative pour specifier les parametres de la connexion
        // factory.setUri("amqp://nomUtilisateur:motDePasse@hostName:numeroPort/virtualHostName");

        // creer une nouvelle connexion
        Connection connexion;
        try {
            connexion = factory.newConnection();

            // ouvrir un canal de communication avec le Broker pour l'envoi et la reception de messages
            Channel canalDeCommunication = connexion.createChannel();

            // declarer un echangeur de type topic
            canalDeCommunication.exchangeDeclare(EXCHANGE_NAME, "topic", passive, durable, autoDelete, null);

            canalDeCommunication.queueDeclare(NOM_FILE_DATTENTE , durable, exclusive, autoDelete, null);
            canalDeCommunication.queueBind(NOM_FILE_DATTENTE, EXCHANGE_NAME, cleDeLiaison);
            canalDeCommunication.basicQos(1);

            // message a envoyer
            String message = "Salut tout le monde!";


            for (int i = 0; i < argv.length; i++){
                // envoyer le message
                //argv[i].getBytes("UTF-8"); a la place de messag.getbyte
                if (argv[i].equals("-t") && argv.length > i+1){
                    canalDeCommunication.basicPublish("", NOM_FILE_DATTENTE, null, argv[i + 1].getBytes("UTF-8"));
                    System.out.println(" - Le texte :'" + argv[i + 1].toString() + "' est envoye avec succes!");
                }
                else if (argv[i].equals("-i") && argv.length > i+1){
                    canalDeCommunication.basicPublish("", "file_d_attente03", null, argv[i + 1].getBytes("UTF-8"));
                    System.out.println(" - Limage à été envoye avec succes!");
                }

            }

            // fermer le canal
            canalDeCommunication.close();

            // fermer la connexion
            connexion.close();

        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
