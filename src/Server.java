import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//this is going to sit on a public server
public class Server extends JFrame{

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output; //from you computer to your friends
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;//set up connection between your cpu and someone else's	
	
	//constructor
	public Server(){
		super("SeaRoth's IM");		//name of window
		userText = new JTextField(); //text field for user
		userText.setEditable(false);//no point unless connected
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage(event.getActionCommand());//hit enter and send
						//set user text to NULL after send
						userText.setText("");						
					}
				}
			);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300,150);
		setVisible(true); //must always make visible
		
	}
	
	//now to set up and run the server
	//need port number,	
	public void startRunning(){		
		//so if something goes wrong we can see what happened
		try{
			server = new ServerSocket(6789,100);//(port number, backlog [queue length])
			
			while (true){
				try{
					waitForConnection();
					setupStreams();
					whileChatting();
					
				}catch(EOFException eofException){ //signals end of stream/connection
					showMessage("\nServer terminated connection! ");
				}finally{
					closeCrap();
				}
			}			
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//this method will wait for the connection
	//then display connection information
	private void waitForConnection() throws IOException{
		showMessage(" Waiting for someone to connect...\n");
		connection = server.accept(); //waiting for socket to connect
		//showMessage can only accept strings, convert to string with gethostname()
		showMessage(" Now connected to " + connection.getInetAddress().getHostName());
	}	
	//get stream to send and receive data
	
	private void setupStreams() throws IOException{
		//creat the pathway that allows up to connect to another computer
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();//good housekeeping.  push the rest of the crap through the stream
		input = new ObjectInputStream(connection.getInputStream());//get the messages the other user is doing
		//can't set up flush here for them because we can't reach over to them and flush for them		
		showMessage("\n Your streams are now setup! \n");				
	}
	
	//code running during conversations
	private void whileChatting() throws IOException{
		String message = " You are now connected! ";
		sendMessage(message);
		ableToType(true);		//now need to let the user to type
		
		do{	//have a conversation			
			try{
				message = (String) input.readObject();
				showMessage("\n " + message);				
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("\n Not known what the user just sent \n");
			}			
		}while(!message.equals("CLIENT - END"));		
	}
	
	//Close the streams and sockets when done chatting	
	public void closeCrap(){
		showMessage("\n Closing connections... \n");
		ableToType(false);
		try{
			//close streams
			output.close();
			input.close();
			//close overall connection / socket
			connection.close();			
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//send message to client
	private void sendMessage(String message){
		
		try{
			output.writeObject("SERVER - " + message);  //send the message through outputstream
			output.flush();
			showMessage("\n SERVER - " + message);
			
		}catch(IOException ioException){
			chatWindow.append("\n ERROR, Can't send that message!\n");
		}
	}
	
	//updates the chat window!!	
	private void showMessage(final String text){
		
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						chatWindow.append(text); //appended to the end of the document
					}
				}
				);
	}
	//Let's the user type Strings into their box
	private void ableToType(final boolean tof){
		
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						userText.setEditable(tof);
					}
				}
				);
	}
	
}













