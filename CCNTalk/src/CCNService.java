/*
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 */

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.logging.Level;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.config.UserConfiguration;
import org.ccnx.ccn.impl.CCNFlowControl.SaveType;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.content.CCNStringObject;
import org.ccnx.ccn.io.content.ContentEncodingException;
import org.ccnx.ccn.profiles.security.KeyProfile;
import org.ccnx.ccn.protocol.CCNTime;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.KeyLocator;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.ccnx.ccn.protocol.PublisherPublicKeyDigest;

public final class CCNService extends Thread{
	
	private final CCNServiceCallback m_callback;
	private ContentName m_namespace; // 채팅방 이름
	private String m_namespaceStr;

	private Timestamp m_lastUpdate;
	private boolean m_finished = false;
	private static String lastMessage = "";

	// this is where we store the friendly name of the user
	private HashMap<PublisherPublicKeyDigest, String> m_friendlyNameToDigestHash;
	private ContentName m_friendlyNameNamespace;

	private static final long CYCLE_TIME = 1000;
	private static final int WAIT_TIME_FOR_FRIENDLY_NAME = 2500;
	private static final String SYSTEM = "System";
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");

	// seperate read and write libraries so we will read our own updates,
	// and don't have to treat out inputs differently than others.
	private CCNStringObject readString;
	private CCNStringObject writeString;

	// we use these for storing the friendly names of users.
	private CCNStringObject readFriendlyNameString;
	private CCNStringObject writeFriendlyNameString;

	private String getFriendlyName(PublisherPublicKeyDigest digest) {
		System.out.println("[CCNService]getFriendlyName");
		if (m_friendlyNameToDigestHash.containsKey(digest)) {
			return m_friendlyNameToDigestHash.get(digest);
		} else {
			Log.info("We DON'T have an entry in our hash for this " + digest);
			return "";
		}
	}

	private void addNameToHash(PublisherPublicKeyDigest digest,
			String friendlyName) {
		System.out.println("[CCNService]addNameToHash");
		m_friendlyNameToDigestHash.put(digest, friendlyName);
	}

	// TODO : sender and receiver format
	private void showMessage(String sender, Timestamp time, String message) {
		System.out.println("[CCNService]showMessage(,,)");
		String tabString = "";
		String dummyString = "                                               ";
		for(int i = 0; i < (50-message.length()); i ++)
			tabString += " ";
		
		if (sender == UserConfiguration.userName()) {	
			System.out.println("[CCNService]showMessage in if");
			m_callback.receiveMessage(dummyString + "[" +sender + " | " + DATE_FORMAT.format(time) +"]\n" + tabString + message + "\n\n");

		} else {
			System.out.println("[CCNService]showMessage in else");
			m_callback.receiveMessage("[" + sender + " "
					+ DATE_FORMAT.format(time) + "]\n" + message + "\n\n");
		}
	}


	private void showMessage(PublisherPublicKeyDigest publisher,
			KeyLocator keyLocator, Timestamp time, String message) {
		System.out.println("showMessage(PublisherPublicKey function");
		showMessage(publisher.shortFingerprint().substring(0, 8), time, message);
	}
	
	private void showSystemMessage(Timestamp time, String message) {
		System.out.println("[CCNService]showSystemMessage(,)");
		m_callback.receiveMessage("[" + DATE_FORMAT.format(time) + "] " + message +"\n\n");
	}

	private static Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	// ==================================================================
	// Internal methods

	/**
	 * CCNService 객체의 생성자
	 * 
	 * @param callback
	 *            The callback to the UI to receive a message
	 * @param namespace
	 *            The namespace of the Chat channel
	 * @throws MalformedContentNameStringException
	 */
	public CCNService(CCNServiceCallback callback, String namespace)
			throws MalformedContentNameStringException {
		m_callback = callback;
		m_namespace = ContentName.fromURI(namespace);
		m_namespaceStr = namespace;
		m_friendlyNameToDigestHash = new HashMap<PublisherPublicKeyDigest, String>();
	}
	
	public CCNService(CCNServiceCallback callback) throws MalformedContentNameStringException{
		m_callback = callback;
		m_namespace = null;
		m_namespaceStr = "";
		m_friendlyNameToDigestHash = new HashMap<PublisherPublicKeyDigest, String>();
	}
	
	public void setNamespace(String namespace) throws MalformedContentNameStringException {
		m_namespace = ContentName.fromURI(namespace);
		m_namespaceStr = namespace;
	}

	/**
	 * Send a message out to the network.
	 * 
	 * @param message
	 *            The text string to send
	 * @throws IOException
	 * @throws ContentEncodingException
	 */
	public synchronized void sendMessage(String message)
			throws ContentEncodingException, IOException {
		writeString.save(message);
	}

	public void shutdown() throws IOException {
		m_finished = true;

		if (readString != null) {
			readString.cancelInterest();
			sendMessage(UserConfiguration.userName() + "님이 퇴장하셨습니다.");
		}
	}

	public void setLogging(Level level) {
		Log.setLevel(Log.FAC_ALL, level);
	}

	/**
	 * This actual CCN loop to send/receive messages. Called by the UI class.
	 * This method blocks! If the UI is not multi-threaded, you should start a
	 * thread to hold listen().
	 * 
	 * When shutdown() is called, listen() will exit.
	 * 
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws MalformedContentNameStringException
	 */
	public void listen() throws ConfigurationException, IOException, MalformedContentNameStringException {
		//publish your keys under the chat "channel name" namespace 
		//ccnx:/namespace
		System.out.println("m_namespace is " + m_namespace);
		System.out.println(UserConfiguration.userName());
		
		if(m_namespace.toString().startsWith("ccnx:/")) {
			UserConfiguration.setDefaultNamespacePrefix(m_namespace.toString().substring(5));
		} else {
			UserConfiguration.setDefaultNamespacePrefix(m_namespace.toString());
		}
		
		//writing must be on a different handle, to enable us to read back text we have written when nobody else is reading.
		CCNHandle tempReadHandle = CCNHandle.getHandle();
		CCNHandle tempWriteHandle = CCNHandle.open();
		
		
		readString = new CCNStringObject(m_namespace, (String)null, SaveType.RAW, tempReadHandle);
		readString.updateInBackground(true);
		
		String introduction = UserConfiguration.userName() + "님이 " + m_namespace.toString().substring(1, m_namespace.toString().length()) + "방에 입장하셨습니다.";
		writeString = new CCNStringObject(m_namespace, introduction, SaveType.RAW, tempWriteHandle);
		writeString.save();
		
		//publish the user's friendly name under a new ContentName
		String friendlyNameNamespaceStr = m_namespaceStr + "/members/";
		m_friendlyNameNamespace = KeyProfile.keyName(ContentName.fromURI(friendlyNameNamespaceStr), writeString.getContentPublisher());
		Log.info("**** Friendly Namespace is " + m_friendlyNameNamespace);
		
		
		//read the string here
		//Use this constructor with null data to avoid an initial blocking call to update in the constructor,
		//for example if you are going to call updateInBackground.
		readFriendlyNameString = new CCNStringObject(m_friendlyNameNamespace, (String)null, SaveType.RAW, tempReadHandle);
		readFriendlyNameString.updateInBackground(true);
		
		
		String publishedNameStr = UserConfiguration.userName();
		Log.info("*****I am adding my own friendly name as " + publishedNameStr);
		writeFriendlyNameString = new CCNStringObject(m_friendlyNameNamespace, publishedNameStr, SaveType.RAW, tempWriteHandle);
		writeFriendlyNameString.save();
		
		try {
			addNameToHash(writeFriendlyNameString.getContentPublisher(), writeFriendlyNameString.string());
		} catch(IOException e) {
			System.err.println("Unable to read from " + writeFriendlyNameString + "for writing to hashMap");
			e.printStackTrace();
		}

		boolean firstCheck = true;
		
		while(!m_finished) {
			try {
				synchronized(readString) {
					readString.wait(CYCLE_TIME);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			if(readString.isSaved()) {
				Timestamp thisUpdate = readString.getVersion();
//				Timestamp thisTime = new Timestamp(System.currentTimeMillis());
//				System.out.println("Timestamp = " + System.currentTimeMillis() + "   " + System.nanoTime());
				if((m_lastUpdate == null) || thisUpdate.after(m_lastUpdate)) {
					Log.info("Got an update : " + readString.getVersion());					
					m_lastUpdate = thisUpdate;
					
					String userFriendlyName = getFriendlyName(readString.getContentPublisher());
					System.out.println("userFriendlyName : " + userFriendlyName);
					if(userFriendlyName.equals("")) {
						//its not in the hashMap
						//so, try and read the user's friendly name from the ContentName and then add it to the hashMap
						String userNameStr = m_namespaceStr + "/members/";
						m_friendlyNameNamespace = KeyProfile.keyName(ContentName.fromURI(userNameStr), readString.getContentPublisher());
						
						try {
							readFriendlyNameString = new CCNStringObject(m_friendlyNameNamespace, (String)null, SaveType.RAW, tempReadHandle);
						} catch(Exception e) {
							Log.info("In while, if (userFriendlyName.equals");
							e.printStackTrace();
						}
						
						//waiting for 2.5 secs
						//update in background and have callback
						readFriendlyNameString.update(WAIT_TIME_FOR_FRIENDLY_NAME);
						
						if(readFriendlyNameString.available()) {
							if(!readString.getContentPublisher().equals(readFriendlyNameString.getContentPublisher())) {
								System.out.println("1 showMessage");
								showMessage(readString.getContentPublisher(), readString.getPublisherKeyLocator(), thisUpdate, readString.string());
								
							} else {
								System.out.println("2 showMessage & " + readFriendlyNameString.string());
								addNameToHash(readFriendlyNameString.getContentPublisher(), readFriendlyNameString.string());
								showMessage(readFriendlyNameString.string(), thisUpdate, readString.string());
								
							}
							
							
						} else {
							System.out.println("3 showMessage");
							showMessage(readString.getContentPublisher(), readString.getPublisherKeyLocator(), thisUpdate, readString.string());
						}
						
						
					} else {
						//When user enters the room, first, introduction
						//showMessage(userFriendlyName, thisUpdate, readString.string());					
						if(firstCheck) {
							showSystemMessage(thisUpdate, readString.string());
							firstCheck = false;
							continue;
						} 
						showMessage(userFriendlyName, thisUpdate, readString.string());
						System.out.println("showMessage");
					}
				}
			}
			
			
			
		}
		
		
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			listen();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedContentNameStringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
