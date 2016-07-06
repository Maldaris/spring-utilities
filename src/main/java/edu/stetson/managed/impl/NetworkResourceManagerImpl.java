package edu.stetson.managed.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stetson.managed.NetworkResourceManager;
/**
 * Allows for network-triggered reloads of resources into memory.
 * Uses JSON objects to communicate, and runs concurrently in a non-blocking
 * fashion to the main HTTP Server thread.
 * @author slfitzge
 *
 */
public abstract class NetworkResourceManagerImpl implements
		NetworkResourceManager {

	Logger log = LoggerFactory.getLogger(NetworkResourceManagerImpl.class);

	private class Tasklet implements Runnable {

		public String triggerPassword = "DEFAULT_PASSWORD";

		public Integer port;

		Logger log = LoggerFactory.getLogger(Tasklet.class);

		public void run() {
			ServerSocket server;
			try {
				server = new ServerSocket(this.port);
			} catch (IOException e) {
				log.error(e.getLocalizedMessage());
				return;
			}

			log.debug(this.getClass().getCanonicalName()
					+ " started listener with password " + triggerPassword);
			log.debug("server is listening on port " + port);

			while (!shouldStop) {
				Socket sock;
				BufferedReader in;
				PrintWriter out;
				try {
					sock = server.accept();
					in = new BufferedReader(new InputStreamReader(
							sock.getInputStream()));
					out = new PrintWriter(sock.getOutputStream(), true);
				} catch (IOException e) {
					log.error(e.getLocalizedMessage());
					break;
				}
				JSONObject msg;
				try {
					msg = new JSONObject(in.readLine());
				} catch (IOException | JSONException e) {
					log.error(e.getLocalizedMessage());
					continue;
				}
				if (msg.getString("token").equals(this.triggerPassword)) {
					if (msg.has("reloadAll") && msg.getBoolean("reloadAll")) {
						reload();
					} else if (msg.has("reload")) {
						JSONArray arr = msg.getJSONArray("reload");
						for (int i = 0; i < arr.length(); i++) {
							reloadResourceByName(arr.getString(i));
						}
					}
				}
				try {
					in.close();
				} catch (IOException e) {
					log.error(e.getLocalizedMessage());
					break;
				}
				out.close();
			}
			try {
				server.close();
			} catch (IOException e) {

				log.error(e.getLocalizedMessage());
				return;
			}
		}
	}

	private Thread listener;

	private Boolean shouldStop = false;

	private Tasklet tasklet = new Tasklet();

	@Override
	public boolean startListener() {
		if (listener.isAlive())
			return false;
		try {
			listener = new Thread(this.tasklet);
			listener.start();
		} catch (Exception e) {

			return false;
		}
		return true;
	}

	@Override
	public void stopListener() {
		if (!listener.isAlive())
			return;
		try {
			shouldStop = true;
			listener.join(150);
		} catch (InterruptedException e) {
			log.error(e.getLocalizedMessage());
			return;
		}

	}

	@Override
	public boolean setPort(Integer i) {
		if (listener.isAlive()) {
			return false;
		} else {
			tasklet.port = i;
			return true;
		}
	}

}
