import java.sql.*;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Connection;
import java.util.*;
import java.awt.*;
import java.io.*;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import java.util.concurrent.TimeoutException;

public class P4 {
    public String textFr = null;
    public String textEn = null;
    public static void main(String[] argv) throws Exception {

        String EXCHANGE_NAME = "echangeur_topic02";
        String NOM_FILE_DATTENTE = "file_d_attente04";
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

        // autre alternative pour specifier les parametres de la connexion
        // factory.setUri("amqp://nomUtilisateur:motDePasse@hostName:numeroPort/virtualHostName");

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

            //String cleDeLiaison = "log.message";
            //String cleDeLiaison = "#";

            // lier la file d'attente a l'echangeur
            canalDeCommunication.queueBind(NOM_FILE_DATTENTE, EXCHANGE_NAME, cleDeLiaison);

            // Ne pas delivrer a un consommateur plus qu'un message a la fois: Fair dispatch
            canalDeCommunication.basicQos(0);

            System.out.println(" -* En attente de super messages ... pour arreter pressez CTRL+C");

            final Consumer consumer = new DefaultConsumer(canalDeCommunication) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    String texteFr = null;
                    String texteEn = null;
                    String img1= null;
                    String img2= null;
                    String img3= null;
                    String[] splited = message.split("\\s+");


                    try
                    {
                        String myDriver = "mysql-connector-java-5.1.46";
                        String myUrl = "jdbc:mysql://localhost:3306/Tp2";
                        //Class.forName(myDriver);
                        java.sql.Connection conn = DriverManager.getConnection(myUrl, "root", "");

                        PreparedStatement pre = conn.prepareStatement("INSERT INTO donnees (texteFR, textEN, image, image2, image3) "
                                +"VALUES (?,?,?,?,?)");

                        if (splited.length == 2){
                            System.out.println(" - Message recu: " + splited[0] + ", Message traduit : " + splited[1]);
                            texteFr = splited[0];
                            texteEn = splited[1];
                            pre.setString(1, texteFr);
                            pre.setString(2, texteEn);
                            pre.setNull(3, java.sql.Types.BLOB);
                            pre.setNull(4, java.sql.Types.BLOB);
                            pre.setNull(5, java.sql.Types.BLOB);
                        }
                        else{
                            System.out.println(" - Message recu: " + splited[0] + ", " + splited[1] + ", " + splited[2]);
                            String encodedfile = null;
                            pre.setString(1, texteFr);
                            pre.setString(2, texteEn);

                            for (int i = 0; i < splited.length; i++){
                                File imgfile = new File(splited[i]);
                                FileInputStream fin = new FileInputStream(imgfile);

                                if (i == 0){
                                    pre.setBinaryStream(3, (InputStream) fin, (int) imgfile.length());
                                }
                                else if (i == 1){
                                    pre.setBinaryStream(4, (InputStream) fin, (int) imgfile.length());
                                }
                                else{
                                    pre.setBinaryStream(5, (InputStream) fin, (int) imgfile.length());
                                }
                            }
                        }


                        pre.executeUpdate();
                        System.out.println(" Données ajoutées à la base de données ! ");
                        conn.close();
                    }
                    catch (Exception e)
                    {
                        System.err.println("Got an exception!");
                        System.err.println(e.getMessage());
                    }
                }
            };
            canalDeCommunication.basicConsume(NOM_FILE_DATTENTE, true, consumer);

        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}