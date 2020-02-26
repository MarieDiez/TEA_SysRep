import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

public class ServeurHttp {
	
	static final int PORT = 1234;

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

	public static void main(String args[]) throws IOException {
		Socket sck = null;
		ServerSocket srvk;
		DataOutputStream os = null;
		BufferedReader br = null;
		try {
			srvk = new ServerSocket(PORT);
			while (true) {
				System.out.println("Serveur en attente " + (nbSessions++));
				sck = srvk.accept();
				os = getWriter(sck);
				br = getReader(sck);
				
				traiterRequete(br, os);
				
				sck.close();
			}
		} catch (IOException e) {
			System.out.println("ERREUR IO" + e);
		}
		System.out.println("ARRET DU SERVEUR");
	}
	
	private static BufferedReader getReader(Socket sock) {
		InputStream in = null;
		try {
			in = sock.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BufferedReader(new InputStreamReader(in));
	}

	private static DataOutputStream getWriter(Socket sock) {
		OutputStream out = null;
		try {
			out = sock.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new DataOutputStream(out);
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
	String headLine;
	headLine = br.readLine();

	if (headLine.contains("GET")) {
		String askedFile = headLine.split(" /")[1].split(" ")[0];
		retourFichier(askedFile, dos);
		
	}else if (headLine.contains("POST")) {
		String askedFile = headLine.split(" /")[1].split("/ ")[0];
		if (askedFile.contains(".htm") || askedFile.contains(".html")) {
				retourFichier(askedFile, dos);
			} else {
				for(int i = 0 ; i < 20 ; i++) {
					headLine = lireLigne(i+" :", br);					
				}
				retourCGIPOST(askedFile, br, dos);
			}
		}

	}

	@SuppressWarnings("unused")
	private static void retourFichier(String path, DataOutputStream os) throws IOException {
		System.out.println(path);
		try {
			FileInputStream fil = new FileInputStream(path);
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = contentType(path);
			contentLengthLine = String.valueOf(fil.available());
			envoi(statusLine + "\r\n", os);
			envoi(contentTypeLine + "\r\n", os);
			envoi(contentLengthLine + "\r\n", os);
			envoi("\r\n", os);
			envoiFichier(fil, os);

		} catch (Exception e) {
			statusLine = "HTTP/1.1 404 Page not found";
			FileInputStream fil = new FileInputStream("page404.html");
			contentTypeLine = "text/html";
			contentLengthLine = String.valueOf(fil.available());
			envoi(statusLine + "\r\n", os);
			envoi(contentTypeLine + "\r\n", os);
			envoi(contentLengthLine + "\r\n", os);
			envoi("\r\n", os);
			envoiFichier(fil, os);
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

	private static void envoi(String m, DataOutputStream os) throws IOException {
		os.write(m.getBytes());
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
}
