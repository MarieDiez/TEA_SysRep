import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {

		System.out.println("Serveur Lancé");
		int c;
		if (args.length > 0) {
			int port = Integer.parseInt(args[0]);
			ServerSocket sock;
			sock = new ServerSocket(port);
			int compteurClient = 0;
			while (true) {
				System.out.println("Attente de client :");
				Socket so = sock.accept();
				System.out.println("Client " + so.getInetAddress() + " est connecté sur le serveur, port : " + so.getPort());

				InputStream in;
				System.out.println("Message du client : ");
				in = so.getInputStream();
				String msgClient = "";
				// PrinterWriter
				
				// VERSION 1 : int k = 0;
				//while ((char) (c = in.read()) != '\n') {
				//	msgClient += (char)c;
				//	k++;
				//}
				//System.out.println(msgClient);
				BufferedReader buff = new BufferedReader(new InputStreamReader(in));
				boolean stop = false;
				while(!stop) {
					String mot = buff.readLine();
					if (mot.length() == 0 || mot.equals("finir")) {
						stop = true;
						if (mot.equals("finir")) {
							so.close();
							System.out.println("Serveur fermé");
							System.exit(0);
						}
					} else {
						System.out.println(mot);
						msgClient += mot;
					}
				}
				System.out.println(msgClient);
				System.out.print('\n');
			
		
				OutputStream out = so.getOutputStream();
				System.out.println("Serveur : Message envoyé");
				String str = "Je suis le serveur "+InetAddress.getLocalHost()+" je t'envoie : Coucou connexion "+(compteurClient+1)+" ! Message Client : "+msgClient+"\n";
				//out.write(str.getBytes());
				PrintWriter print = new PrintWriter(out);
				print.println(str);
				print.println("");
				print.flush();
				compteurClient++;
			 
				
			}
		} else {
			System.out.println("Indiquer le numéro de port");
		}
		
	}
}
