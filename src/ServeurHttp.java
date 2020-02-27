import java.net.*;
import java.io.*;
import java.util.*;

import javax.net.ssl.SSLServerSocketFactory;

import java.lang.*;

public class ServeurHttp {
	
	static final String CGI_FOLDER = "cgi-bin";
	static int nbSessions = 0;
	static String serverLine = "";
	static String statusLine = null;
	static String contentTypeLine = null;
	static String entityBody = null;
	static String contentLengthLine = null;
	static final int DEBUG = 255;
	static final int PORT = 1234;

	public static void main(String args[]) throws IOException {
		System.setProperty("javax.net.ssl.keyStore", "../certificate.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "mdpmdp");
		ServerSocket sslServerSocket = null;
		SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		try {
			sslServerSocket = sslServerSocketFactory.createServerSocket(PORT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (true) {
			try {
				System.out.println("Serveur en attente " + (nbSessions++));
				Socket sck1 = sslServerSocket.accept();
				DataOutputStream os = getWriter(sck1);
				BufferedReader br = getReader(sck1);
				Traitement trait = new Traitement(os,br,sck1);
				trait.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
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
				retourCGIPOST(askedFile, br, dos);
			}
		}

	}

	@SuppressWarnings("unused")
	private static void retourFichier(String path, DataOutputStream os) throws IOException {
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

	private static ArrayList<String> executer(String f,String parameters, DataOutputStream dos) throws IOException {
		ArrayList<String> rep = new ArrayList<String>();
		try {
			ProcessBuilder pb = new ProcessBuilder("./"+CGI_FOLDER+"/"+f);
			Map<String, String> mp = pb.environment();
			mp.clear();
			mp.put("REQUEST_METHOD", "POST");
			Process p = pb.start();
			
			System.out.println(parameters);
			OutputStream o = p.getOutputStream();
			o.write(parameters.getBytes());
			o.close();
		
			
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
