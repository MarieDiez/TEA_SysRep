import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

public class ServeurHttp {

	// comptage du nombre de sessions
	static int nbSessions = 0;

	// chaines de caracteres formant la reponse HTTP
	static String serverLine = "Server: Simple";

	static String statusLine = null;
	static String contentTypeLine = null;
	static String entityBody = null;
	static String contentLengthLine = null;

	// constante a positionner pour controler le niveau d'impressions // de controle
	// (utilisee dans la methode debug(s,n)
	static final int DEBUG = 255;

	// Lancement du serveur // Le serveur est en boucle infinie, et ne s'arrete que
	// si il y a une // erreur d'Entree/Sortie. Il y a fermeture de socket apres
	// chaque // requete.
	public static void go(int port) {
		Socket sck = null;
		ServerSocket srvk;
		DataOutputStream os = null;
		BufferedReader br = null;
		try {
			srvk = new ServerSocket(port);
			while (true) {
				System.out.println("Serveur en attente " + (nbSessions++));
				sck = srvk.accept();
				os = new DataOutputStream(sck.getOutputStream());
				br = new BufferedReader(new InputStreamReader(sck.getInputStream()));
				traiterRequete(br, os);
				sck.close();
			}
		} catch (IOException e) {
			System.out.println("ERREUR IO" + e);
		}
	}

	// Methode utile pour demander ou non des print de Trace a l'execution
	public static void debug(String s, int n) {
		if ((DEBUG & n) != 0)
			System.out.println("(" + n + ")" + s);
	}

	public static String lireLigne(String p, BufferedReader br) throws IOException {
		String s;
		s = br.readLine();
		debug(p + " " + s, 2);
		return s;
	}

	public static void traiterRequete(BufferedReader br, DataOutputStream dos) throws IOException {
		/*
		 * Cette methode lit des lignes sur br (utiliser LireLigne) et recherche une
		 * ligne commencant par GET ou par POST.
		 * 
		 * Si la ligne commence par GET : - on extrait le nom de fichier demande dans la
		 * ligne et on appelle la methode retourFichier. - Si le suffixe du nom de
		 * fichier est .htm ou .html (utiliser la methode contentType) - on lit ensuite
		 * toutes les lignes qui suivent jusqu'a entrouver une vide, nulle ou contenant
		 * juste "\n\r"
		 * 
		 * Si la ligne commence par POST : - on extrait le nom de fichier demande dans
		 * la ligne et on appelle la methode retourFichier. - Si le suffixe du nom de
		 * fichier est .htm ou .html, on fait la meme chose que ci-dessus pour GET - Si
		 * le suffixe est autre, on appelle la methode retourCGIPOST
		 */
		String str;
		str = lireLigne("", br);

		if (str.contains("GET")) {
			String nom = str.split(" /")[1].split(" ")[0];
			retourFichier(nom, dos);
		}

		if (str.contains("POST")) {
			String nom = str.split(" /")[1].split("/ ")[0];
			if (nom.contains(".htm") || nom.contains(".html")) {
				retourFichier(nom, dos);
			} else {
				for(int i = 0 ; i < 13; i++) {
					str = lireLigne(i+" :", br);				
				}
				retourCGIPOST(nom, br, dos);
			}
		}	

	}

	@SuppressWarnings("unused")
	private static void retourFichier(String f, DataOutputStream data) throws IOException {

		try {
			FileInputStream fil = new FileInputStream(f);
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = contentType(f);
			contentLengthLine = String.valueOf(fil.available());
			envoi(statusLine + "\r\n", data);
			envoi(contentTypeLine + "\r\n", data);
			envoi(contentLengthLine + "\r\n", data);
			envoi("\r\n", data);
			envoiFichier(fil, data);

		} catch (Exception e) {
			statusLine = "HTTP/1.1 404 Page not found";
			FileInputStream fil = new FileInputStream("page404.html");
			contentTypeLine = "text/html";
			contentLengthLine = String.valueOf(fil.available());
			envoi(statusLine + "\r\n", data);
			envoi(contentTypeLine + "\r\n", data);
			envoi(contentLengthLine + "\r\n", data);
			envoi("\r\n", data);
			envoiFichier(fil, data);
		}

	}

	private static void envoiFichier(FileInputStream fis, DataOutputStream os) throws IOException {
		byte[] buffer = new byte[1024];
		int bytes = 0;
		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
		envoi("\r\n", os);
	}

	private static ArrayList executer(String f, DataOutputStream dos) throws IOException {
		ArrayList<String> rep = new ArrayList<String>();
		try {
			Process p = Runtime.getRuntime().exec("./cgi-bin/test.cgi");
            BufferedReader in = new BufferedReader(
                                new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                rep.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		return rep;
	}

	private static void retourCGIPOST(String f, BufferedReader br, DataOutputStream dos) throws IOException {		
		String nom = f.split(" ")[0];
		ArrayList<String> rep = executer(nom, dos);
		
		statusLine = "HTTP/1.1 200 OK";
		envoi(statusLine + "\r\n", dos);
		envoi(contentLengthLine + "\r\n", dos);
		contentTypeLine = rep.get(0).split(" ")[1];
		envoi(contentTypeLine + "\r\n", dos);
		envoi("\r\n", dos);
		
		rep.remove(0);
		for (String line:rep){
			envoi(line+"\r\n",dos);
			
		}
	}

	private static void envoi(String m, DataOutputStream dos) throws IOException {
		dos.write(m.getBytes());
	}

	private static void entete(DataOutputStream dos) throws IOException {
		envoi(statusLine + "\r\n", dos);
		envoi(serverLine + "\r\n", dos);
		envoi(contentTypeLine + "\r\n", dos);
		envoi(contentLengthLine + "\r\n", dos);
		envoi("\r\n", dos);
	}

	private static String contentType(String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		return "";
	}

	public static void main(String args[]) throws IOException {
		go(1234);
		System.out.println("ARRET DU SERVEUR");
	}
}
