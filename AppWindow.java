package net.dreamersnet.ChatServer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.Document;

public class AppWindow {
	static PrintStream output;
	static Client cli;
	private JFrame frame;
	static private boolean windowActive = false;
	static private JTextPane txtChat = new JTextPane();
	static private Document doc = txtChat.getDocument();
	static private JList<String> lstUsers = new JList<String>();
	private String[] users = new String[30];
	private JScrollPane scrollPane;
	private String sound1Str= "Electro_-S_Bainbr-7953_hifi.wav";
	private String sound2Str= "polish-xrikazen-7425_hifi.wav";
	private URL file1 = AppWindow.class.getResource(sound1Str);
	private URL file2 = AppWindow.class.getResource(sound2Str);
	Clip clip1;
	Clip clip2;
	AudioInputStream ais1;
	AudioInputStream ais2;
	
	/**
	 * Launch the application.
	 */
	public synchronized static void run() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppWindow window = new AppWindow(cli);
					window.initialize();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public synchronized void setActive(boolean active){
		windowActive = active;
	}
	
	public boolean isActive() {
		return windowActive;
	}
	
	/**
	 * Create the application.
	 */
	public AppWindow(Client c) {
		initialize();
		cli = c;	
	}

	public OutputStream getOutputStream() {
		return (output);
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private synchronized void initialize() {
				frame = new JFrame();
				frame.setBounds(100, 100, 973, 653);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
				final JTextArea txtEnter = new JTextArea();
				txtEnter.setFont(new Font("Monospaced", Font.PLAIN, 20));
				txtEnter.setRows(1);
				txtEnter.addKeyListener(new KeyAdapter() {
					@Override
						public void keyReleased(KeyEvent keyPress) {
							if (keyPress.getKeyCode()==10)
							{
								String message = txtEnter.getText();
								if ((message.length()>0) && (message.indexOf("/") == 0)) {
									message = message.substring(1);	
									cli.sendCommand(message);
									txtEnter.setText("");
								} else if (message.length()>0) {
									cli.sendMessage(message);
									txtEnter.setText("");
								}
							}
						}
				});
				txtEnter.setForeground(new Color(255, 255, 255));
				txtEnter.setBackground(new Color(51, 51, 255));
				txtEnter.setTabSize(20);
				
				lstUsers.setFont(new Font("Monospaced",Font.PLAIN, 20));
				lstUsers.setListData(users);				
				lstUsers.setForeground(new Color(255,255,255));
				lstUsers.setBackground(new Color(0,0,0));
				
				frame.getContentPane().add(lstUsers, BorderLayout.EAST);
				frame.getContentPane().add(txtEnter, BorderLayout.SOUTH);
				txtChat.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
				txtChat.setForeground(new Color(255, 255, 240));
				txtChat.setEditable(false);
				txtChat.setBackground(Color.BLACK);
		
				scrollPane = new JScrollPane(txtChat);
				frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
				txtEnter.setBackground(new Color(51, 51, 255));
				txtEnter.setTabSize(20);
				frame.getContentPane().add(txtEnter, BorderLayout.SOUTH);
				frame.addWindowFocusListener(new WindowAdapter() {
				    public void windowGainedFocus(WindowEvent e) {
				        txtEnter.requestFocusInWindow();
				        setActive(true);
				    }
				});
				frame.addWindowFocusListener(new WindowAdapter() {
				    public void windowLostFocus(WindowEvent e) {
				    	setActive(false);
				    }
				});
				try {
					clip1 = AudioSystem.getClip();
					clip2 = AudioSystem.getClip();
					ais1 = AudioSystem.getAudioInputStream(file1);
					ais2 = AudioSystem.getAudioInputStream(file2);
					clip1.open(ais1);
					clip2.open(ais2);
				} catch (Exception e) {
					e.printStackTrace();
				}
	}
	
	public String askString(String question, String title) {
		String answer = (String) JOptionPane.showInputDialog(frame, question);
		return answer;
	}
	
	public synchronized void print(final String str) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {				
				try {
					doc = txtChat.getDocument();
					doc.insertString(doc.getLength(), str, null);
				} catch (Exception e) { 
					System.out.println("txt Print error : " + e); 
				}
			}
		});
	}
	
	public synchronized void println(final String str) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {		
				try {
					doc = txtChat.getDocument();
					doc.insertString(doc.getLength(), str + "\n", null);
				} catch (Exception e) {
					System.out.println("txt Print error : " + e); 
				}
				txtChat.setCaretPosition(doc.getLength());
			}
		});
	}
	
	public void playMsgSound() {
		if (isActive() == false)
		{
			clip1.setFramePosition(0);
			clip1.start();
		}
	}
	
	public void playEnterSound() {
		clip2.start();
	}
	
	public void setNames(String[] newList) {
		this.users = newList;
		lstUsers.setListData(users);
	}
	
}
