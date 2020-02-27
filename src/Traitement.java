import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Traitement extends Thread{
	
	DataOutputStream os;
	BufferedReader br;
	Socket sck1;
	
	public Traitement(DataOutputStream os, BufferedReader br, Socket sck1) {
		this.os = os;
		this.br = br;
		this.sck1 = sck1;
	}
	@Override
	public void run() {
		try {
			ServeurHttp.traiterRequete(this.br, this.os);
			this.sck1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
