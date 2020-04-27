import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Sender {

	public static void main(String args[]) throws Exception {
		
		
		String address = args[0];
		
		int Rport = Integer.parseInt(args[1]);
		
		int Sport = Integer.parseInt(args[2]);
		
		String f = args[3];
		
		int timeout = Integer.parseInt(args[4]);
		
		DatagramSocket ds = new DatagramSocket(Sport);

		byte[] b = new byte[1000];
		DatagramPacket dp = new DatagramPacket(b, b.length);
		ds.receive(dp);
		
		
		//String path = new String(b);

		//File file = new File(path.trim());
		
		File file = new File(f);
		int length = (int) file.length();
		byte[] filebyte = new byte[length];
	
		
		FileInputStream fis = new FileInputStream(file);
		fis.read(filebyte);
		fis.close();

		InetAddress ip = InetAddress.getByName(address);
		byte[] databyte = new byte[125];
		DatagramPacket dpsend = new DatagramPacket(databyte, databyte.length, ip, Rport);

		int p = 0;
		int Seq = 0;
		int c = 0;
		
		ds.setSoTimeout(timeout);
		while (true) {

			if ((length - p) > 124) {
				byte[] header = String.valueOf(Seq).getBytes();
				System.arraycopy(header, 0, databyte, 0, 1);
				System.arraycopy(filebyte, p, databyte, 1, 124);
				ds.send(dpsend);
				
				byte[] ackbyte = new byte[1];
				DatagramPacket ackdp = new DatagramPacket(ackbyte,ackbyte.length);
				try {
					ds.receive(ackdp);
				}catch(SocketTimeoutException e) {
				
					continue;
				}
				
				p = p + 124;
			} else {
				int offset = length - p;

				byte[] last = new byte[offset + 1];
				byte[] header = String.valueOf(Seq).getBytes();
				System.arraycopy(header, 0, last, 0, 1);
				System.arraycopy(filebyte, p, last, 1, offset);
				dpsend.setData(last);
				ds.send(dpsend);
				
				byte[] ackbyte = new byte[1];
				DatagramPacket ackdp = new DatagramPacket(ackbyte,ackbyte.length);
				try {
					ds.receive(ackdp);
				}catch(SocketTimeoutException e) {
					continue;
				}
				
				byte[] eotbyte = String.valueOf(3).getBytes();
				DatagramPacket eotdp = new DatagramPacket(eotbyte, eotbyte.length, ip, Rport);
				ds.send(eotdp);

				break;
			}
			
			if (Seq == 1) {
				Seq = 0;
			} else {
				Seq = 1;
			}

			System.out.println(c);
			c++;
		}

		ds.close();
	}

	
}
