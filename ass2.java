import java.net.*;  
import java.io.*;  
import java.util.Arrays;

class fc{  
public static void main(String args[]){  

	//Create a socket
	Socket s=null;


	try{
	
		//Connect to ds-server
		int serverPort = 50000;
		s=new Socket("localhost", serverPort); 
		//System.out.println("Port number: "+s.getPort());  
		
		//Intialise input and output streams associated with socket
		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		DataOutputStream out=new DataOutputStream(s.getOutputStream());  



		//HANDSHAKE
		//Send HELO
		out.write(("HELO\n").getBytes()); 
	
		//Recieve OK 
		String serverMessage = in.readLine();
	
		//Send AUTH
		String username= System.getProperty("user.name");
		out.write(("AUTH"+username+"\n").getBytes());
	
		//Recieve OK
		serverMessage = in.readLine();

		out.write("REDY\n".getBytes());
		String jobState = in.readLine();
		String[] jobInfo = jobState.split(" ",5);
		String capInfo=jobInfo[4];
		String jobID=jobInfo[2];

		//Send gets message
		out.write(("GETS Capable "+capInfo).getBytes()); 
		//Recieve DATA nRecs recSize
		serverMessage = in.readLine();

		//extract nRecs
		String [] arrOfMess = serverMessage.split(" ");
		int nRecs = Integer.valueOf(arrOfMess[1]);

		//Send OK
		out.write(("OK\n").getBytes());

		//Recieve each record of servers
		String server = in.readLine();
		String[] serverInfo = server.split(" "); 
		String serverType = serverInfo[0];
		String serverID = serverInfo[1];

		for(int i=0;i<nRecs-1;i++){
			in.readLine();
		}

		//Ready to 
		out.write("OK\n".getBytes());
		in.readLine();

		//Scheduling job
		out.write(("SCHD "+jobID+" "+serverType+" "+serverID+"\n").getBytes());

		//Send QUIT
		out.write(("QUIT\n").getBytes());
		
		//Recieve QUIT
	 	in.readLine();
		System.out.println("Goodbye and thank you for using this job scheduler :)");
	
	 	//Close the socket
		in.close();
		out.close();
	 	s.close();
		


	}
	catch (UnknownHostException e){
		System.out.println("Sock: "+e.getMessage());
		}
	catch(EOFException e){
		System.out.println("EOF: "+e.getMessage());
	}
	catch(IOException e){
		System.out.println("IO: "+e.getMessage());
	}
	finally {if (s!=null){
		try { s.close();
		}
		catch(IOException e){
			System.out.println("close: "+e.getMessage());
		}
	}}}

}