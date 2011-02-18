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
package ar.com.kernelfusion.icsuda.server;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;
import java.sql.*;
import ar.com.kernelfusion.icsuda.*;

public class ICSUDA_S implements ICSUDA_ServerInterface
{
	private ICSUDA_Logfile log;
	private InetAddress ipAddress;
	private SSLServerSocketFactory sFactory;
	private SSLServerSocket sSocket;
	private int myPort;
	private boolean shutdown;
	private ICSUDA_ClientHandler[] clientHandlers;
	private ICSUDA_DB db;
	private ICSUDA_Contact[] onlineUsers;

	public synchronized void leaveMessage(ICSUDA_Message m)
	{
	}

	public synchronized void notifyOnline(long uin)
	{
		// Get contacts for this user.
		try
		{
			if(!db.online())
				if(!db.open())
					throw new Exception("DB could not be opened.");
			ResultSet rset = db.Stmt.executeQuery("SELECT uin FROM contacts WHERE contact=" + uin);
			while(rset.next())
			{
				ICSUDA_ClientHandler h = isOnline2(rset.getLong("uin"));
				if(h != null)
				{
					ICSUDA_Packet p = new ICSUDA_Packet();
					p.code = ICSUDA_Packet.CODE_CONTACT;
					p.subcode = ICSUDA_Packet.SUBCODE_ONLINE;
					p.c = new ICSUDA_Contact(uin, "");
					h.sendPacket(p);
				}
			}
		} catch(Exception ex) {
			log.Log("notifyOnline(): Could not access database: " + ex.getClass() + ": " + ex.getMessage());
		}
	}

	public synchronized void notifyOffline(long uin)
	{
		// Get contacts for this user.
		try
		{
			if(!db.online())
				if(!db.open())
					throw new Exception("DB could not be opened.");
//			log.Log("notifyOffline() : entering " + uin);
			ResultSet rset = db.Stmt.executeQuery("SELECT uin FROM contacts WHERE contact=" + uin);
			while(rset.next())
			{
				long u = rset.getLong("uin");
//				log.Log("notifyOffline() : looking up " + u);
				ICSUDA_ClientHandler h = isOnline2(u);
				
				if(h != null)
				{
//					log.Log("notifyOffline: " + rset.getLong("uin"));
					ICSUDA_Packet p = new ICSUDA_Packet();
					p.code = ICSUDA_Packet.CODE_CONTACT;
					p.subcode = ICSUDA_Packet.SUBCODE_OFFLINE;
					p.c = new ICSUDA_Contact(uin, "");
					h.sendPacket(p);
				}
			}
		} catch(Exception ex) {
			log.Log("notifyOffline(): Could not access database: " + ex.getClass() + ": " + ex.getMessage());
		}
	}
	
	public synchronized ICSUDA_ClientHandler isOnline(long uin)
	{
		int j = clientHandlers.length;
		ICSUDA_Contact c;

		for(int i = 0; i < j; i++)
		{
			c = clientHandlers[i].getContact();
			if(c != null)
				if(c.getUIN() == uin)
					return clientHandlers[i];
		}
		return null;
	}

	public synchronized ICSUDA_ClientHandler isOnline2(long uin)
	{
		int j = onlineUsers.length;
		ICSUDA_Contact c;

		for(int i = 0; i < j; i++)
		{
			if(onlineUsers[i] != null)
			{
				c = (ICSUDA_Contact)onlineUsers[i];
				if(c.getUIN() == uin)
					return clientHandlers[i];
			}
		}
		return null;
	}

	public synchronized ArrayList getContacts(long uin)
	{
		ArrayList foo = new ArrayList();
		// Get contacts for this user.
		try
		{
			if(!db.online())
				if(!db.open())
					throw new Exception("DB could not be opened.");
			ResultSet rset = db.Stmt.executeQuery("SELECT contact FROM contacts WHERE uin=" + uin);
			while(rset.next())
			{
				long u = rset.getLong("contact");
				String n = new String();
				int o = 0;
				// Get name and online status for each contact.
				ResultSet rset2 = db.Stmt2.executeQuery("SELECT name,loggedIn FROM users WHERE uin=" + u);
				while(rset2.next())
				{
					n = new String(rset2.getString("name"));
					o = rset2.getInt("loggedIn");
				}
				foo.add(new ICSUDA_Contact(u, n, o == 0 ? false : true));
			}
		} catch(Exception ex) {
			log.Log("getContacts(): Could not access database: " + ex.getClass() + ": " + ex.getMessage());
		} finally {
		}
		return foo;
	}

	public synchronized ICSUDA_Contact login(int id, long uin, String password, String ip, int port)
	{
		int i = id - 1;
		onlineUsers[i] = null;
		log.Log("LOGIN for " + uin + "from: " + ip + ":" + port);
		try
		{
			if(!db.online())
				if(!db.open())
					throw new Exception("DB could not be opened.");
			ResultSet rset = db.Stmt.executeQuery("SELECT * FROM users WHERE loggedIn=0 AND uin=" + uin + " AND password=password(\"" + password + "\")");
			if(rset.next())
			{
				rset.updateInt("loggedIn", 1);
				rset.updateString("ip", ip);
				rset.updateInt("port", port);
				rset.updateRow();
				onlineUsers[i] = new ICSUDA_Contact(rset.getLong("uin"), rset.getString("name"));
			}
		} catch(Exception ex) {
			log.Log("login(): Could not access database: " + ex.getClass() + ": " + ex.getMessage());
		} finally {
		}
		return onlineUsers[i];
	}

	public synchronized void logout(int id)
	{
		int i = id - 1;
		notifyOffline(onlineUsers[i].getUIN());
		log.Log("LOGOUT for " + onlineUsers[i].getUIN());

		// Update database.
		try
		{
			if(!db.online())
				if(!db.open())
					throw new Exception("DB could not be opened.");
			ResultSet rset = db.Stmt.executeQuery("SELECT * FROM users WHERE loggedIn=1 AND uin=" + onlineUsers[i].getUIN());
			if(rset.next())
			{
				rset.updateInt("loggedIn", 0);
				rset.updateString("ip", "");
				rset.updateInt("port", 0);
				rset.updateRow();
			}			
		} catch(Exception ex) {
			log.Log("logout(): Could not access database: " + ex.getClass() + ": " + ex.getMessage());
		} finally {
		}
		// Update online users list.
		onlineUsers[i] = null;
		return;
	}

	// The security manager is a class that allows applications to implement a security 
	// policy. It allows an application to determine, before performing a possibly 
	// unsafe or sensitive operation, what the operation is and whether it is being 
	// attempted in a security context that allows the operation to be performed. The 
	// application can allow or disallow the operation. 
	// 
	// The SecurityManager class contains many methods with names that begin with the 
	// word check. These methods are called by various methods in the Java libraries 
	// before those methods perform certain potentially sensitive operations.
	// SecurityManager security = System.getSecurityManager();

	// Constructor.	
	public ICSUDA_S(String address, int port, int backlog, String dbHost, int dbPort, String dbUser, String dbPass, String dbName)
	{
		int i;
		// Open log, quit if can't log anything.
		log = new ICSUDA_Logfile("./ICSUDA_Server.log", "ICSUDA_Server");
		log.Log("Starting.");
		
		// Open SSL Socket.
		log.Log("Opening SSL Socket.");
		try
		{
			ipAddress = InetAddress.getByName(address);
			myPort = port;
			sFactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
			sSocket = (SSLServerSocket)sFactory.createServerSocket(port, backlog, ipAddress);
		} catch(Exception e) {
			log.Log("Could not open SSL Socket: " + e.getClass() + ": " + e.getMessage());
			return;
		}
		// Get supported procotols and cipher suites.
		log.Log("SSL Socket opened successfully at: " + ipAddress.toString() + ":" + myPort);
		String[] protos = sSocket.getSupportedProtocols();
		String[] cipher = sSocket.getSupportedCipherSuites();
		log.Log("Supported protocols: ");
		for(i = 0; i < protos.length; i++)
			log.Log("\t" + protos[i]);
		log.Log("Supported cipher suites: ");
		for(i = 0; i < cipher.length; i++)
			log.Log("\t" + cipher[i]);

		// Set which protocols and cipher suites we'll use.
		String[] foo = new String[] {"SSL_DH_anon_WITH_3DES_EDE_CBC_SHA"};
		sSocket.setEnabledCipherSuites(foo);
		foo = new String[] {"SSLv3"};
		sSocket.setEnabledProtocols(foo);
		protos = sSocket.getEnabledProtocols();
		cipher = sSocket.getEnabledCipherSuites();
		log.Log("Using protocols: ");
		for(i = 0; i < protos.length; i++)
			log.Log("\t" + protos[i]);
		log.Log("Using cipher suites: ");
		for(i = 0; i < cipher.length; i++)
			log.Log("\t" + cipher[i]);

		// Initialize DB object.
		db = new ICSUDA_DB(dbHost, dbPort, dbUser, dbPass, dbName);
		onlineUsers = new ICSUDA_Contact[backlog];

		// Initialize objects for future clients.
		clientHandlers = new ICSUDA_ClientHandler[backlog];
		// Setup threads.
		for(i = 0; i < backlog; i++)
		{
			clientHandlers[i] = new ICSUDA_ClientHandler(i + 1, this);
			clientHandlers[i].start();
		}
		// Everything initialized properly, go on.
		shutdown = false;

		// Start main server loop.
		log.Log("Ready for connections.");
		while(!shutdown)
		{
			// XXX: A SecurityManager could be implemented right here so maybe we can
			// trash invalid connections *before* we accept() them, thus gaining some
			// performance.
			try
			{
				Socket c = sSocket.accept();
				c.setKeepAlive(true);
				//set default recv() timeout to 5'.
//				c.setSoTimeout(300000);
				//set default lingering timeout to 5'.
				c.setSoLinger(true, 300000);
				SocketAddress sAddress = c.getRemoteSocketAddress();
				for(i = 0; i < backlog; i++)
				{
					if(clientHandlers[i].isReady())
					{
						clientHandlers[i].newClient(c);
						clientHandlers[i].wakeup();
						break;
					}
				}
				if(i == backlog)
				{
					// Close connection, all our threads are busy right now...
					log.Log("Rejected connection from: " + c.toString() + ". BUSY BUSY BUSY.");
					try { c.close(); } catch(Exception e) { }
					continue;
				}

			} catch(Exception ex) {
				try { sSocket.close(); } catch (Exception fooEx) { }
				log.Log("Could not accept() connections: " + ex.getClass() + ": " + ex.getMessage());
				return;
			}
		}
		// Something happened, we're shutting down.
		log.Log("Main server finished.");
	}

	// Main Entry point.
	public static void main(String[] args)
	{
		if(args.length < 2)
		{
			System.out.println("Use: ICSUDA_Server <address> <port>");
			System.exit(0);
		}
		new ICSUDA_S(args[0], Integer.parseInt(args[1]), 20, "1.1.1.1", 3306, "user", "pass", "ICSUDA");
	}
}
