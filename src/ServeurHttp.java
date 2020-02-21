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
		 
		 * Si la ligne commence par GET : 
			 * 	- on extrait le nom de fichier demande dans la ligne et on appelle la methode
			 * 	retourFichier. 
			 * - Si le suffixe du nom de fichier est .htm ou .html (utiliser
			 * la methode contentType) 
			 * - on lit ensuite toutes les lignes qui suivent
			 * jusqu'a entrouver une vide, nulle ou contenant juste "\n\r" 
		
		 * Si la ligne commence par POST :
			 * 	- on extrait le nom de fichier demande dans la ligne et
			 * 	on appelle la methode retourFichier. 
			 * - Si le suffixe du nom de fichier est
			 * .htm ou .html, on fait la meme chose que ci-dessus pour GET 
			 * - Si le suffixe est autre, on appelle la methode retourCGIPOST
		 */
		String str;
		str = lireLigne("debug : ",br);
	    	
    	if (str.contains("GET")) {
    		String nom = str.split(" /")[1].split(" ")[0];
    		retourFichier(nom,dos);
    		
    		if (nom.contains(".htm") || nom.contains(".html")) {
    			contentTypeLine = contentType(nom);
	    	}
    		
    	}
    	
    	if (str.contains("POST")) {
    		String nom = str.split(" /")[1].split("/ ")[0];
    		retourFichier(nom,dos);
    		if (nom.contains(".htm") || nom.contains(".html")) {
    			contentTypeLine = contentType(nom);
	    	} else {
	    		System.out.println("retourCGI");
	    		//retourCGIPOST(nom, br, dos);
	    	}
    	}
	
		
	}

	private static void retourFichier(String f, DataOutputStream data) throws IOException {
		/*
		 * - Si le fichier existe, on prepare les infos status, contentType,
		 * contentLineLength qui conviennent on les envoit, et on envoit le fichier
		 * (methode envoiFichier) 
		 * 
		 * - Si le fichier n'existe pas on prepare les infos qui
		 * conviennent et on les envoit
		 */
		
		if (f != null) {
			statusLine = "fichier existe";
			contentTypeLine = contentType(f);
			contentLengthLine = String.valueOf(f.length());
			envoi(statusLine + "\r\n", data);
			envoi(serverLine + "\r\n", data);
			envoi(contentTypeLine + "\r\n", data);
			envoi(contentLengthLine + "\r\n", data);
			envoi("\r\n", data);
			
			FileInputStream fil = new FileInputStream(f);
			envoiFichier(fil, data);
			
		} else {
			statusLine = "fichier existe pas";
			contentTypeLine = contentType(f);
			contentLengthLine = String.valueOf(f.length());
			envoi(statusLine + "\r\n", data);
			envoi(serverLine + "\r\n", data);
			envoi(contentTypeLine + "\r\n", data);
			envoi(contentLengthLine + "\r\n", data);
			envoi("\r\n", data);
			
			FileInputStream fil = new FileInputStream(f);
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

	private static String executer(String f) {
		String R = "";
		return "";
	}

	private static void retourCGIPOST(String f, BufferedReader br, DataOutputStream dos) throws IOException {
		/*
		 * On lit toutes les lignes jusqu'a trouver une ligne commencant par
		 * Content-Length Lorsque cette ligne est trouvee, on extrait le nombre qui
		 * suit(nombre de caracteres a lire). On lit une ligne vide On lit les
		 * caracteres dont le nombre a ete trouve ci-dessus on les range dans une
		 * chaine, On appelle la methode 'executer' en lui donnant comme parametre une
		 * chaine qui est la concatenation du nom de fichier, d'un espace et de la
		 * chaine de parametres. 'executer' retourne une chaine qui est la reponse ‡
		 * renvoyer au client, apres avoir envoye les infos status, contentTypeLine,
		 * ....
		 */
		String str;
		while (!(str = lireLigne("debug : ",br)).contains("Content-length")) {
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
