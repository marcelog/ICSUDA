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
import java.util.*;

public class ICSUDA_Logfile
{
	private String name;
	private String prefix;
	
	// Support functions.
	public void Log(String m)
	{
		StringBuffer sb = new StringBuffer();
		String timestamp = getTimestamp();
		sb.append(timestamp + " - " + prefix + ": " + m);
		System.out.println(sb);
	}

	private String getTimestamp()
	{
		String timestamp;
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		String hourStr = formatTime(hour);
		int minute = calendar.get(Calendar.MINUTE);
		String minuteStr = formatTime(minute);
		timestamp = formatTime(calendar.get(Calendar.DATE)) + "/" +
			formatTime(calendar.get(Calendar.MONTH)) + "/" +
			formatTime(calendar.get(Calendar.YEAR)) + " - " + hourStr + ":" + minuteStr;
		return timestamp;
	}

	private String formatTime(int time)
	{	
		String timeStr;
		if(time < 10)
			timeStr = "0" + time;
		else
			timeStr = "" + time;
		return timeStr;
	}

	public ICSUDA_Logfile(String n, String pfx)
	{
		name = new String(n);
		prefix = new String(pfx);
	}
}
