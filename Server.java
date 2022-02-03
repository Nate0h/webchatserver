/* Notes: 
 * This code is modified from the original to work with 
 * the CS 352 chat client:
 *
 * 1. added args to allow for a command line to the port 
 * 2. Added 200 OK code to the sendResponse near line 77
 * 3. Changed default file name in getFilePath method to ./ from www 
 */ 

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Read the full article https://dev.to/mateuszjarzyna/build-your-own-http-server-in-java-in-less-than-one-hour-only-get-method-2k02
public class Server {

    public static String user;
    public static  ArrayList<String> chatlog = new ArrayList<>();
    public static int test = 0;
    public static int tracker = 8;
    public static int valid; 
    public static ArrayList<String> html4 = new ArrayList<>();
    public static void main( String[] args ) throws Exception {

	if (args.length != 1) 
        {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }
        //create server socket given port number
        int portNumber = Integer.parseInt(args[0]);
	
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    handleClient(client);
                }
            }
        }
    }

    private static void handleClient(Socket client) throws IOException {
	test++;
	/*	if(test > 4){
	    test = 1;
	    }*/
	BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        
	StringBuilder requestBuilder = new StringBuilder();
        String request;
       

	while(br.ready()){
	    char c = (char)br.read();
	    requestBuilder.append(c);
	}
	
        request = requestBuilder.toString();
	String password = "";
	String message = "";
	
         
	String[] requestsLines = request.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        String method = requestLine[0];
        String path = requestLine[1];
	System.out.println(path);
        String version = requestLine[2];
        String host = requestsLines[1].split(" ")[1];

	// build the reponse here 
        List<String> headers = new ArrayList<>();
        for (int h = 2; h < requestsLines.length; h++) {
            String header = requestsLines[h];
            headers.add(header);
        }

        String accessLog = String.format("Client %s, method %s, path %s, version %s, host %s, headers %s",
                client.toString(), method, path, version, host, headers.toString());
	        System.out.println(accessLog);
	
	
	if(request.substring(0,4).equals("POST")){
	   
	String info = headers.get(headers.size() - 1);

	if(info.charAt(0) == 'u'){
	int p1 = info.indexOf("=") + 1;
        int p2 = info.indexOf("&");
        int p3 = info.lastIndexOf("=") + 1;
        int p4 = info.length();
        user = info.substring(p1,p2);
        password = info.substring(p3,p4);
	}
	if(info.charAt(0) == 'm'){
	    int t1 = info.indexOf("=") + 1;
	    int t2 = info.length();
	    message = info.substring(t1,t2);
	    String input = user + ": " + message;
	    chatlog.add(input);
	}

      
	if(test == 2){
	File file = new File("credentials.txt");
	
        BufferedReader br2 = new BufferedReader(new FileReader(file));
        String st;
        while ((st = br2.readLine()) != null){
	    String[] cred = st.split(",");
	    if(cred[0].equals(user) && cred[1].equals(password) ){
		valid = 1;
		}
	  
	}
	}
	}
	
        System.out.println(test);
	if(test == 3){
	File file2 = new File("chat/chat.html");
        BufferedReader br3 = new BufferedReader(new FileReader(file2));
	String line;
	int row = 0;
	while ((line = br3.readLine()) != null){
	      if(row == 7){
		for(String str: chatlog){
		line = "<p>" + str + "</p>";
		System.out.println(line);
		html4.add(line);
		row++;
		}
		continue;
	      }
	    row++;
	    html4.add(line);
	    System.out.println(line);
            
	}
	}

	if(test > 3){
	    if(test % 3 == 0){
            String str = chatlog.get(chatlog.size() - 1);
            String mew = "<p>" + str  + "</p>";
	    System.out.println(mew);
            html4.add(tracker, mew);
	    System.out.println("----------------------------");
	    for(String lin: html4){
		System.out.println(lin);
	    }
	    }
	}

	
       Path filePath = getFilePath(path);
        if (Files.exists(filePath)) {
            // file exist
            String contentType = guessContentType(filePath);
            sendResponse(client, "HTTP/1.1 200 OK", contentType, Files.readAllBytes(filePath));
        } else {
            // 404
            byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
            sendResponse(client, "404 Not Found", "text/html", notFoundContent);
        }
    }

    private static void sendResponse(Socket client, String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 200 OK" + status + "\r\n").getBytes());       
        clientOutput.write(("Content: " + contentType + "\r\n").getBytes());
	if(valid == 1){
	clientOutput.write(("Set-Cookie:" + "userID:" + user ).getBytes());
	}
        clientOutput.write("\r\n".getBytes());
       
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());

	
	if(test % 4 == 0){
	for(String str: html4){
	 clientOutput.write((str + "\n").getBytes());
	}
	}
        clientOutput.flush();
        client.close();
    }

     private static Path getFilePath(String path) {
	 if(path.equals("/login/")){
	     path = "login/login.html";
	 }
	 if(path.equals("/chat/")){
	     path = "chat/chat.html";
	 }
	 if(path.equals("/login/login.html")){
	     path = "/login/login.html";
	 }
	 if(path.equals("chat/chat.html")){
	     path = "chat/chat.html";
	 }
        return Paths.get("./", path);
	}

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

}
