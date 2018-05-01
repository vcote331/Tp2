import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.*;
import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

public class Reception {

	public static void main(String[] argv) throws Exception {

		String NOM_FILE_DATTENTE = "file_d-attente_1";
		String nomUtilisateur = "guest"; // par defaut
		String motDePasse = "guest"; // par defaut
		int numeroPort = 5672; // par defaut
		String virtualHostName = "/"; // par defaut
		String hostName = "localhost";
		boolean autoAck = false;
		boolean durable = true; 

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
		Connection connexion = factory.newConnection();

		// ouvrir un canal de communication avec le Broker pour l'envoi et la
		// reception de messages
		Channel canalDeCommunication = connexion.createChannel();

		// declarer une file d'attente nommee NOM_FILE_DATTENTE
		canalDeCommunication.queueDeclare(NOM_FILE_DATTENTE, durable, false, false, null);
		
		// Ne pas delivrer a un consommateur plus qu'un message a la fois: Fair dispatch
		canalDeCommunication.basicQos(1);
		
		System.out.println(" -* En attente de messages ... pour arreter pressez CTRL+C");

		Consumer consumer = new DefaultConsumer(canalDeCommunication) {
		      @Override
		      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		          throws IOException {
		        String message = new String(body, "UTF-8");
		        System.out.println(" - Message recu: '" + message + "'");
		        
		        canalDeCommunication.basicAck(envelope.getDeliveryTag(), false);
		      }
		    };
		canalDeCommunication.basicConsume(NOM_FILE_DATTENTE, autoAck, consumer);		
	}
}
