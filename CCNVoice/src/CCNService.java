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


/**
 * Networking service class to communicate with other CCNx node
 */
public final class CCNService extends Thread{
	
	private final CCNServiceCallback m_callback;
	private ContentName m_namespace;		// room name of service for users
	private String m_namespaceStr;			// string version of the room name

	private Timestamp m_lastUpdate;			// recent time tag
	private boolean m_finished = false;		// tag for checking the end of service
	private static String lastMessage = "";	// meesage container

	// this is where we store the friendly name of the user
	private HashMap<PublisherPublicKeyDigest, String> m_friendlyNameToDigestHash;
	private ContentName m_friendlyNameNamespace;

	private static final long CYCLE_TIME = 1000;	// running cycle
	private static final int WAIT_TIME_FOR_FRIENDLY_NAME = 2500;
	private static final String SYSTEM = "System";
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:SS");

	// seperate read and write libraries so we will read our own updates,
	// and don't have to treat out inputs differently than others.
	private CCNStringObject readString;		// content object of CCN to read received message
	private CCNStringObject writeString;	// content object of CCN to write sending message

	// we use these for storing the friendly names of users.
	private CCNStringObject readFriendlyNameString;
	private CCNStringObject writeFriendlyNameString;

	private String getFriendlyName(PublisherPublicKeyDigest digest) {
		if (m_friendlyNameToDigestHash.containsKey(digest)) {
			return m_friendlyNameToDigestHash.get(digest);
		} else {
			return "";
		}
	}

	private void addNameToHash(PublisherPublicKeyDigest digest,
			String friendlyName) {
		m_friendlyNameToDigestHash.put(digest, friendlyName);
	}

	// TODO : sender and receiver format
	private void showMessage(String sender, Timestamp time) {
		m_callback.receiveMessage("[" + sender + "] | " + DATE_FORMAT.format(time));
	}
	
	private void playAudio(String sender, Timestamp time, String AudioData) {
		m_callback.receiveData(AudioData);
	}
	


	private static Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}

	// ==================================================================
	// Internal methods

	/**
	 * constructor of CCNService object
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
	
	// constructor of CCNServer without namespace
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
		
		String introduction = UserConfiguration.userName() + "enters the " + m_namespace.toString().substring(1, m_namespace.toString().length()) + "room.";
		writeString = new CCNStringObject(m_namespace, introduction, SaveType.RAW, tempWriteHandle);
		writeString.save();
		
		//publish the user's friendly name under a new ContentName
		String friendlyNameNamespaceStr = m_namespaceStr + "/members/";
		m_friendlyNameNamespace = KeyProfile.keyName(ContentName.fromURI(friendlyNameNamespaceStr), writeString.getContentPublisher());
		
		
		//read the string here
		//Use this constructor with null data to avoid an initial blocking call to update in the constructor,
		//for example if you are going to call updateInBackground.
		readFriendlyNameString = new CCNStringObject(m_friendlyNameNamespace, (String)null, SaveType.RAW, tempReadHandle);
		readFriendlyNameString.updateInBackground(true);
		
		
		String publishedNameStr = UserConfiguration.userName();
		writeFriendlyNameString = new CCNStringObject(m_friendlyNameNamespace, publishedNameStr, SaveType.RAW, tempWriteHandle);
		writeFriendlyNameString.save();
		
		try {
			addNameToHash(writeFriendlyNameString.getContentPublisher(), writeFriendlyNameString.string());
		} catch(IOException e) {
			System.err.println("Unable to read from " + writeFriendlyNameString + "for writing to hashMap");
			e.printStackTrace();
		}

		boolean firstCheck = true;
		
		// service thread
		while(!m_finished) {
			try {
				synchronized(readString) {
					readString.wait(CYCLE_TIME);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			if(readString.isSaved()) {
				Timestamp thisUpdate = readString.getVersion();;
				if((m_lastUpdate == null) || thisUpdate.after(m_lastUpdate)) {
					m_lastUpdate = thisUpdate;
					
					String userFriendlyName = getFriendlyName(readString.getContentPublisher());
					if(userFriendlyName.equals("")) {
						//its not in the hashMap
						//so, try and read the user's friendly name from the ContentName and then add it to the hashMap
						String userNameStr = m_namespaceStr + "/members/";
						m_friendlyNameNamespace = KeyProfile.keyName(ContentName.fromURI(userNameStr), readString.getContentPublisher());
						
						try {
							readFriendlyNameString = new CCNStringObject(m_friendlyNameNamespace, (String)null, SaveType.RAW, tempReadHandle);
						} catch(Exception e) {
							e.printStackTrace();
						}
						
						//waiting for 2.5 secs
						//update in background and have callback
						readFriendlyNameString.update(WAIT_TIME_FOR_FRIENDLY_NAME);
						
						if(readFriendlyNameString.available()) {
							if(!readString.getContentPublisher().equals(readFriendlyNameString.getContentPublisher())) {
								showMessage(userFriendlyName, thisUpdate);
								playAudio(userFriendlyName, thisUpdate, readString.string());
								
							} else {
								addNameToHash(readFriendlyNameString.getContentPublisher(), readFriendlyNameString.string());
								showMessage(getFriendlyName(readFriendlyNameString.getContentPublisher()), thisUpdate);
								playAudio(getFriendlyName(readFriendlyNameString.getContentPublisher()), thisUpdate, readFriendlyNameString.string());
								
							}
							
							
						} else {
							showMessage(userFriendlyName, thisUpdate);
							playAudio(userFriendlyName, thisUpdate, readString.string());
						}
						
						
					} else {
						showMessage(userFriendlyName, thisUpdate);
						playAudio(userFriendlyName, thisUpdate, readString.string());
					}
				}
			}			
		}		
	}

	@Override
	public void run() {
		try {
			listen();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (MalformedContentNameStringException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
