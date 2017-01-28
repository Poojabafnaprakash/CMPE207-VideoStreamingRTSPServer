/***********************************************
 * CMPE 207, Spring 2016                       *
 * TEAM 12                                     *
 **********************************************/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class VideoStreamingClient  {

  private static Socket SocketOfClient = null;
  private static PrintStream os = null;
  private static DataInputStream is = null;
  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  
  public static void main(String[] args) throws NumberFormatException, IOException {
    BufferedReader br =new BufferedReader(new InputStreamReader(System.in));
	System.out.println("Enter the port number:");
    int i=0;
	int ClientPORT = 0;
    int portNumber = Integer.parseInt(br.readLine());
    System.out.print("Enter the host IP Address: ");
    String host = br.readLine();
    if (args.length < 2) 
    {
      System.out.println("Java VideoStreamingClient <host> <portNumber>\n" + "Connected to Server=" + host + ", portNumber=" + portNumber);
   
    }
    else 
    {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    try 
    { 
      long start= System.currentTimeMillis();
      SocketOfClient = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(SocketOfClient.getOutputStream());
      is = new DataInputStream(SocketOfClient.getInputStream());   
      InputStream is = SocketOfClient.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br1 = new BufferedReader(isr);
      String message = br1.readLine();
      System.out.println("Receiving Port Of Client : " +message);
      SocketOfClient.close();
      long end =System.currentTimeMillis();
      long diff =end-start;
      System.out.println("Server response time in milliseconds: "+diff);
      System.out.println();
      ClientPORT = Integer.parseInt(message);  
    } 
    catch (UnknownHostException e) 
    {
      System.err.println("Don't know about host " + host);
    }
    catch (IOException e) 
    {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }

    if (SocketOfClient != null && os != null && is != null) {
      try {
    	  Frame f= new Frame();  
    	  
  	    DatagramSocket DataSocket = null;
  	    DatagramPacket inPacket = null;
  	    byte[] inBuf = new byte[65000];
  	    try {		
  	      DataSocket = new DatagramSocket(ClientPORT);
  	      InetAddress address = InetAddress.getByName("127.0.0.1");
  	      JFrame frame = new JFrame("Display Image");
  	      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  	     JPanel panel = (JPanel)frame.getContentPane();
  	     JLabel label = new JLabel();
  	      while (true) {
  	        inPacket = new DatagramPacket(inBuf, inBuf.length);
  	        DataSocket.receive(inPacket);
  	        i++;
  	        RTPDepacketizer rtp_depacketizer = new RTPDepacketizer(inPacket.getData(), inPacket.getLength());
  	        String msg = new String(inBuf, 0, inPacket.getLength());
  	        
  	        System.out.println("Length of Packet From " + inPacket.getAddress() + ":" + inPacket.getLength());
  	        System.out.println("Data of Packet From " + inPacket.getAddress() + ":"+ inPacket.getData());
  	        System.out.println("Number of Packets received: "+i);
  	        System.out.println();
  	        int payload_length = rtp_depacketizer.getpayload_length();
  	        byte [] payload = new byte[payload_length];
  	        rtp_depacketizer.getpayload(payload); 
    
  	    Toolkit toolkit = Toolkit.getDefaultToolkit();
  	    Image image = toolkit.createImage(payload, 0, payload.length);
  	   
  	    
  	    f.icon = new ImageIcon(image);
  	    f.iconLabel.setIcon(f.icon); 
  	   }
  	       
  } catch (IOException ioe) {
  	      System.out.println(ioe);
  	    }
  	  
    	  while (!closed) {
          os.println(inputLine.readLine().trim());
        }
        
        os.close();
        is.close();
        SocketOfClient.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

}


class Frame
{

JFrame f = new JFrame("Client");
JPanel mainPanel = new JPanel();
JPanel buttonPanel = new JPanel();
JLabel iconLabel = new JLabel();
ImageIcon icon;
Frame()
	{
 
		buttonPanel.setLayout(new GridLayout(1,0));
		iconLabel.setIcon(null);
 
		mainPanel.setLayout(null);
		mainPanel.add(iconLabel);
		mainPanel.add(buttonPanel);
		iconLabel.setBounds(0,0,380,280);
		buttonPanel.setBounds(0,280,380,50);
		iconLabel.setBackground(Color.pink);
		iconLabel.setOpaque(true);
		f.getContentPane().add(mainPanel, BorderLayout.CENTER);
		f.setSize(new Dimension(390,370));
		f.setVisible(true);
	}
}



class RTPDepacketizer {

	static int SizeOfHeader = 12;
	public int Version;
	public int Padding;
	public int Extension;
	public int CC;
	public int Marker;
	public int PayloadType;
	public int SequenceNumber;
	public int TimeStamp;
	public int SyncSource;
	public byte[] header;
	public int payload_size;
	public byte[] payload;


public RTPDepacketizer(int PType, int Framenb, int Time, byte[] data,
int data_length) {

	Version = 2;
	Padding = 0;
	Extension = 0;
	CC = 0;
	Marker = 0;
	SyncSource = 0;
	SequenceNumber = Framenb;
	TimeStamp = Time;
	PayloadType = PType;
	header = new byte[SizeOfHeader];

	header[1] = (byte) ((Marker << 7) | PayloadType);
	header[2] = (byte) (SequenceNumber >> 8);
	header[3] = (byte) (SequenceNumber);

	for (int i = 0; i < 4; i++)
		header[7 - i] = (byte) (TimeStamp >> (8 * i));

	for (int i = 0; i < 4; i++)
		header[11 - i] = (byte) (SyncSource >> (8 * i));

	payload_size = data_length;
	payload = new byte[data_length];
	payload = data;

}


public RTPDepacketizer(byte[] packet, int packet_size) {

	Version = 2;
	Padding = 0;
	Extension = 0;
	CC = 0;
	Marker = 0;
	SyncSource = 0;


	if (packet_size >= SizeOfHeader) {
		header = new byte[SizeOfHeader];
		for (int i = 0; i < SizeOfHeader; i++)
			header[i] = packet[i];


		payload_size = packet_size - SizeOfHeader;
		payload = new byte[payload_size];
		for (int i = SizeOfHeader; i < packet_size; i++)
			payload[i - SizeOfHeader] = packet[i];


		PayloadType = header[1] & 127;
SequenceNumber = unsigned_int(header[3]) + 256
* unsigned_int(header[2]);
TimeStamp = unsigned_int(header[7]) + 256 * unsigned_int(header[6])
+ 65536 * unsigned_int(header[5]) + 16777216
* unsigned_int(header[4]);
}
}


public int getpayload(byte[] data) {

for (int i = 0; i < payload_size; i++)
data[i] = payload[i];
return (payload_size);

}

public int getpayload_length() {
return (payload_size);
}


public int getlength() {
return (payload_size + SizeOfHeader);
}


public int getpacket(byte[] packet) {

	for (int i = 0; i < SizeOfHeader; i++)
		packet[i] = header[i];
	for (int i = 0; i < payload_size; i++)
		packet[i + SizeOfHeader] = payload[i];
	return (payload_size + SizeOfHeader);
}


public int gettimestamp() {
	return (TimeStamp);
}


public int getsequencenumber() {
return (SequenceNumber);
}


public int getpayloadtype() {
	return (PayloadType);
}


public void printheader() {

	for (int i = 0; i < (SizeOfHeader - 4); i++) {
		for (int j = 7; j >= 0; j--)
			if (((1 << j) & header[i]) != 0)
				System.out.print("1");
			else
				System.out.print("0");
		System.out.print(" ");
	}

	System.out.println();
}


static int unsigned_int(int nb) {
	if (nb >= 0)
		return (nb);
	else
		return (256 + nb);
	}

}

