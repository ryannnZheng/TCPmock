import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;

import java.net.*;

public class Receiver extends JFrame {

	private JPanel MainPane;
	private JTextField hostt;
	private JTextField Sportt;
	private JTextField Rportt;
	private JTextField filet;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				try {
					Receiver frame = new Receiver();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Receiver() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 500);
		MainPane = new JPanel();
		MainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(MainPane);
		MainPane.setLayout(null);
		
		JLabel hostl = new JLabel("Host Address of Sender");
		hostl.setBounds(20, 25, 120, 15);
		MainPane.add(hostl);
		
		hostt = new JTextField();
		hostt.setBounds(20, 40, 180, 20);
		MainPane.add(hostt);
		hostt.setColumns(10);
		
		JLabel Sportl = new JLabel("Host Port of Sender");
		Sportl.setBounds(230, 25, 120, 15);
		MainPane.add(Sportl);
		
		Sportt = new JTextField();
		Sportt.setBounds(229, 40, 180, 20);
		MainPane.add(Sportt);
		Sportt.setColumns(10);
		
		JLabel Rportl = new JLabel("Host port of Receiver");
		Rportl.setBounds(20, 90, 120, 15);
		MainPane.add(Rportl);
		
		JLabel filel = new JLabel("Name of File");
		filel.setBounds(230, 90, 120, 15);
		MainPane.add(filel);
		
		Rportt = new JTextField();
		Rportt.setBounds(20, 105, 180, 20);
		MainPane.add(Rportt);
		Rportt.setColumns(10);
		
		filet = new JTextField();
		filet.setBounds(230, 105, 180, 20);
		MainPane.add(filet);
		filet.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(20, 155, 400, 215);
		MainPane.add(scrollPane);
		
		JTextArea display = new JTextArea();
		scrollPane.setViewportView(display);
		display.setWrapStyleWord(true);
		
		JButton Reliable = new JButton("TRANSFER");
		Reliable.setBounds(270, 400, 150, 25);
		MainPane.add(Reliable);
		
		JCheckBox Unreliable = new JCheckBox("Unreliable TRANSFER");
		Unreliable.setBounds(20, 400, 200, 25);
		MainPane.add(Unreliable);
		
		JLabel Outputl = new JLabel("Output :");
		Outputl.setBounds(20, 140, 50, 14);
		MainPane.add(Outputl);
		
		Reliable.addActionListener(new work(hostt,Sportt,Rportt,filet,display,Unreliable));
	}
	
	private class work implements ActionListener{
		private JTextField ipt;
		private JTextField Sportt;
		private JTextField Rportt;
		private JTextField filet;
		private JTextArea display;
		private JCheckBox r;
		
		public work(JTextField a, JTextField b, JTextField c, JTextField d, JTextArea e, JCheckBox f) {
			this.ipt = a;
			this.Sportt = b;
			this.Rportt = c;
			this.filet = d;
			this.display = e;
			this.r = f;
		}
		
			
		public void actionPerformed(ActionEvent event) {
			String actionCommand = event.getActionCommand();
			if (actionCommand.equals("TRANSFER"))
            {	
				String address = this.ipt.getText().trim();
				
				try {
					InetAddress ip = InetAddress.getByName(address);
					
					int Sport = Integer.parseInt(this.Sportt.getText());
					int Rport = Integer.parseInt(this.Rportt.getText());
					
					String file = this.filet.getText();
					
					Boolean unreliable = r.isSelected();
					
					try {
						SwingWorker<Integer,Integer> worker = new SwingWorker<Integer,Integer>(){

							@Override
							protected Integer doInBackground() throws Exception {
								
								long start = System.currentTimeMillis(), elapsed;
								
								DatagramSocket ds = new DatagramSocket(Rport);
								
								byte[] b = "HandShake".getBytes();
								
								DatagramPacket dp = new DatagramPacket(b,b.length,ip,Sport);
								
								ds.send(dp);
								
								File outputFile = new File(file);
								
								FileOutputStream fos = new FileOutputStream(outputFile);
								
								int c = 0;
								int n = 0;
								int Seq = 0;
								while(true) {
									byte[] databyte = new byte[125];
									
									DatagramPacket dpReceive = new DatagramPacket(databyte,databyte.length);
									
									ds.receive(dpReceive);
									c++;
									
									if(c==10 && unreliable) {
										c = 0 ;
										continue;
									}
										
									byte[] header = new byte[1];
									byte[] main = new byte[dpReceive.getLength()-1];
									
									System.arraycopy(databyte, 0, header, 0, 1);
									System.arraycopy(databyte, 1, main, 0, dpReceive.getLength()-1);
									
									DatagramPacket ackdp = new DatagramPacket(header,header.length,ip,Sport);
									
									ds.send(ackdp);
									
									String s = new String(header);
							
									
									if(!s.equals(String.valueOf(Seq))&&!s.equals("3")) {
										continue;
									}
									
									if(s.equals("3")) {	
										break;
									}
												
									fos.write(main);
									
									publish(n);
									n++;
									
									
									if (Seq == 1) {
										Seq = 0;
									} else {
										Seq = 1;
									}
								}
								
				
								fos.close();
								ds.close();
								
								elapsed = System.currentTimeMillis() - start;
								
								System.out.println("Total Running time : " + elapsed);
								return null;
							}
							
							
							@Override
							protected void process(List<Integer> chunk) {
								for(int i : chunk) {
									display.setText("Packet Number : "+String.valueOf(i));
								}
							}
							
						};
						
						worker.execute();

						//Receiver(ip,Sport,Rport,file,false);
					} catch (Exception e) {
						
						e.printStackTrace();
					}
							
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
								
            }
		}
		

	

	}
}
