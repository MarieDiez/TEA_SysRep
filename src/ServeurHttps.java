import java.net.*;
import java.io.*;
import javax.net.ssl.SSLServerSocketFactory;

public class ServeurHttps {
	
	static int nbSessions = 0;
	static final int DEBUG = 255;
	static final int PORT = 1234;

	/**
	 * main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		
		// HTPPS : set properties 
		setProperties();
		
		// server socket
		ServerSocket sslServerSocket = null;
		
		// HTTPS : SSLServerSocketFactory 
		SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		try {
			sslServerSocket = sslServerSocketFactory.createServerSocket(PORT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// boucle d'acceptation des clients
		while (true) {
			try {
				System.out.println("Serveur en attente : " + (nbSessions++));
				Socket sck1 = sslServerSocket.accept();
				DataOutputStream os = getWriter(sck1);
				BufferedReader br = getReader(sck1);
				// MultiThreading - traitement des clients
				Traitement trait = new Traitement(os,br,sck1);
				trait.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * set properties for HTTPS
	 */
	public static void setProperties() {
		System.setProperty("javax.net.ssl.keyStore", "../certificate/certificate.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "mdpmdp");
	}
	
	/**
	 * get input stream of a socket
	 * @param sock
	 * @return
	 */
	private static BufferedReader getReader(Socket sock) {
		InputStream in = null;
		try {
			in = sock.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BufferedReader(new InputStreamReader(in));
	}

	/**
	 * get output stream of a socket
	 * @param sock
	 * @return
	 */
	private static DataOutputStream getWriter(Socket sock) {
		OutputStream out = null;
		try {
			out = sock.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new DataOutputStream(out);
	}

}
