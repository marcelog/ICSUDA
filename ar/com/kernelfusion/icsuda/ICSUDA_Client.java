//
// ICSUDA: (I) (C)an (S)ee (Y)ou, (D)umbass
//
// Secure P2P messaging system.
//
// Author:
// 	Marcelo Gornstein
//		CopyRight(c) 2003 All rights reserved.
//
// Comments, suggestions, bug reports, patches, beer to:
//
// 	Marcelo Gornstein
// 	marcelog@gmail.com
//
//
// 04/2003
//
//
// License terms:
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
// 
// Redistributions of source code must retain the above copyright notice, this list
// of conditions and the following disclaimer. 
//
// Redistributions in binary form must reproduce the above copyright notice, this
// list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution. 
//
// All advertising materials mentioning features or use of this software must display
// the following acknowledgement: 
//
// This product includes software developed by Marcelo Gornstein
//
// Neither the name of the author(s) nor the names of its contributors may be used to
// endorse or promote products derived from this software without specific prior
// written permission. 
//
// THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
// SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
// BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
// ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
package ar.com.kernelfusion.icsuda;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;

public class ICSUDA_Client
{
	private ICSUDA_Logfile log;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private SSLSocket socket;
	private SocketFactory socketFactory;
	private ICSUDA_Contact myContact;
	private ArrayList myContacts = new ArrayList();
//	private ICSUDA_Contact[] myContacts;
	private boolean loggedIn;
	private boolean connected;
	private ClientResponseHandler rHandler;
	private int thid = 1;
	private ICSUDA_ClientInterface client;

	//
	// This is thread in charge of receiving packets from server. Every synch-event will have to register here first
	// in order to get responses.
	//
	class ClientResponseHandler extends Thread
	{
		private ArrayList registeredClients;
		private ICSUDA_Packet response;
			
		public synchronized void register(Thread t)
		{
			registeredClients.add(t);
			return;
		}

		public synchronized void unregister(Thread t)
		{
			registeredClients.remove(registeredClients.indexOf(t));
			return;
		}
	
		public void run()
		{
			//log.Log("ClientResponseHandler: launched!");
			while(connected)
			{
				try
				{
//					log.Log("Reading from server...");
					response = (ICSUDA_Packet)in.readObject();
//					log.Log("Got packet for thread " + response.thid);
					if(response.thid < 0)
						continue;
					else if(response.thid == 0)
					{
						switch(response.code)
						{
							case ICSUDA_Packet.CODE_AUTH:
								break;
							case ICSUDA_Packet.CODE_CONTACT:
								switch(response.subcode)
								{
									case ICSUDA_Packet.SUBCODE_ADD:
										break;
									case ICSUDA_Packet.SUBCODE_DEL:
										break;
									case ICSUDA_Packet.SUBCODE_GET:
										break;
									case ICSUDA_Packet.SUBCODE_ACK:
										break;
									case ICSUDA_Packet.SUBCODE_ONLINE:
//										log.Log(response.c.getUIN() + " online");
										client.contactOnline(response.c.getUIN());
										break;
									case ICSUDA_Packet.SUBCODE_OFFLINE:
//										log.Log(response.c.getUIN() + " offline");
										client.contactOffline(response.c.getUIN());
										break;
									default:
										break;
								}
								break;
							case ICSUDA_Packet.CODE_MESSAGE:
								switch(response.subcode)
								{
									case ICSUDA_Packet.SUBCODE_ADD:
										client.incomingMessage(response.m);
//										log.Log(response.m.getFrom().getName() + " says " + response.m.getBody());
										break;
									case ICSUDA_Packet.SUBCODE_DEL:
										break;
									case ICSUDA_Packet.SUBCODE_GET:
										break;
									case ICSUDA_Packet.SUBCODE_ACK:
										break;
									default:
										break;
								}
								break;
							// Illegal code.
							default:
//								break;
						}
					} else {
						int j = registeredClients.size();
						for(int i = 0; i < j; i++)
						{
							ClientThread t = (ClientThread)registeredClients.get(i);
							// guilty? ok, give response and wake up the sucker.
							if(t.id == response.thid)
							{
								t.p = response;
								t.running = false;
//								log.Log("Thread " + t.id + " woke up");
								//t.wakeup();
							}
						}
					}
				} catch (Exception e) {
					response = null;
//					log.Log("ClientResponseHandler: Could not read from server: " + e.getClass() + ": " + e.getMessage());
					close();
					int j = registeredClients.size();
					for(int i = 0; i < j; i++)
					{
						ClientThread t = (ClientThread)registeredClients.get(i);
						t.running = false;
						//t.p = response;
						t.wakeup();
					}
				}
			}
			return;
		}

		ClientResponseHandler()
		{
			registeredClients = new ArrayList();
		}

		// The finalize() method is called by the Java virtual machine (JVM)* before
		// the program exits to give the program a chance to clean up and release
		// resources. Multi-threaded programs should close all Files and Sockets they use
		// before exiting so they do not face resource starvation. The call to server.close()
		// in the finalize() method closes the Socket connection used by each thread in
		//this program. 
		protected void finalize()
		{
			//Objects created in run method are finalized when
			//program terminates and thread exits
			//close();
			//log.Log("ClientResponseHandler: Finished.");
		}
	}

	//
	// This kind of thread will be created for every local request, will register to the thread above and
	// sleep until getting a response.
	//
	class ClientThread extends Thread
	{
		public int id;
		public ICSUDA_Packet p;
		private ICSUDA_Packet p2;
		public boolean running = true;
		private int mytime = 0;

		public synchronized void wakeup() { notifyAll(); }

		public void run()
		{
			try
			{
				sendPacket(p2);
				while(running)
				{
					sleep(200);
					mytime += 200;
//					synchronized(this) { wait(); }
					if(mytime > 60000)
						running = false;
				}
			} catch(Exception e) {
			} finally {
			}
			rHandler.unregister(this);
			return;
		}

		ClientThread(ICSUDA_Packet pa)
		{
			p = null;
			p2 = pa;
			p2.thid = id = getNextThreadId();
			rHandler.register(this);
		}

		// The finalize() method is called by the Java virtual machine (JVM)* before
		// the program exits to give the program a chance to clean up and release
		// resources. Multi-threaded programs should close all Files and Sockets they use
		// before exiting so they do not face resource starvation. The call to server.close()
		// in the finalize() method closes the Socket connection used by each thread in
		//this program. 
		protected void finalize()
		{
			//Objects created in run method are finalized when
			//program terminates and thread exits
			//close();
			//log.Log("Finished.");
		}
	}

	private synchronized int getNextThreadId() { thid++; return thid;}

	private synchronized void sendPacket(ICSUDA_Packet p) throws Exception
	{
		if((out != null) && (in != null))
		{
			out.writeObject(p);
			out.flush();
		}
		return;
	}

	public boolean online() { return loggedIn; }
	public boolean connected() { return connected; }
	public long getUIN() { if(myContact != null) return myContact.getUIN(); else return 0; }
	public String getName() { if(myContact != null) return myContact.getName(); else return null; }

	public ICSUDA_Contact getContact(String name)
	{
		for(int i = 0; i < myContacts.size(); i++)
		{
			ICSUDA_Contact f = (ICSUDA_Contact)myContacts.get(i);
			if(f.getName().equals(name))
				return f;
		}
		return null;
	}

	public ArrayList getContacts(boolean on)
	{
		ArrayList r = new ArrayList();
		for(int i = 0; i < myContacts.size(); i++)
		{
			ICSUDA_Contact f = (ICSUDA_Contact)myContacts.get(i);
			if(on)
			{
				if(f.isOnline())
					r.add(f);
			} else {
				if(!f.isOnline())
					r.add(f);
			}
		}
		return r;
	}

	public ICSUDA_Packet sendMessage(long u, String s, String b) throws Exception
	{
		ClientThread t;
		ICSUDA_Packet r = new ICSUDA_Packet();

		if(!loggedIn)
			return null;

		r.code = ICSUDA_Packet.CODE_MESSAGE;
		r.subcode = ICSUDA_Packet.SUBCODE_ADD;
		r.m = new ICSUDA_Message(myContact, new ICSUDA_Contact(u, ""), s, b);

		t = new ClientThread(r);
		t.start();

		while(t.isAlive()) Thread.sleep(200);
		r = t.p;

		return r;
	}

	public void login(long u, String p) throws Exception
	{
		ClientThread t;
		ICSUDA_Packet r = new ICSUDA_Packet();
		ICSUDA_Contact c = new ICSUDA_Contact(u, "", p);

		r.code = ICSUDA_Packet.CODE_AUTH;
		r.subcode = ICSUDA_Packet.SUBCODE_LOGIN;
		r.c = c;
		loggedIn = false;

		t = new ClientThread(r);
		t.start();
//		t.join();
		while(t.isAlive()) Thread.sleep(200);
		r = t.p;

		if(r == null)
			throw new Exception("Timeout.");
		if(r.errorcode != ICSUDA_Packet.E_SUCCESS)
			throw new Exception("Access denied: Invalid uin or password");

		loggedIn = true;
		myContact = new ICSUDA_Contact(r.c);
		myContacts = new ArrayList(r.contacts);
	}

	public void logout()
	{
		ClientThread t;
		ICSUDA_Packet r = new ICSUDA_Packet();
		r.code = ICSUDA_Packet.CODE_AUTH;
		r.subcode = ICSUDA_Packet.SUBCODE_LOGOUT;

		t = new ClientThread(r);
		t.start();
		try
		{
		while(t.isAlive()) Thread.sleep(200);
//			t.join();
		} catch(Exception e) {
		}

		loggedIn = connected = false;
		close();
		return;
	}

	public void connect(String host, int port) throws Exception
	{
		// Try to connect to server.
		connected = false;
		socketFactory = SSLSocketFactory.getDefault();
		socket = (SSLSocket)socketFactory.createSocket(host, port);
		socket.setKeepAlive(true);
		// Set which protocols and cipher suites we'll use.
		String[] foo = new String[] {"SSL_DH_anon_WITH_3DES_EDE_CBC_SHA"};
		socket.setEnabledCipherSuites(foo);
		foo = new String[] {"SSLv3"};
		socket.setEnabledProtocols(foo);
		//	> I am using ObjectInputStream and ObjectOutputStream in socket
		//	> networking. When I use the code in client case 2) below, everything
		//	> is okay. But If I use the code in case 1), both a server and a
		//	> client got stuck. The only difference between case1 and case2 is the
		//	> order of getting streams from socket. I am curious why client case 1
		//	> doesn't work.
		//	
		//	When you create an ObjectInputStream, it blocks while it tries to read
		//	a header from the underlying SocketInputStream. When you create the
		//	corresponding ObjectOutputStream at the far end, it writes the header
		//	that the ObjectInputStream is waiting for, and both are able to
		//	continue.
		//	
		//	If you try to create both ObjectInputStreams first, each end of the
		//	connection is waiting for the other to complete before proceeding,
		//	i.e. deadlock.
		//	
		//	This behaviour is described in the API documentation for the
		//	ObjectInputStream and ObjectOutoutStream constructors.
		//	
		//	/gordon
		in = new ObjectInputStream(new DataInputStream(socket.getInputStream()));
		out = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()));
		connected = true;

		// Create event-receiver thread.
		rHandler = new ClientResponseHandler();
		rHandler.setPriority(Thread.MAX_PRIORITY);
		rHandler.start();
	}

	// Constructor.
	public ICSUDA_Client(ICSUDA_ClientInterface c)
	{
		log = new ICSUDA_Logfile("./ICSUDA_Client.log", "ICSUDA_Client");
		loggedIn = connected = false;
		client = c;
	}

	// Close the connection.
	public void close()
	{
		//socket = null;
		try
		{
			if(socket != null)
				socket.close();
		} catch(Exception e) {
		} finally {
//			log.Log("Connection closed.");
			socket = null;
			in = null;
			out = null;
			connected = false;
			myContacts = new ArrayList();
		}
	}
}
