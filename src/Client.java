import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws InterruptedException, UnknownHostException, IOException {

		int c;
		System.out.println("Clients Lancé");

		if (args.length > 0) {
			int port = Integer.parseInt(args[0]);
			String str = "coucou";
			String str2 = "server";
			for (int i = 0; i < 1; i++) {
				System.out.println("Client " + (i + 1) + " lancé");
				System.out.println("Connection au serveur");
				Socket so = new Socket("localhost", port);
				System.out.println("Connecté au serveur");

				System.out.println("Client : Envoye au serveur un message\n");
				OutputStream out = so.getOutputStream();

				// VERSION 1 : String msg = "Client " + (i+1) + ": Coucou serveur ! \n";

				// VERSION 2 : String newStr = str+"\n";
				// out.write(newStr.getBytes());
				PrintWriter print = new PrintWriter(out);
				// int nbLigne = 2;
				// for (int j = 0; j < nbLigne; j++) {
				// String monStr = "ligne "+j;
				// out.write(monStr.getBytes());
				// print.println(monStr);
				// }

				Scanner sc = new Scanner(System.in);
				System.out.println("Saisie de lignes :");
				String string;
				while (!(string = sc.nextLine()).equals("")) {
					print.println(string);
					print.flush();
					if (string.equals("finir")) {
						so.close();
						System.out.println("Client fermé");
						System.exit(0);
					}
				}
				print.println("");
				print.flush();

				// out.write("".getBytes());

				InputStream in = so.getInputStream();
				BufferedReader buff = new BufferedReader(new InputStreamReader(in));
				System.out.println("Client : Lecture du message");
				System.out.print("Reception du message : \"");

				// while ((char) (c = in.read()) != '\n') {
				// System.out.print((char) c);
				// }
				boolean stop = false;
				while (!stop) {
					String mot = buff.readLine();
					if (mot.length() == 0) {
						stop = true;
					} else {
						System.out.print(mot);
					}
				}
				System.out.println("\"");

				so.close();
			}
			/*
			 * } else {
			 * System.out.println("Indiquer le message à envoyer : 2 lignes minimum"); } }
			 * else {
			 * System.out.println("Indiquer le numéro de port et le message à envoyer"); }
			 */
		} else {
			System.out.println("Indiquer le numéro de port et le message à envoyer");
		}
	}
}
