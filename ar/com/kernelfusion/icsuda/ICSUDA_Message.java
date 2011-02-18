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
import java.util.*;

public class ICSUDA_Message implements Serializable
{
	private ICSUDA_Contact from;
	private ICSUDA_Contact to;
	private String subject;
	private String body;
	private Date dateCreated;
	private Date dateSent;
	private Date dateReceived;
	
	public ICSUDA_Contact getFrom() { return new ICSUDA_Contact(from); }
	public ICSUDA_Contact getTo() { return new ICSUDA_Contact(to); }
		
	public void setBody(String b) { body = new String(b); }
	public String getBody() { return new String(body); }

	public void setSubject(String s) { subject = new String(s); }
	public String getSubject() { return new String(subject); }
	
	public Date getDateCreated() { return new Date(dateCreated.getTime()); }
	public Date getDateReceived() { return new Date(dateReceived.getTime()); }
	public Date getDateSent() { return new Date(dateSent.getTime()); }

	public ICSUDA_Message(ICSUDA_Contact f, ICSUDA_Contact t)
	{
		from = new ICSUDA_Contact(f);
		to = new ICSUDA_Contact(t);

		// Fill current date.
		dateCreated = Calendar.getInstance().getTime();
		body = new String();
		subject = new String();
	}

	public ICSUDA_Message(ICSUDA_Contact f, ICSUDA_Contact t, String s, String b)
	{
		from = new ICSUDA_Contact(f);
		to = new ICSUDA_Contact(t);

		// Fill current date.
		dateCreated = Calendar.getInstance().getTime();
		body = new String(b);
		subject = new String(s);
	}
}
