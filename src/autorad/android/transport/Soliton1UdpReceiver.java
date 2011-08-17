package autorad.android.transport;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import android.os.SystemClock;
import android.util.Log;
import autorad.android.C;


public class Soliton1UdpReceiver implements Runnable {
	
	private boolean running = true;
	
	private static DatagramSocket dsocket;
	
	private DataListener listener;
	private DataStatusChangeListener statusListener;
	protected long lastRecieve = 0;
	
	public Soliton1UdpReceiver(DataListener l) {
		this.listener = l;
	}
	
	public void setDataStatusChangeListener(DataStatusChangeListener listener) {
		statusListener = listener;
	}
	
	public void finish() {
		if (C.D) Log.d(C.TAG, "UdpReciever Stop requested");
		running = false;
		//if (dsocket != null) {
		//	dsocket.disconnect();
		//	dsocket = null;
		//}
		statusListener = null;
		listener = null;
	}
	
	public void run() {
		try {
			Log.i(C.TAG, "UdpReciever Starting...");
			//System.out.println("UdpReciever Starting...");
			int port = 48879;

			// Create a socket to listen on the port.
			if (dsocket == null) {
				dsocket = new DatagramSocket(port);
				dsocket.setReuseAddress(true);
			}

			// Create a buffer to read datagrams into. If a
			// packet is larger than this buffer, the
			// excess will simply be discarded!
			byte[] buffer = new byte[200];
			int[] dataBuffer = new int[20];
			
			// Create a packet to receive data into the buffer
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			if (statusListener != null) {
				statusListener.onDataStatusChange(SourceType.MOTOR_CONTROLLER, DataStatus.WAITING);
			} else {
				Log.w(C.TAG, "Soliton1 UDP Data Status Listener not set");
			}
			new Thread(new Runnable() {
				public void run() {
					while (running) {
						SystemClock.sleep(10000);
						if ((System.currentTimeMillis() - lastRecieve) > 15000) {
							if (statusListener != null) {
								statusListener.onDataStatusChange(SourceType.MOTOR_CONTROLLER, DataStatus.WAITING);
							}
						}
					}
				}
			}).start();
			
			// Now loop forever, waiting to receive packets and printing them.
			while (running) {
				// Wait to receive a datagram
				dsocket.receive(packet);
				if (statusListener != null) {
					statusListener.onDataStatusChange(SourceType.MOTOR_CONTROLLER, DataStatus.RECEIVING);
				}
				lastRecieve = System.currentTimeMillis();
				
		        int	packetLen = packet.getLength();
		        int j = 0;
		        for (int i=0; i<packetLen; i++) {
		        	dataBuffer[j++] = ((buffer[i] & 0xFF) ) + ((buffer[++i] & 0xFF) << 8);
		        }
		        
		        if (listener != null) {
		        	listener.onData(dataBuffer);
		        }
		        
		        // Reset the length of the packet before reusing it.
		        packet.setLength(buffer.length);
		        
		        Arrays.fill(dataBuffer, (char)0);
		        Arrays.fill(buffer, (byte)0);
		        
		        //SystemClock.sleep(100);
		    }	
			if (statusListener != null) {
				statusListener.onDataStatusChange(SourceType.MOTOR_CONTROLLER, DataStatus.STOPPED);
			}
		    if (C.D) Log.d(C.TAG, "UdpReciever Stopped");
		} catch (Exception e) {
			if (statusListener != null) {
				statusListener.onDataStatusChange(SourceType.MOTOR_CONTROLLER, DataStatus.STOPPED);
			}
			Log.e(C.TAG, e.getMessage(), e);
	    }
	}
	
	public static void main(String[] args) {
		Soliton1UdpReceiver rec = new Soliton1UdpReceiver(null);
		new Thread(rec).start();
	}
}
