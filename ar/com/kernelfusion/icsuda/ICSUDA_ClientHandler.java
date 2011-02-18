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
import java.net.*;
import java.util.*;
import java.awt.event.*;

public class ICSUDA_ClientHandler extends Thread
{
	private Socket c;
	private int id;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean doneRequest;
	private boolean shutdown;
	private ICSUDA_Logfile log;
	private javax.swing.Timer timeOut;
	private boolean loggedIn;
	private ICSUDA_Packet request;
	private ICSUDA_Packet response;
	public ICSUDA_Contact cl;
	private ICSUDA_Message m;
	private ICSUDA_ServerInterface server;

	public ICSUDA_Contact getContact() { return loggedIn ? cl : null; }

	public synchronized void sendPacket(ICSUDA_Packet p) throws Exception
	{
//		log.Log("Sending packet to client " + c.toString() + ":" + p.thid);
		out.writeObject(p);
		out.flush();
		//out.reset();
	}

	// We will work with this client from now on.
	public synchronized void newClient(Socket C)
	{
		c =  C;
		doneRequest = false;
	}

	// New connection available!
	public void handleConnection()
	{
		if(doneRequest)
		{
			log.Log("WARNING: SPURIOUS OR WRONG WAKEUP!! c=null");
			return;
		}
	
		log.Log("Incoming connection from: " + c.toString());

		try
		{
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
			//
	
			// Get I/O.
			out = new ObjectOutputStream(new DataOutputStream(c.getOutputStream()));
			in = new ObjectInputStream(new DataInputStream(c.getInputStream()));
		} catch(Exception e) {
			log.Log("Error reading from client, closing connection: " + e.getClass() + ": " + e.getMessage());
			// Close connection.
			log.Log("Done with request.");
			try { c.close(); } catch(Exception E) { } finally { }
			return;
		}

		// Accept/Handle client input.
		while(!doneRequest)
		{
			log.Log("Awaiting next command.");
			try
			{
				// Read data.
				request = (ICSUDA_Packet)in.readObject();
				response = new ICSUDA_Packet();
				response.errorcode = ICSUDA_Packet.E_FAILED;
				cl = request.c;
				m = request.m;

				response.code = request.code;
				response.subcode = request.subcode;
				response.thid = request.thid;

				switch(response.code)
				{
					case ICSUDA_Packet.CODE_AUTH:
						switch(response.subcode)
						{
							case ICSUDA_Packet.SUBCODE_LOGIN:
								response.c = cl = server.login(id, cl.getUIN(), cl.getPassword(), c.getInetAddress().getHostAddress(), c.getPort());
								if(cl != null)
								{
									response.contacts = server.getContacts(cl.getUIN());
									response.errorcode = ICSUDA_Packet.E_SUCCESS;
									loggedIn = true;
									server.notifyOnline(cl.getUIN());
								}
								break;
							case ICSUDA_Packet.SUBCODE_LOGOUT:
								if(loggedIn)
								{
									server.logout(id);
									doneRequest = true;
									response.errorcode = ICSUDA_Packet.E_SUCCESS;
									loggedIn = false;
									server.notifyOffline(cl.getUIN());
									cl = null;
								}
								break;
							default:
								throw new Exception("INVALID SUBCODE, dropping connection.");
//								break;
						}
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
							default:
								throw new Exception("INVALID SUBCODE, dropping connection.");
//								break;
						}
						break;
					case ICSUDA_Packet.CODE_MESSAGE:
						switch(response.subcode)
						{
							case ICSUDA_Packet.SUBCODE_ADD:
								ICSUDA_ClientHandler h = server.isOnline2(m.getTo().getUIN());
								log.Log("New message " + m.getFrom().getUIN() + "->" + m.getTo().getUIN() + ":" + h);
								if(h != null)
								{
									try
									{
										request.thid = 0;
										h.sendPacket(request);
										response.errorcode = ICSUDA_Packet.E_SUCCESS;
									} catch(Exception e) {
										response.errorcode = ICSUDA_Packet.E_FAILED;
									} finally {
									}
								} else {
									try {
										server.leaveMessage(m);
										response.errorcode = ICSUDA_Packet.E_SUCCESS;
									} catch (Exception e) {
										response.errorcode = ICSUDA_Packet.E_FAILED;
									}
								}									
								break;
							case ICSUDA_Packet.SUBCODE_DEL:
								break;
							case ICSUDA_Packet.SUBCODE_GET:
								break;
							case ICSUDA_Packet.SUBCODE_ACK:
								break;
							default:
								throw new Exception("INVALID SUBCODE, dropping connection.");
//								break;
						}
						break;
					default:
						throw new Exception("INVALID CODE, dropping connection.");
//						break;
				}
				sendPacket(response);
			} catch(Exception e) {
				log.Log("Error reading from client, closing connection: " + e.getClass() + ": " + e.getMessage());
				// Close connection.
				if(loggedIn)
				{
					server.logout(id);
					doneRequest = true;
					response.errorcode = ICSUDA_Packet.E_SUCCESS;
					loggedIn = false;
					server.notifyOffline(cl.getUIN());
					cl = null;
				}
				log.Log("Done with request.");
				try { c.close(); } catch(Exception E) { } finally { }
				return; 
			}
		}
	}

	public boolean isReady() { return doneRequest; }
	public synchronized void wakeup() { notify(); }

	public void run()
	{
		log.Log("Launched!");
		c = null;
		shutdown = false;

		// Each client has about 7' to enter a room, and will be disconnected after this long if it
		// hasn't.
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				timeOut.stop();
				if(!loggedIn)
				{
					log.Log("login timeout expired for this client, dropping connection.");
					// Close connection.
					try { c.close(); } catch(Exception E) { } finally {}
					return; 
				}
			}
		};
		timeOut = new javax.swing.Timer(30000, taskPerformer);

		// Wait until server "notify" us of a new connection to handle.
		while(shutdown == false)
		{
			try
			{
				// Cleanup, prepare for next client.
				c = null;
				doneRequest = true;
				synchronized(this) { wait(); }
				doneRequest = false;
				loggedIn = false;
				// Start timeout for login on this client.
				timeOut.start();
				// Deal with client commands.
				handleConnection();
				// Stop timer.
				timeOut.stop();
			} catch(Exception e) {
			}
		}
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
		log.Log("Finished.");
	}

	// Constructor.
	public ICSUDA_ClientHandler(int i, ICSUDA_ServerInterface s)
	{
		super();
		server = s;
		id = i;
		doneRequest = loggedIn = false;
		cl = null;
		// Open log, quit if can't log anything.
		log = new ICSUDA_Logfile("./ICSUDA_ClientHandler" + id + ".log", "ICSUDA_ClientHandler." + id);
		log.Log("Starting.");
	}
}
