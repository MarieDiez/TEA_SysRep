import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocketFactory;

public class ThreadServer extends Thread {
	
	ServerSocket sslServerSocket;
	DataOutputStream os;
	BufferedReader br;
	Socket sck1;
	
	public ThreadServer(Socket sck1) {
		this.os = null;
		this.br = null;
		this.sck1 = sck1;
	}
	
	@Override
	public void run() {
		os = getWriter(sck1);
		br = getReader(sck1);
		Traitement trait = new Traitement(os,br,sck1);
		trait.start();
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
}
