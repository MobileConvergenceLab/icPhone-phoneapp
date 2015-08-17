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
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

//export CCNX_USER_NAME=your_new_username
//export CCNX_DIR=/var/tmp/your_new_username/.ccnx
//ccnchat ccnx:/test_room

public class CCNTalk extends JFrame implements ActionListener,
		CCNServiceCallback {
	private static final long serialVersionUID = -8779269133035264361L;

	private JPanel ConfigPanel = new JPanel(new FlowLayout());
	private JPanel ChattingPanel = new JPanel(new BorderLayout());
	private JPanel tempPanel = new JPanel();

	private JTextField InputRoomName = new JTextField(15);
	private JButton okBtn = new JButton("OK");
	private JButton sendBtn = new JButton("send");

	protected JTextArea MessageArea = new JTextArea(20, 10);
	private JTextField TypedText = new JTextField(11);
	public String ChattingRoomName = "ccnx:/";
	public String UserName = "default";

	private static CCNService ccnService = null;


	public CCNTalk() throws MalformedContentNameStringException {
		ccnService = new CCNService(this);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					stop();
				} catch (IOException e1) {
					System.out.println("IOException shutting down listener : "
							+ e1);
					e1.printStackTrace();
				}
			}
		});
		
		

		// 스크린 사이즈에 맞춰 최대화해줄 수 있도록.
		// 또는 라즈베리파이의 화면 해상도에 맞춰서 크기 조절
		this.setSize(250, 300);
		this.setVisible(true);
		//this.setResizable(false);
		//this.setResizable(true);
		
		setTitle("[CCN Talk]");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ImageIcon imageIcon = new ImageIcon("src/KHU.jpeg");

		JLabel icon = new JLabel(imageIcon);
		JLabel RoomInfo = new JLabel("Room Name");
		ConfigPanel.setBackground(Color.WHITE);
		ConfigPanel.add(RoomInfo);
		ConfigPanel.add(InputRoomName);
		ConfigPanel.add(okBtn);
		ConfigPanel.add(icon);
		ConfigPanel.setVisible(true);
		getContentPane().add(ConfigPanel);
		
		
		okBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ChattingRoomName += InputRoomName.getText();

				if (ChattingRoomName != "ccnx:/") {
					// When RoomName is normal
					System.out
							.println("[CCNTalkMainActivity] RoomName is normarl");
				} else {
					// When RoomName is abnormal
					System.out
							.println("[CCNTalkMainActivity] RoomName is abnormal");
					ChattingRoomName = "DefaultRoomName";
				}
				try {
					System.out.println("ChattingRoomName is " + ChattingRoomName);
					ccnService.setNamespace(ChattingRoomName);
				} catch (MalformedContentNameStringException e) {
					System.out.println("setNamespace error");
					e.printStackTrace();
				}

				getContentPane().removeAll();
				getContentPane().add(ChattingPanel);
				revalidate();
				repaint();
				
				
				ccnService.start();				
			}
		});

		
		JPanel SouthPanel = new JPanel(new BorderLayout());
		MessageArea.setEditable(false);
		MessageArea.setBackground(Color.LIGHT_GRAY);
		MessageArea.setLineWrap(true);
		TypedText.addActionListener(this);
		
		SouthPanel.add(TypedText, BorderLayout.CENTER);
		SouthPanel.add(sendBtn, BorderLayout.EAST);

		ChattingPanel.add(new JScrollPane(MessageArea), BorderLayout.CENTER);
		ChattingPanel.add(SouthPanel, BorderLayout.SOUTH);
		
		sendBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String newText = TypedText.getText();
					if ((newText != null) && (newText.length() > 0)) {
						ccnService.sendMessage(newText);						
					}
				} catch (Exception e1) {
					System.err.println("Exception saving out input: "
							+ e1.getClass().getName() + ":" + e1.getMessage());
					e1.printStackTrace();
					receiveMessage("Exception saving out input: "
							+ e1.getClass().getName() + ":" + e1.getMessage());
				}
				TypedText.setText("");
				TypedText.requestFocusInWindow();
				
			}
			
		});
		revalidate();
		repaint();

		TypedText.requestFocusInWindow();
		setVisible(true);
		

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// process input to TextField after user hit enter button.
		try {
			String newText = TypedText.getText();
			if ((newText != null) && (newText.length() > 0)) {
				ccnService.sendMessage(newText);
			}
		} catch (Exception e1) {
			System.err.println("Exception saving out input: "
					+ e1.getClass().getName() + ":" + e1.getMessage());
			e1.printStackTrace();
			receiveMessage("Exception saving out input: "
					+ e1.getClass().getName() + ":" + e1.getMessage());
		}
		TypedText.setText("");
		TypedText.requestFocusInWindow();
	}

	@Override
	public void receiveMessage(String msg) {
		System.out.println("[CCNTalk]receiveMessage");
		MessageArea.insert(msg, MessageArea.getText().length());
		MessageArea.setCaretPosition(MessageArea.getText().length());
	}

	@Override
	public String getUserName() {
		return UserName;
	}
	

	public static void usage() {
		System.err.println("usage : CCNTalk <ccn URI>");
	}

	protected void start() throws IOException, ConfigurationException,
			MalformedContentNameStringException {
		ccnService.setNamespace(ChattingRoomName);
		ccnService.listen();
	}

	protected void stop() throws IOException {
		// called by window thread when window closes
		ccnService.shutdown();
	}

	public static void main(String[] args) {

		try {
			new CCNTalk();

		} catch (MalformedContentNameStringException e) {
			System.err.println("Not a valid ccn URI: " + args[0] + ": "
					+ e.getMessage());
			e.printStackTrace();
		}
	}





}
