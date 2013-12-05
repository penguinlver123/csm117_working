import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;

import javax.bluetooth.*;
import javax.microedition.io.*;

/*** Class that implements an SPP Server which accepts single line of
 * * message from an SPP client and sends a single line of response to the client.
 * */

public class SimpleSPPServer1 {
	//start server
    Socket socketToNodeJS;
    //ObjectOutputStream out;
    PrintWriter out;
	private void startServer() throws IOException{
		//Create a UUID for SPP
		UUID uuid = new UUID("1101", true);
		//Create the service url
        String connectionString = "btspp://localhost:" + uuid +";name=Sample SPP Server";
        
        //open server url
        StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier)Connector.open( connectionString );
        
        //Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect...");
        StreamConnection connection=streamConnNotifier.acceptAndOpen();
        
        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);  
        System.out.println("Remote device address: "+dev.getBluetoothAddress());
        System.out.println("Remote device name: "+dev.getFriendlyName(true));
        
        InputStream inStream=connection.openInputStream();
        //OutputStream outStream=connection.openOutputStream();
        
        try{
        //read string from spp client
        BufferedReader bReader=new BufferedReader(new InputStreamReader(inStream));
		//PrintWriter pWriter=new PrintWriter(new OutputStreamWriter(outStream));
        socketToNodeJS = new Socket("localhost", 8888);
        System.out.println("Connected to localhost in port 8888");
        //out = new ObjectOutputStream(socketToNodeJS.getOutputStream());
        //out = socketToNodeJS.getOutputStream();
        out = new PrintWriter(socketToNodeJS.getOutputStream(), true);
        //out.flush();

        while(true)
        {
        	String lineRead = bReader.readLine();
        	if(lineRead != null)
        	{
        		//System.out.println(lineRead);
        		try{
        			System.out.println(lineRead);
        			out.println(lineRead);
        			//out.writeObject(lineRead);
        			//out.flush();
        		}
        		catch(Exception ioException){
        			ioException.printStackTrace();
        		}
        		
        //send response to spp client
        		//pWriter.write("Response String from SPP Server\r\n");
        	}//}catch(IOException e){
        //	System.out.println("Connection broken: " + e.getMessage() + ".");
        //	streamConnNotifier.close();
        //}
        }
        } catch(Exception ex){
        	System.out.println(ex);
        	
    		//pWriter.flush();  
    		//pWriter.close();
        }
        finally{
			//4: Closing connection
			try{
				out.close();
				socketToNodeJS.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
        
        }
	
	public static void main(String[] args) throws IOException {
		//display local device address and name 
		LocalDevice localDevice = LocalDevice.getLocalDevice();
		System.out.println("Address: "+localDevice.getBluetoothAddress());
		System.out.println("Name: "+localDevice.getFriendlyName());
		
		SimpleSPPServer1 sampleSPPServer=new SimpleSPPServer1();
		sampleSPPServer.startServer(); 
		}
}