package edu.stetson.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;

public class NetworkSqlStatementLoader extends SqlStatementLoader {

	private String lastPattern = "";
	
	private static final int DEFAULT_PORT = 9090; 
	
	private int port = DEFAULT_PORT;

	private Thread sockThread;
	
	private final Object mapLock = new Object();
	
	private class Tasklet implements Runnable {
		
		public String triggerPassword = "DEFAULT_PASSWORD";
		
		public void run(){
			ServerSocket server;
			try {
				server = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			while(true){
				Socket sock;
				try {
					sock = server.accept();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				BufferedReader in;
				PrintWriter out;
				try {
					 in = new BufferedReader(
							new InputStreamReader(sock.getInputStream()));
					 out = new PrintWriter(sock.getOutputStream(), true);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				String pw;
				try {
					pw = in.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				if(pw.equals(this.triggerPassword)){
					int	i = loadStatementsByPattern(lastPattern);
					out.write(""+i+" statements loaded");
				}				
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				out.close();		
				continue;
			}
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public NetworkSqlStatementLoader(ClassLoader contextCL) {
		super(contextCL);
		sockThread = new Thread(new Tasklet());
		sockThread.run();
	}

	public NetworkSqlStatementLoader(ClassLoader contextCL, Logger log) {
		super(contextCL, log);
		sockThread = new Thread(new Tasklet());
		sockThread.run();
	}
	
	public NetworkSqlStatementLoader(ClassLoader contextCL, Logger log, int port, String triggerPassword) {
		super(contextCL, log);
		this.port = port;
		Tasklet t = new Tasklet();
		t.triggerPassword = triggerPassword;
		sockThread = new Thread(t);
		sockThread.run();
	}
	@Override
	public int loadStatementsByPattern(String s){
		int i;
		synchronized(mapLock){
			i = super.loadStatementsByPattern(s);
			this.lastPattern = s;
		}
		return i;
	}

}
