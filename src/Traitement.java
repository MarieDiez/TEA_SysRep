import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class Traitement extends Thread{
	
	DataOutputStream os;
	BufferedReader br;
	Socket sck1;
	static String statusLine = null;
	static String contentTypeLine = null;
	static String entityBody = null;
	static String contentLengthLine = null;
	static final String CGI_FOLDER = "cgi-bin";
	
	public Traitement(DataOutputStream os, BufferedReader br, Socket sck1) {
		this.os = os;
		this.br = br;
		this.sck1 = sck1;
	}
	
	@Override
	public void run() {
		try {
			// traitement de la requete
			traiterRequete(this.br, this.os);
			this.sck1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Traitement de la requete : En fonction GET / POST 
	 * GET : appel de retourFichier
	 * POST : si contient .html ou .htm appel de retourFichier
	 * 		  sinon appel de retourGCIPOST : GCI
	 * @param br
	 * @param dos
	 * @throws IOException
	 */
	public void traiterRequete(BufferedReader br, DataOutputStream dos) throws IOException {
	String headLine;
	headLine = br.readLine();

	// GET
	if (headLine.contains("GET")) {
		String askedFile = headLine.split(" /")[1].split(" ")[0];
		retourFichier(askedFile, dos);
	} 
	// POST
	else if (headLine.contains("POST")) {
		String askedFile = headLine.split(" /")[1].split("/ ")[0];
		if (askedFile.contains(".htm") || askedFile.contains(".html")) {
				retourFichier(askedFile, dos);
			} else {
				// CGI
				retourCGIPOST(askedFile, br, dos);
			}
		}
	}

	/**
	 * Remplissage des éléments de requete https : statusLine / ContentLine / ContentLengthLine 
	 * Envoie de ces informations au client avec la méthode envoi
	 * Envoie du fichier
	 * @param path
	 * @param os
	 * @throws IOException
	 */
	private static void retourFichier(String path, DataOutputStream os) throws IOException {
		try {
			// Page trouvé
			FileInputStream fil = new FileInputStream("../html/"+path);
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = contentType(path);
			contentLengthLine = String.valueOf(fil.available());
			entete(os);
			envoiFichier(fil, os);
		} catch (Exception e) {
			// page non trouvé
			statusLine = "HTTP/1.1 404 Page not found";
			FileInputStream fil = new FileInputStream("../html/page404.html");
			contentTypeLine = "text/html";
			contentLengthLine = String.valueOf(fil.available());
			entete(os);
			envoiFichier(fil, os);
		}

	}

	/**
	 * Envoie du fichier au client
	 * @param fis
	 * @param os
	 * @throws IOException
	 */
	private static void envoiFichier(FileInputStream fis, DataOutputStream os) throws IOException {
		byte[] buffer = new byte[1024];
		int bytes = 0;
		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
		envoi("\r\n", os);
	}

	/**
	 * Execution du script CGI :
	 * On lance le processus et on met la variable d'environnement REQUEST_METHOD à POST
	 * On lance le script avec start et on lui envoie des paramètres (a et b pour l'addition)
	 * On récupère la réponse
	 * La réponse du script étant l'addition des parametres et la génération d'une page html avec la réponse du calcule,
	 * celle-ci ce présente sous la forme d'une ArrayList de String qui représente le code html de la page. 
	 * @param f
	 * @param parameters
	 * @param dos
	 * @return
	 * @throws IOException
	 */
	private static ArrayList<String> executer(String f,String parameters, DataOutputStream dos) throws IOException {
		ArrayList<String> rep = new ArrayList<String>();
		
		try {
			ProcessBuilder pb = new ProcessBuilder("./"+CGI_FOLDER+"/"+f);
			Map<String, String> mp = pb.environment();
			mp.clear();
			mp.put("REQUEST_METHOD", "POST");
			Process p = pb.start();
			OutputStream o = p.getOutputStream();
			o.write(parameters.getBytes());
			o.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                rep.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		return rep;
	}

	/**
	 * On récupère la valeur du content-length dans l'entête de la requête.
	 * On cherche une ligne vide et par la suite on récupère les arguments/paramètre.
	 * On execute le script cgi avec les paramètres
	 * On récupère la réponse de l'execution (sous forme d'ArrayList de String)
	 * On envoi la réponse du script CGI au client -> affichage de la page html avec la réponse de l'addition.
	 * @param f
	 * @param br
	 * @param dos
	 * @throws IOException
	 */
	private static void retourCGIPOST(String f, BufferedReader br, DataOutputStream dos) throws IOException {
		String fileName = f.split(" ")[0];
		int value = 0;
		String header = br.readLine();
		while(!header.split(": ")[0].equals("Content-Length")) {
			header = br.readLine();
		}
		value = Integer.parseInt(header.split(": ")[1]);
		while(!br.readLine().equals("")) {}
		String ch = "";
		for(int i = 0; i < value; i++) {
			ch += (char)br.read();
		}
		ArrayList<String> rep = executer(fileName, ch, dos);
		statusLine = "HTTP/1.1 200 OK";
		contentTypeLine = rep.get(0).split(" ")[1];
		entete(dos);
		rep.remove(0);
		for (String line:rep){
			envoi(line+"\r\n",dos);
		}
	}

	/**
	 * Envoi de donnée au client : ecriture sur le dataoutput
	 * @param m
	 * @param os
	 * @throws IOException
	 */
	private static void envoi(String m, DataOutputStream os) throws IOException {
		os.write(m.getBytes());
	}

	/**
	 * Envoie l'entete des requetes https
	 * @param dos
	 * @throws IOException
	 */
	private static void entete(DataOutputStream dos) throws IOException {
		envoi(statusLine + "\r\n", dos);
		envoi(contentTypeLine + "\r\n", dos);
		envoi(contentLengthLine + "\r\n", dos);
		envoi("\r\n", dos);
	}

	/**
	 * determine le contentType avec le nom du fichier
	 * @param fileName
	 * @return
	 */
	private static String contentType(String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		return "";
	}
}
