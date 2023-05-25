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
            out.write(("AUTH "+username+"\n").getBytes());

            //Recieve OK
            serverMessage = in.readLine();
            String jobState="";
            out.write("REDY\n".getBytes());
            jobState=in.readLine();
            int RR=0;

            while(!jobState.equals("NONE")){
                System.out.println("entering First loop");
                System.out.println(jobState);
                String[] jobInfo = jobState.split(" ",5);
                String jobCommand=jobInfo[0];

                if(jobCommand.equals("JOBN")){
                    String capInfo=jobInfo[4];
                    String jobID=jobInfo[2];
                    System.out.println("job is JOBN");
                    //Send gets message
                    out.write(("GETS Capable "+ capInfo + "\n").getBytes()); 
                    //Recieve DATA nRecs recSize
                    String dataMessage = in.readLine();
                    System.out.println(dataMessage);

                    //extract nRecs
                    String [] arrOfMess = dataMessage.split(" ");
                    System.out.println(Arrays.toString(arrOfMess));
                    int nRecs = Integer.valueOf(arrOfMess[1]);
            
                    //Send OK
                    out.write(("OK\n").getBytes());
                    boolean bestServerFound=false;
                    String [] servers = new String[nRecs];
                    int serverNum=0;
                    String serverType = "";
                    String serverID = "";

                    for(int i=0; i<nRecs; i++){
                        servers[i]=in.readLine();
                    }

                    for(int i=0; i<nRecs; i++){
                        String[] serverInfo = servers[i].split(" ");
                        serverType = serverInfo[0];
                        serverID = serverInfo[1];
                        int serverWJobs = Integer.valueOf(serverInfo[7]);
                        int serverRJobs = Integer.valueOf(serverInfo[8]);

                        if(serverWJobs==0 && serverRJobs==0 && !bestServerFound){
                            bestServerFound=true;
                            serverNum=i;
                            
                        }
                    }
                    if(!bestServerFound){
                        if(RR>=nRecs){
                            RR=0;
                        }
                        serverNum=0+RR;
                        RR++;
                    }

                    String[] serverInfo = servers[serverNum].split(" ");
                    serverType = serverInfo[0];
                    serverID = serverInfo[1];

                
                    
                    // while(!bestServerFound){
                    //     System.out.println("entering serverReady loop");
                    //     servers[serverNum]=in.readLine();
                    //     String[] serverInfo = servers[serverNum].split(" ");
                    //     //System.out.println(serverInfo);
                    //     serverType = serverInfo[0];
                    //     serverID = serverInfo[1];
                    //     int serverWJobs = Integer.valueOf(serverInfo[7]);
                    //     int serverRJobs = Integer.valueOf(serverInfo[8]);

                    //     if(serverWJobs==0 && serverRJobs==0){
                    //         bestServerFound=true;
                    //         System.out.println(serverType);
                    //     }

                    //     if(serverNum==nRecs-1){
                    //         bestServerFound=true;
                    //         System.out.println("reached end of servers");
                    //     }
                    //     serverNum++;

                    // }


                    
                    //Ready to
                    out.write("OK\n".getBytes());
                    in.readLine();

                    //Scheduling job
                    out.write(("SCHD "+jobID+" "+serverType+" "+serverID+"\n").getBytes());
                    in.readLine();

                

                }
                else{
                    System.out.println("JCPL");
                }


                //REDY for next job and reading in
                out.write("REDY\n".getBytes());
                jobState = in.readLine();
                System.out.println("job state is:" + jobState);


            }


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