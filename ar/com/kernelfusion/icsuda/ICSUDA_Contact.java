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
import java.io.*;

public class ICSUDA_Contact implements Serializable
{
	public static int STATUS_AVAILABLE;
	public static int STATUS_AWAY;
	public static int STATUS_LONGAWAY;
	public static int STATUS_INVISIBLE;

	private long uin;
	private String name;
	private String password;
	private boolean online;
	private int status;

	public void setStatus(int s) { status = s; }
	public int getStatus() { return status; }
	public String getPassword() { return new String(password); }
	public String getName() { return new String(name); }
	public void setName(String n) { name = new String(n); }
	public long getUIN() { return uin; }
	public boolean isOnline() { return online; }

	public ICSUDA_Contact(ICSUDA_Contact c)
	{
		uin = c.getUIN();
		name = c.getName();
	}

	public ICSUDA_Contact(long u, String n)
	{
		uin = u;
		name = new String(n);
	}

	public ICSUDA_Contact(long u, String n, boolean o)
	{
		uin = u;
		name = new String(n);
		online = o;
	}

	public ICSUDA_Contact(long u, String n, String p)
	{
		uin = u;
		name = new String(n);
		password = new String(p);
	}

	public ICSUDA_Contact()
	{
	}
}
