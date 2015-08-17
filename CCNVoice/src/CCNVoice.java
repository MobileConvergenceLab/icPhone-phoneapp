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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.ccnx.ccn.io.content.ContentEncodingException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;


/**
 * front-end(UI)
 * GUI for communicate user
 */
public class CCNVoice extends JFrame implements CCNServiceCallback {

	private static final long serialVersionUID = -328096355073388654L;
	public ByteArrayOutputStream out;
	private static CCNService ccnService = null;
	public String ChattingRoomName = "ccnx:/";
	private boolean isRecorded = false;
	private boolean isPlayed = false;
	private String AudioData = null;
	private AudioFormat format = getFormat();
	private String charsetName = "UTF-16";
	private int BufferSize = 1024;
	private DataLine.Info TargetInfo;
	private static TargetDataLine m_TargetDataLine;


	// swing based GUI component
	// based panel
	private JPanel ConfigPanel = new JPanel(new FlowLayout()); // Forr
																// congifuring
																// room name
	private JPanel ChattingPanel = new JPanel(new BorderLayout()); // For
																	// Message
																	// List
	private JPanel ControlPanel = new JPanel(new GridLayout(0, 3)); // For
																	// record,
																	// play,
																	// send Btn
	private JTextField InputRoomName = new JTextField(15);

	// button image icon
	private URL imageURL = getClass().getResource("/img/KHU.jpeg");
	private URL recordURL = getClass().getResource("/img/record.png");
	private URL playURL = getClass().getResource("/img/play.png");
	private URL stopURL = getClass().getResource("/img/stop.png");

	public ImageIcon imageIcon;
	private ImageIcon recordIcon;
	private ImageIcon playIcon;
	private ImageIcon stopIcon;

	private JButton okBtn;
	private JButton recordBtn;
	private JButton playBtn;
	private JButton sendBtn;

	private JTextArea MessageArea = null;

	// constructor of CCNVoice object
	public CCNVoice() throws MalformedContentNameStringException {
		ccnService = new CCNService(this);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stop();
			}
		});

		// the window size of application
		// window size : maximize or customize your device
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);

		setTitle("[CCN Voice]");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		imageIcon = new ImageIcon(imageURL);
		recordIcon = new ImageIcon(recordURL);
		stopIcon = new ImageIcon(stopURL);
		playIcon = new ImageIcon(playURL);
		
		okBtn = new JButton("OK");
		sendBtn = new JButton(" Send ");
		recordBtn = new JButton(recordIcon);
		playBtn = new JButton(playIcon);
		

		MessageArea = new JTextArea();	

		// add each components
		JLabel icon = new JLabel(imageIcon);
		JLabel RoomInfo = new JLabel("Room Name");
		ConfigPanel.setBackground(Color.WHITE);
		ConfigPanel.add(RoomInfo);
		ConfigPanel.add(InputRoomName);
		ConfigPanel.add(okBtn);
		ConfigPanel.add(icon);
		ConfigPanel.setVisible(true);
		getContentPane().add(ConfigPanel);

		// button listener
		okBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ChattingRoomName += InputRoomName.getText();

				if (ChattingRoomName != "ccnx:/") {
					// When RoomName is normal
				} else {
					// When RoomName is abnormal
					ChattingRoomName = "DefaultRoomName";
				}

				try {
					ccnService.setNamespace(ChattingRoomName);
				} catch (MalformedContentNameStringException e1) {
					e1.printStackTrace();
				}

				getContentPane().removeAll();
				getContentPane().add(ChattingPanel);
				revalidate();
				repaint();

				// start CCN based networking service(back-end)
				ccnService.start();
			}
		});

		recordBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isRecorded) {
//					recordBtn.setText("Record");
					recordBtn.setIcon(recordIcon);
					isRecorded = false;
				} else {
					recordAudio();
//					recordBtn.setText(" Stop ");
					recordBtn.setIcon(stopIcon);
				}

			}

		});
		sendBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendAudioData();
			}

		});
		playBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(isPlayed) {
//					playBtn.setText("Play");
					playBtn.setIcon(playIcon);
					isPlayed = false;
				} else {
					playAudio();
//					playBtn.setText("Stop");
					playBtn.setIcon(stopIcon);
				}
			}

		});
		MessageArea.setBackground(Color.LIGHT_GRAY);
		MessageArea.setEditable(false);
		MessageArea.setLineWrap(true);

		// set GUI panel
		ControlPanel.setPreferredSize(new Dimension(50,100));
		ControlPanel.add(recordBtn);
		ControlPanel.add(playBtn);
		ControlPanel.add(sendBtn);

		ChattingPanel.add(new JScrollPane(MessageArea), BorderLayout.CENTER);
		ChattingPanel.add(ControlPanel, BorderLayout.SOUTH);

		revalidate();
		repaint();
		setVisible(true);
		
		TargetInfo = new DataLine.Info(TargetDataLine.class, format);
		try {
			m_TargetDataLine = (TargetDataLine) AudioSystem.getLine(TargetInfo);
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}
	


	}

	// stop the application
	protected void stop() {
		try {
			ccnService.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// record a audio data(PCM)	
	private void recordAudio() {
		try {			
			m_TargetDataLine.open(format);
			m_TargetDataLine.start();
			
			Runnable runner = new Runnable() {
				byte buffer[] = new byte[BufferSize];
				public void run() {
					out = new ByteArrayOutputStream();
					isRecorded = true;
					try {
						while (isRecorded) {
							int count = m_TargetDataLine.read(buffer, 0, buffer.length);
							if(count > 0) {
								out.write(buffer, 0, count);
							}							
						}						
						AudioData = new String(out.toByteArray(), charsetName);
						out.close();
						m_TargetDataLine.close();
					} catch (IOException e) {
						System.err.println("I/O problems: " + e);
					}

				}
			};
			Thread captureThread = new Thread(runner);
			captureThread.start();
		} catch (LineUnavailableException e) {
			System.err.println("Line unavailable: " + e);
		}
	}

	// play a audio data(received audio and sended audio)
	private void playAudio() {
		try {
			byte[] audio = null;
			isPlayed = true;
			try {
				audio = AudioData.getBytes(charsetName);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			InputStream input = new ByteArrayInputStream(audio);
			final AudioInputStream ais = new AudioInputStream(input, format,
					audio.length / format.getFrameSize());
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start();
			Runnable runner = new Runnable() {
				byte buffer[] = new byte[BufferSize];
				public void run() {
					try {
						int count;
						while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
							if (count > 0) {
								line.write(buffer, 0, count);
							}
						}
						line.drain();
						line.close();
						setPlayBtnText();
					} catch (IOException e) {
						System.err.println("I/O problems: " + e);
					}
				}
			};
			Thread playThread = new Thread(runner);
			playThread.start();
		} catch (LineUnavailableException e) {
			System.err.println("Line unavailable: " + e);
		}
	}

	// play the parameter data
	public void playAudio(byte[] data) {
		try {
			InputStream input = new ByteArrayInputStream(data);
			final AudioFormat format = getFormat();
			final AudioInputStream ais = new AudioInputStream(input, format,
					data.length / format.getFrameSize());
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			final SourceDataLine line = (SourceDataLine) AudioSystem
					.getLine(info);
			line.open(format);
			line.start();
			Runnable runner = new Runnable() {
				byte buffer[] = new byte[BufferSize];
				public void run() {
					try {
						int count;
						while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
							if (count > 0) {
								line.write(buffer, 0, count);
							}
						}
						line.drain();
						line.close();
					} catch (IOException e) {
						System.err.println("I/O problems: " + e);
					}
				}
			};
			Thread playThread = new Thread(runner);
			playThread.start();
		} catch (LineUnavailableException e) {
			System.err.println("Line unavailable: " + e);
		}
	}

	// send the audio data to back-end(network service part)
	private void sendAudioData() {
		try {
			ccnService.sendMessage(AudioData);
		} catch (ContentEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Send data to ccn, excepting audio data for example, session info, meta
	 * data
	 * 
	 * @param payload
	 */
	public void sendData(byte[] payload) {
		// TODO send data excepting audio
	}

	// information about recording audio
	private AudioFormat getFormat() {
		float sampleRate = 8000;
		int sampleSizeInBits = 8;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
				bigEndian);
	}

	public static void main(String[] args) {
		try {
			System.out.println("[CCNVoice]main");
			new CCNVoice();
		} catch (MalformedContentNameStringException e) {
			System.err.println("Not a valid ccn URI: " + args[0] + ": "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void receiveData(String data) {
		
		AudioData = data;
		try {
			playAudio(AudioData.getBytes(charsetName));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(String msg) {
		MessageArea.insert(msg + "\n", MessageArea.getText().length());
		MessageArea.setCaretPosition(MessageArea.getText().length());
		
	}
	
	public void setPlayBtnText() {
//		playBtn.setText("Play");
		playBtn.setIcon(playIcon);
		isPlayed = false;
	}

}
