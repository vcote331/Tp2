import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;

public class Envoi {

	public static void main(String[] argv) throws java.io.IOException {

		String NOM_FILE_DATTENTE = "file_d-attente_1";
		String nomUtilisateur = "guest"; // par defaut
		String motDePasse = "guest"; // par defaut
		int numeroPort = 5672;  // par defaut
		String virtualHostName = "/"; // par defaut
		String hostName = "localhost";
		//String hostName = "192.168.183.129";
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
		Connection connexion;
		try {
			connexion = factory.newConnection();

			// ouvrir un canal de communication avec le Broker pour l'envoi et la reception de messages
			Channel canalDeCommunication = connexion.createChannel();

			// declarer une file d'attente nommee NOM_FILE_DATTENTE
			canalDeCommunication.queueDeclare(NOM_FILE_DATTENTE, durable, false, false, null);

			// Ne pas delivrer a un consommateur plus qu'un message a la fois: Fair dispatch
			canalDeCommunication.basicQos(1);
		
			// message a envoyer 
			String message = "Salut tout le monde!";

			// envoyer le message  
			canalDeCommunication.basicPublish("", NOM_FILE_DATTENTE, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());

			System.out.println(" - Le message :'" + message + "' est envoye avec succes!");

			// fermer le canal
			canalDeCommunication.close();

			// fermer la connexion
			connexion.close();

		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
