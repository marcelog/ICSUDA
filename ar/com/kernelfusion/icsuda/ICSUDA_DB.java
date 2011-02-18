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
// 	Marcelo Gornstein - NetLabs, Argentina
// 	marcelog@netlabs.com.ar
//		http://www.netlabs.com.ar/ICSUDA
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
// This product includes software developed by NetLabs, Argentina and its contributors. 
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
import java.sql.*;

public class ICSUDA_DB
{
	private ICSUDA_Logfile log;
	public Connection Conn = null;
	public Statement Stmt = null;
	public ResultSet Rset = null;
	public Statement Stmt2 = null;
	public ResultSet Rset2 = null;
	public PreparedStatement pStmt = null;
	private String host;
	private String user;
	private String password;
	private String db;
	private String DBUrl;
	private boolean connected;
	
	public boolean online() { return connected; }

	public synchronized void close()
	{
		if(pStmt != null)
			try { pStmt.close(); } catch (SQLException sqlEx) { }
		if(Stmt != null)
			try { Stmt.close(); } catch (SQLException sqlEx) { }
		if(Stmt2 != null)
			try { Stmt2.close(); } catch (SQLException sqlEx) { }
		if(Conn != null)
			try { Conn.close(); } catch (SQLException sqlEx) { }
		log.Log("Database closed.");
		return;
	}

	public synchronized boolean open()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Conn = DriverManager.getConnection(DBUrl);
			Conn.setAutoCommit(true);
			Stmt = Conn.createStatement();
			Stmt2 = Conn.createStatement();
			connected = true;
			log.Log("Database opened.");
		} catch(Exception e) {
			log.Log("DB_Open(): " + e.getClass() + ": " + e.getMessage());
			connected = false;
		}
		return true;
	}

	public ICSUDA_DB(String h, int po, String u, String p, String d)
	{
		log = new ICSUDA_Logfile("./ICSUDA_DB.log", "ICSUDA_DB");
		log.Log("Starting.");
		host = new String(h);
		user = new String(u);
		password = new String(p);
		db = new String(d);
		connected = false;
		DBUrl = new String("jdbc:mysql://" + host + ":" + po + "/" + d + "?user=" + user + "&" + "password=" + password);
		log.Log("Using " + DBUrl);
	}
}
