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
/*

	"Pontus Lundwall" <dr-m@brutalhomicideSPAM.com> wrote in message news:9r8c0g$9ru$1@yggdrasil.utfors.se...
	>
	> I am constructing a application that heavily relies on serialization. The
	> application uses a lot of sometimes complex objects that are serialized and
	> sent to a server. This all works, but I am suffering from a memory leak on
	> both ends, seems that what ever I do using serialization, leaks memory. I
	> can't really understand why this is happening either.
	>
	> Roughly, I'm instanciating an Event, filling it with some information and
	> sending it to the server. The server will regulary send back updates to all
	> clients in the same manner. The Event stores the data in a HashMap, holding
	> anything from Integers to ArrayLists and other HashMap's goes in there.
	> Anyone else here suffered from leaks when using serialization?
	>
	
	If you are using the same ObjectInputStream / ObjectOutputStream
	for the entire run of the program, you should know that correct
	behavior of these streams requires them to keep a reference to
	every object sent through them, until they are closed or reset.
	This behavior may mimic the symptoms of a leak. (Or you
	could call it a "logical leak".)
	
	
	> I understand. The I suppose I need to reset() the OIS and OOS? I hope I
	> won't have to touch the originating socket, closing and reopening a TCP
	> stream seems a little bit clumsy.. ;)
	
	Yeah, calling reset() will do the job just fine.
	
	> Do you know what reason the designers had to store a reference to every
	> object sent trough the OOS and OIS?
	
	Can you imagine what would happen when you try to serialize a doubly linked
	list if it didn't have that behavior?
	Message 5 in thread 
	Autor:Kenny MacLeod (Kenny_macleod@S.P.A.M.yahoo.com)
	Asunto:Re: Serializable problems 
	 
	  
	> Do you know what reason the designers had to store a reference to every
	> object sent trough the OOS and OIS?

	Instead of opening and closing the object streams, try keeping the raw input
	and output streams open, but re-instantiate the OIS and OOS around them
	every time you want to send an event.
	
	It's part of the serialisation spec.  If you try serialize the same object
	twice, the mechanism will instead just serialize a reference to the first
	one.
	
	For example, say you have an OOS and you write two objects to it.  Each of
	these objects has a reference to the same third object.  When you read these
	from the OIS, the two objects should still refer to the same third object.
	So you can see that the OIS and IIS have to keep track of objects as they go
	through.
	
	You don't need to close the socket. Also, I believe you can get away
	with just resetting the OOS; I think it sends a code to the receiving
	stream to do the same. But you might want to test my assumption.
	
	
	> Do you know what reason the designers had to store a reference to every
	> object sent trough the OOS and OIS?
	> Thanks for the quick help anyway!
	
	Consider a Vector of three elements, with element 1 and 3 being
	references to the same object. If you just naively serialize everything
	that comes your way, on the far side, there'd be three different objects,
	where 1 and 3 happened to have the same values.
	
	Yep, it did the work. Just resetting after each incoming or outgoing object
	removes all the references and frees the memory.
	
	 /Pontus
*/
package ar.com.kernelfusion.icsuda;
import java.io.*;
import java.util.*;

public class ICSUDA_Packet implements Serializable
{
	// Codes.
	public static final int CODE_AUTH = 0;
	public static final int CODE_CONTACT = 1;
	public static final int CODE_MESSAGE = 2;
	public static final int CODE_FILE = 3;

	// Subcodes.
	public static final int SUBCODE_LOGIN = 0;
	public static final int SUBCODE_LOGOUT = 1;
	public static final int SUBCODE_ADD = 2;
	public static final int SUBCODE_GET = 3;
	public static final int SUBCODE_DEL = 4;
	public static final int SUBCODE_ACK = 5;
	public static final int SUBCODE_ONLINE = 6;
	public static final int SUBCODE_OFFLINE = 7;
	public static final int SUBCODE_UP = 8;
	public static final int SUBCODE_DOWN = 9;
	public static final int SUBCODE_LIST = 10;

	// Errorcodes.
	public static final int E_SUCCESS = 0;
	public static final int E_FAILED = 1;

	public int code;
	public int subcode;
	public int errorcode;
	public int thid;

	public ICSUDA_Contact c;
	public ICSUDA_Message m;
	public ArrayList contacts;

	public ICSUDA_Packet(int Code, int Subcode, int Thid, String Msg)
	{
		code = Code;
		subcode = Subcode;
		thid = Thid;
	}

	public ICSUDA_Packet()
	{
	}
}
