//
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
package ar.com.kernelfusion.icsuda.client;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.*;
import ar.com.kernelfusion.icsuda.*;

public class ICSUDA_C extends JFrame implements WindowListener, ActionListener, ICSUDA_ClientInterface
{
	private ICSUDA_Client client;
	private Toolkit kit;
	private Dimension myScreen;
	private Rectangle r;
	private JButton menuButton = new JButton("ICSUDA!");
	private JPopupMenu popMenu = new JPopupMenu("Main options");
	private Box contactPanel = Box.createVerticalBox();
	private JMenuItem addContactMenuCmd = new JMenuItem("Add Contact");
	private JMenuItem offlineMenuCmd = new JMenuItem("Offline");
	private JMenuItem onlineMenuCmd = new JMenuItem("Online");
	private JMenuItem shutdownMenuCmd = new JMenuItem("Shutdown");

	private ContactList onlineContactsList = new ContactList();
	private Vector onlineContactsVector = new Vector();
	private Vector onlineContactsNamesVector = new Vector();
	
	private ContactList offlineContactsList = new ContactList();
	private Vector offlineContactsVector = new Vector();
	private Vector offlineContactsNamesVector = new Vector();
	
	private LoginPrompt loginPrompt = null;

	private ArrayList windowMessageOpen = new ArrayList();
	private ArrayList windowMessage = new ArrayList();

	// This is a simple StatusBar
	class StatusPane extends JLabel
	{
		private String text;

		public void refreshText() { repaint(r); };
		public void setText(String t) { text = new String("Status:" + t); super.setText(text); };
		public StatusPane(String t)
		{
			setBackground(Color.lightGray);
			setForeground(Color.black);
			setHorizontalAlignment(LEFT);
			setFont(new Font("Serif", Font.PLAIN, 12));
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			setPreferredSize(new Dimension(100, 20));
			text = new String(t);
			r = getBounds(r);
			this.setText(t);
		}
	}

	// This private class is for the user to enter host, port, uin and password.
	class LoginPrompt extends JDialog implements WindowListener, ActionListener, KeyListener
	{
		private JTextField u = new JTextField(16);
		private JPasswordField p = new JPasswordField(16);
		private JTextField h = new JTextField(16);
		private JTextField po = new JTextField(5);
		private JButton ok = new JButton("OK");
		private JButton cancel = new JButton("Cancel");
		private JLabel l1 = new JLabel("UIN");
		private JLabel l2 = new JLabel("Password");
		private JLabel l3 = new JLabel("Host");
		private JLabel l4 = new JLabel("Port");
		private StatusPane statusBar = new StatusPane("Offline");


		public void keyPressed(KeyEvent e)
		{
		}

		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
		{
			if(e.getKeyCode() == 27)
				escPressed();
		}

		public void escPressed()
		{
			System.exit(0);
		}

		// Invoked when an action occurs. 
		public void actionPerformed(ActionEvent e)
		{
			Object o = e.getSource();
			if(o == ok)
			{
				String host = h.getText();
				int port = 0;
				if(host.length() < 1)
				{
					JOptionPane.showMessageDialog(this, "Illegal host name", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try
				{
					port = Integer.parseInt(po.getText());
					if(port > 65535 || port <= 0)
						throw new Exception("asd");
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(this, "Illegal port number", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				long uin = 0;
				try
				{
					uin = Long.parseLong(u.getText());
					if(uin <= 0)
						throw new Exception("asd");
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(this, "Illegal UIN", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try
				{
					statusBar.setText("Connecting to: " + host + ":" + port + "....");
					statusBar.refreshText();
					client.connect(host, port);
					statusBar.setText("Sending login information...");
					statusBar.refreshText();
					char[] foo = p.getPassword();
					if(foo == null)
						throw new Exception("Must supply a password");
					client.login(uin, new String(foo));
					statusBar.setText("Logged in successfully");
					statusBar.refreshText();
					// Clear password after successfull login. 
					p.setText("");
					hide();
				} catch(Exception ex2) {
					statusBar.setText("Error: " + ex2.getClass() + ": " + ex2.getMessage());
					client.close();
					return;
				}
			} else if(o == cancel) {
				System.exit(0);
			}
			return;
		}

		// Invoked when the Window is set to be the active Window. 
		public void windowActivated(WindowEvent e) { }
	
		// Invoked when a window has been closed as the result of calling dispose on the window. 
		public void windowClosed(WindowEvent e) { System.exit(0); }
	
		// Invoked when the user attempts to close the window from the window's system menu. 
		public void windowClosing(WindowEvent e) {System.exit(0); } 
	
		// Invoked when a Window is no longer the active Window. 
		public void windowDeactivated(WindowEvent e) { } 
	
		// Invoked when a window is changed from a minimized to a normal state. 
		public void windowDeiconified(WindowEvent e) { } 
	
		// Invoked when a window is changed from a normal to a minimized state. 
		public void windowIconified(WindowEvent e) { }
	
		// Invoked the first time a window is made visible.
		public void windowOpened(WindowEvent e) { }

		// Constructor.
		LoginPrompt(JFrame parent)
		{
			// Initialize window.
			super(parent, "ICSUDA", true);

			Container pane = getContentPane();
			addKeyListener(this);

			pane.setLayout(new BorderLayout());
			JPanel myPanel = new JPanel();
			myPanel.setLayout(new BorderLayout());
			myPanel.setBorder(new TitledBorder(new EtchedBorder(), "Login information", TitledBorder.CENTER, TitledBorder.CENTER));

			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			addWindowListener(this);
			setBounds(myScreen.width/2 - 75, myScreen.height/2 - 75, 150, 150);
			setResizable(false);

			// Create Login prompt.
			JPanel logPanel = new JPanel();
			logPanel.setLayout(new GridLayout(4, 2, 10, 5));
			ok.addActionListener(this);
			cancel.addActionListener(this);
			ok.setMnemonic(KeyEvent.VK_O);
			ok.setDisplayedMnemonicIndex(0);
			cancel.setMnemonic(KeyEvent.VK_C);
			cancel.setDisplayedMnemonicIndex(0);
			
			logPanel.add(l1);
			logPanel.add(u);
			logPanel.add(l2);
			logPanel.add(p);
			logPanel.add(l3);
			logPanel.add(h);
			logPanel.add(l4);
			logPanel.add(po);

			JPanel bPanel = new JPanel();
			bPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			bPanel.add(ok);
			bPanel.add(cancel);

			myPanel.add(logPanel, BorderLayout.NORTH);
			myPanel.add(bPanel, BorderLayout.CENTER);

			pane.add(myPanel, BorderLayout.CENTER);
			pane.add(statusBar, BorderLayout.SOUTH);

			// Show window.
			pack();
			setVisible(true);
		}
	}

	class ContactList extends JList implements MouseListener
	{
		private JFrame parent;

		public void setParent(JFrame f) { parent = f; }

		public void mouseClicked(MouseEvent e)
		{
			if(e.getSource() == onlineContactsList)
			{
				offlineContactsList.clearSelection();
//				offlineContactsList.repaint(offlineContactsList.getBounds(null));
			} else {
				onlineContactsList.clearSelection();
//				onlineContactsList.repaint(onlineContactsList.getBounds(null));
			}
			if(e.getClickCount() == 2 && e.getModifiers() == InputEvent.BUTTON1_MASK)
			{
				//int index = locationToIndex(e.getPoint());
				if(!windowMessageOpen.contains((String)getSelectedValue()))
					new WindowMessage(parent, client.getContact((String)getSelectedValue()));
				else
				{
					WindowMessage w = (WindowMessage)windowMessage.get(windowMessageOpen.indexOf((String)getSelectedValue()));
					w.toFront();
				}
			}
		}
		public void mousePressed(MouseEvent e) { };
		public void mouseReleased(MouseEvent e) { };
		public void mouseEntered(MouseEvent e) { };
		public void mouseExited(MouseEvent e) { };
	}

	// Each of these is a messaging window.
	class WindowMessage extends JFrame implements WindowListener, ActionListener, WindowFocusListener
	{
		private JTextArea incomingMessages = new JTextArea();
		private JTextArea outgoingMessages = new JTextArea();
		private JButton sendButton = new JButton("Send");
		private StatusPane statusBar = new StatusPane("");
		private ICSUDA_Contact c;
		private ICSUDA_Packet p;
		private JScrollPane scrolledIncoming;

		public void windowGainedFocus(WindowEvent e)
		{
			outgoingMessages.requestFocus();
		}
	
		public void windowLostFocus(WindowEvent e)
		{
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

		public void addIncomingText(String n, String m)
		{
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			String hourStr = formatTime(hour);
			int minute = calendar.get(Calendar.MINUTE);
			String minuteStr = formatTime(minute);
			incomingMessages.append(n + " (" + hourStr + ":" + minuteStr + ") : \n" + m + "\n");
			incomingMessages.setCaretPosition(incomingMessages.getDocument().getLength());
			outgoingMessages.requestFocus();
		}

		public void addOutgoingText(String n)
		{
		}

		// Invoked when the Window is set to be the active Window. 
		public void windowActivated(WindowEvent e) { }
	
		// Invoked when a window has been closed as the result of calling dispose on the window. 
		public void windowClosed(WindowEvent e) { hide(); }
	
		// Invoked when the user attempts to close the window from the window's system menu. 
		public void windowClosing(WindowEvent e) { windowMessageOpen.remove(windowMessageOpen.indexOf(c.getName())); hide(); } 
	
		// Invoked when a Window is no longer the active Window. 
		public void windowDeactivated(WindowEvent e) { } 
	
		// Invoked when a window is changed from a minimized to a normal state. 
		public void windowDeiconified(WindowEvent e) { } 
	
		// Invoked when a window is changed from a normal to a minimized state. 
		public void windowIconified(WindowEvent e) { }
	
		// Invoked the first time a window is made visible.
		public void windowOpened(WindowEvent e) { }


		private void sndmsg()
		{
			String t = outgoingMessages.getText();
			if(t.length() < 1)
				return;
			sendButton.setEnabled(false);
			statusBar.setText("Sending...");
			statusBar.refreshText();
			try
			{
				p = client.sendMessage(c.getUIN(), "subject", t);
				if(p != null)
				{
					addIncomingText(client.getName(), t);
					statusBar.setText("Sent");
					statusBar.refreshText();
					outgoingMessages.setText("");
				} else {
					statusBar.setText("Timeout!");
					statusBar.refreshText();
				}
			} catch(Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getClass() + ": " + ex.getMessage(), "Could not send message", JOptionPane.ERROR_MESSAGE);
				statusBar.setText(ex.getClass() + ": " + ex.getMessage());
				statusBar.refreshText();
			}
			sendButton.setEnabled(true);
		}

		public void keyTyped(KeyEvent event)
		{
		}

		public void keyReleased(KeyEvent event)
		{
		}
	
		// Invoked when an action occurs. 
		public void actionPerformed(ActionEvent e)
		{
			Object o = e.getSource();
			if(o == sendButton)
				sndmsg();
		}
	
		WindowMessage(JFrame parent, ICSUDA_Contact co)
		{
			// Initialize window.
//			super(parent, co.getName(), false);
			super(co.getName());
			addWindowFocusListener(this);

			String n = co.getName();
			windowMessageOpen.add(n);
			windowMessage.add(windowMessageOpen.indexOf(n), this);

			c = co;

			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			addWindowListener(this);

			Container pane = getContentPane();
			pane.setLayout(new BorderLayout());
//			myPanel.setLayout(new BorderLayout());
//			myPanel.setBorder(new TitledBorder(new EtchedBorder(), "Login information", TitledBorder.CENTER, TitledBorder.CENTER));
			JPanel myPanel = new JPanel();
			myPanel.setLayout(new GridLayout(0, 1));

			// Incoming messages.
			JPanel p1 = new JPanel();
			p1.setLayout(new GridLayout());
			p1.setBorder(new EtchedBorder());
			p1.add(incomingMessages);
			scrolledIncoming = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrolledIncoming.getViewport().add(p1);
			incomingMessages.setEditable(false);
			incomingMessages.setBackground(Color.black);
			incomingMessages.setForeground(Color.green);
			incomingMessages.setLineWrap(true);
			incomingMessages.setWrapStyleWord(true);

			// Outgoing messages.
			JPanel p2 = new JPanel();
			p2.setBorder(new EtchedBorder());
			p2.add(outgoingMessages);
			p2.setLayout(new GridLayout());
			JScrollPane scrolledOutgoing = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrolledOutgoing.getViewport().add(p2);
			outgoingMessages.setEditable(true);
			outgoingMessages.setBackground(Color.black);
			outgoingMessages.setForeground(Color.green);
			outgoingMessages.setLineWrap(true);
			outgoingMessages.setWrapStyleWord(true);
//			outgoingMessages.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

			// button panel.
			JPanel bPanel = new JPanel();
//			Box bPanel = Box.createHorizontalBox();
			bPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 1));
//			bPanel.setLayout(hb);
			sendButton.addActionListener(this);
			sendButton.setDisplayedMnemonicIndex(0);
			sendButton.setMnemonic(KeyEvent.VK_S);
			bPanel.add(sendButton);
//			bPanel.add(Box.createHorizontalStrut(300));
			myPanel.add(scrolledIncoming);
			myPanel.add(scrolledOutgoing);
//			vb.add(scrolledIncoming);
//			vb.add(scrolledOutgoing);
//			vb.add(bPanel);
//			vb.add(statusBar);
//			myPanel.add(vb);
			myPanel.add(bPanel);
			pane.add(myPanel, BorderLayout.CENTER);
			pane.add(statusBar, BorderLayout.SOUTH);
//			pane.add(bPanel);
			setResizable(true);
			pack();
			setBounds(myScreen.width/2 - 75, myScreen.height/2 - 75, 450, 250);
			show();
			outgoingMessages.requestFocus();
		}
	}

	// Invoked when an action occurs. 
	public void actionPerformed(ActionEvent e)
	{
		Object o = e.getSource();
		if(o == menuButton)
		{
			popMenu.show(menuButton, 0, 0);
		} else if(o == offlineMenuCmd) {
			client.logout();
			onlineContactsVector = new Vector();
			onlineContactsNamesVector = new Vector();
			onlineContactsList.setListData(onlineContactsNamesVector);
			offlineContactsVector = new Vector();
			offlineContactsNamesVector = new Vector();
			offlineContactsList.setListData(offlineContactsNamesVector);
			onlineMenuCmd.setVisible(true);
			addContactMenuCmd.setVisible(false);
			offlineMenuCmd.setVisible(false);
		} else if(o == onlineMenuCmd) {
			loginPrompt.show();
			getContacts();
			offlineMenuCmd.setVisible(true);
			addContactMenuCmd.setVisible(true);
			onlineMenuCmd.setVisible(false);
		} else if(o == shutdownMenuCmd) {
			Quit();
			dispose();
			System.exit(0);
		}
		
		return;
	}

	// Invoked when the Window is set to be the active Window. 
	public void windowActivated(WindowEvent e) { }

	// Invoked when a window has been closed as the result of calling dispose on the window. 
	public void windowClosed(WindowEvent e) { Quit(); }

	// Invoked when the user attempts to close the window from the window's system menu. 
	public void windowClosing(WindowEvent e) { Quit(); } 

	// Invoked when a Window is no longer the active Window. 
	public void windowDeactivated(WindowEvent e) { } 

	// Invoked when a window is changed from a minimized to a normal state. 
	public void windowDeiconified(WindowEvent e) { } 

	// Invoked when a window is changed from a normal to a minimized state. 
	public void windowIconified(WindowEvent e) { }

	// Invoked the first time a window is made visible.
	public void windowOpened(WindowEvent e) { }

	// Called to exit program.
	// Why the JVM calls me twice???
	public void Quit()
	{
		if(client != null)
			client.close();
	}

	public void getContacts()
	{
		// Generate contact List.
		ArrayList onc = client.getContacts(true);
		ArrayList ofc = client.getContacts(false);


		for(int i = 0; i < onc.size(); i++)
		{
			onlineContactsVector.add((ICSUDA_Contact)onc.get(i));
			onlineContactsNamesVector.add(((ICSUDA_Contact)onc.get(i)).getName());
		}
		for(int i = 0; i < ofc.size(); i++)
		{
			offlineContactsVector.add((ICSUDA_Contact)ofc.get(i));
			offlineContactsNamesVector.add(((ICSUDA_Contact)ofc.get(i)).getName());
		}

		onlineContactsList.setListData(onlineContactsNamesVector);
		offlineContactsList.setListData(offlineContactsNamesVector);
		return;
	}

	public synchronized void contactOnline(long uin)
	{
		ICSUDA_Contact co;
//		System.out.println("online " + uin);
		for(Enumeration e = offlineContactsVector.elements() ; e.hasMoreElements() ;)
		{
			co = (ICSUDA_Contact)e.nextElement();
			if(co.getUIN() == uin)
			{
				
				offlineContactsNamesVector.remove(co.getName());
				offlineContactsVector.remove(co);
				onlineContactsNamesVector.add(co.getName());
				onlineContactsVector.add(co);
			}
		}
		onlineContactsList.setListData(onlineContactsNamesVector);
		offlineContactsList.setListData(offlineContactsNamesVector);
	}

	public synchronized void contactOffline(long uin)
	{
		ICSUDA_Contact co;
//		System.out.println("offline " + uin);
		for(Enumeration e = onlineContactsVector.elements() ; e.hasMoreElements() ;)
		{
			co = (ICSUDA_Contact)e.nextElement();
			if(co.getUIN() == uin)
			{
				
				onlineContactsNamesVector.remove(co.getName());
				onlineContactsVector.remove(co);
				offlineContactsNamesVector.add(co.getName());
				offlineContactsVector.add(co);
			}
		}
		onlineContactsList.setListData(onlineContactsNamesVector);
		offlineContactsList.setListData(offlineContactsNamesVector);
	}

	public synchronized void incomingMessage(ICSUDA_Message m)
	{
		WindowMessage w;
		String n = m.getFrom().getName();
		int i;
		if((i = windowMessageOpen.indexOf(n)) < 0)
			w = new WindowMessage(this, m.getFrom());
		else
			w = (WindowMessage)windowMessage.get(i);
		w.addIncomingText(n, m.getBody());
		w.toFront();
	}

	public ICSUDA_C()
	{
		// Initialize window.
		super("ICSUDA");
		// Show login screen and connect to server.
		kit = getToolkit();
		myScreen = kit.getScreenSize();
		client = new ICSUDA_Client(this);
		loginPrompt = new LoginPrompt(this);

		setTitle(String.valueOf(client.getUIN()));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(this);
		setBounds(myScreen.width-200, 0, 100, 600);
		setResizable(true);

		// Build bottom buttons panel.
		Box buttonsPanel = Box.createHorizontalBox();
		menuButton.addActionListener(this);
		buttonsPanel.add(menuButton);

		// Build pop-menu for ICSUDA!
		popMenu.add(addContactMenuCmd);
		popMenu.add(offlineMenuCmd);
		popMenu.add(onlineMenuCmd);
		onlineMenuCmd.setVisible(false);
		popMenu.add(new JPopupMenu.Separator());
		popMenu.add(shutdownMenuCmd);
		addContactMenuCmd.addActionListener(this);
		offlineMenuCmd.addActionListener(this);
		onlineMenuCmd.addActionListener(this);
		shutdownMenuCmd.addActionListener(this);
		popMenu.setInvoker(menuButton);

		// Build online contacts panel.
		JPanel onlineContacts = new JPanel();
		onlineContacts.setLayout(new GridLayout());
		TitledBorder b = new TitledBorder(new EtchedBorder(), "Online", TitledBorder.CENTER, TitledBorder.CENTER);
//		b.setTitleColor(Color.green);
		onlineContacts.setBorder(b);
//		onlineContactsList.setFixedCellWidth(contactPanel.getWidth());
		onlineContactsList.setParent(this);
		onlineContactsList.setPrototypeCellValue("1234567890");
		onlineContactsList.setBackground(onlineContacts.getBackground());
		onlineContactsList.setForeground(Color.blue);
//		onlineContactsList.setHorizontalTextPosition(SwingConstants.CENTER);
		onlineContacts.add(onlineContactsList);
		onlineContactsList.addMouseListener(onlineContactsList);
		
		// Build offline contacts panel.
		JPanel offlineContacts = new JPanel();
		offlineContacts.setLayout(new GridLayout());
		b = new TitledBorder(new EtchedBorder(), "Offline", TitledBorder.CENTER, TitledBorder.CENTER);
//		b.setTitleColor(Color.red);
		offlineContacts.setBorder(b);
//		offlineContactsList.setFixedCellWidth(offlineContacts.getWidth());
		offlineContactsList.setParent(this);
		offlineContactsList.setPrototypeCellValue("1234567890");
		offlineContactsList.setBackground(offlineContacts.getBackground());
		offlineContactsList.setForeground(Color.red);
		offlineContacts.add(offlineContactsList);
		offlineContactsList.addMouseListener(offlineContactsList);

		// Build main contacts panel.
		contactPanel.add(onlineContacts);
		contactPanel.add(offlineContacts);

		JScrollPane scrolledContactPanel = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrolledContactPanel.getViewport().add(contactPanel);

		// Show window.
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(scrolledContactPanel, BorderLayout.CENTER);
		pane.add(buttonsPanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);

		// Generate contact List.
		getContacts();

	}

	// Main Entry point.
	public static void main(String[] args)
	{
		new ICSUDA_C();
	}
}