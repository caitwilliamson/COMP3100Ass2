import java.net.*;
import java.util.Arrays;
import java.io.*;  

class TestClient{  
    public static void main(String args[]){  

        //Create a socket
        Socket s=null;


        try{
        
            //Connect to ds-server
            int serverPort = 50000;
            s=new Socket("localhost", serverPort); 
            
            //Intialise input and output streams associated with socket
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream out=new DataOutputStream(s.getOutputStream());  



            //HANDSHAKE
            //Send HELO
            out.write("HELO\n".getBytes()); 
        
            //Recieve OK 
            String serverMessage = in.readLine();
        
            //Send AUTH
            String username= System.getProperty("user.name");
            out.write(("AUTH "+username+"\n").getBytes());

            //Recieve OK
            serverMessage = in.readLine();
            String jobState="";
            out.write("REDY\n".getBytes());
            jobState=in.readLine();

            while(!"NONE".equals(jobState)){
                String[] jobInfo = jobState.split(" ",5);
                String jobCommand=jobInfo[0];

                if("JOBN".equals(jobCommand)){
                    String capInfo=jobInfo[4];
                    String jobID=jobInfo[2];
                    //Find available servers
                    out.write(("GETS Avail "+ capInfo + "\n").getBytes());
                    boolean availableServers=true;
                    //Recieve DATA nRecs recSize
                    serverMessage = in.readLine();
                    String [] arrOfMess = serverMessage.split(" ");
                    int nRecs = Integer.parseInt(arrOfMess[1]);
                    
                    //if no available servers
                    if(nRecs==0){
                        availableServers=false;
                        out.write("OK\n".getBytes());
                        serverMessage = in.readLine();
                        
                        //Find capable servers
                        out.write(("GETS Capable "+ capInfo + "\n").getBytes()); 
                        //Recieve DATA nRecs recSize
                        serverMessage = in.readLine();
                        arrOfMess = serverMessage.split(" ");
                        nRecs = Integer.parseInt(arrOfMess[1]);
                    }
            
                    //Send OK
                    out.write("OK\n".getBytes());
                    boolean bestServerFound=false;
                    boolean serverNoJobsFound=false;
                    String [] servers = new String[nRecs];
                    int serverNum=0;
                    String serverType = "";
                    String serverID = "";

                    //reading in servers
                    for(int i=0; i<nRecs; i++){
                        servers[i]=in.readLine();
                    }

                    
                    int globalBestWJobs=0;
                    int globalBestRJobs=0;
                    //loop through to find the most suitable server 
                    for(int i=0; i<nRecs; i++){
                        //if available servers, choose first one
                        if(availableServers){
                            serverNum=0;
                            break;
                        }
                        String[] serverInfo = servers[i].split(" ");
                        int serverWJobs = Integer.parseInt(serverInfo[7]);
                        int serverRJobs = Integer.parseInt(serverInfo[8]);
                        
                        //clearing memory
		                Arrays.fill(serverInfo, null);
                        
                        //initialise global bests for first server
                        if(i==0){
                            globalBestWJobs=serverWJobs;
                            globalBestRJobs=serverRJobs;
                        }
                        
                        // Find the best server :

                        if(serverWJobs==0 && serverRJobs==0 && !bestServerFound){
                            bestServerFound=true;
                            serverNum=i;
                            break;
                        }

                        if(serverWJobs==0 && !serverNoJobsFound){
                            serverNoJobsFound=true;
                            globalBestWJobs=0;
                            serverNum=i;
                        }

                        if(serverRJobs<globalBestRJobs && serverWJobs<=globalBestWJobs){
                            globalBestRJobs=serverRJobs;
                            globalBestWJobs=serverWJobs;
                            serverNum=i;
                        }

                        if(serverWJobs<globalBestWJobs){
                            globalBestWJobs=serverWJobs;
                            serverNum=i;
                        }

                        
                    }

                    // Abstracting server info from selected server
                    String[] bestServerInfo = servers[serverNum].split(" ");
                    serverType = bestServerInfo[0];
                    serverID = bestServerInfo[1];
                    //clearing memory
		            Arrays.fill(servers, null);
		            Arrays.fill(bestServerInfo, null);

                    
                    //Ready to
                    out.write("OK\n".getBytes());
                    in.readLine();

                    //Scheduling job
                    out.write(("SCHD "+jobID+" "+serverType+" "+serverID+"\n").getBytes());
                    in.readLine();
                	//clearing memory
		            Arrays.fill(jobInfo, null);

                

                }
                else{
                    //if JCPL do nothing
                }


                //REDY for next job and reading in
                out.write("REDY\n".getBytes());
                jobState = in.readLine();


            }


            //Send QUIT
            out.write("QUIT\n".getBytes());
            
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