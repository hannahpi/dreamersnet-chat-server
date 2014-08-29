package net.dreamersnet.ChatServer;

import java.awt.*;
import java.awt.event.*;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.*;
import javax.swing.text.*;

public class AppWindow {
	static PrintStream output;
	static Client cli;
	private JFrame frame;
	static private JTextPane txtChat = new JTextPane();
	static private Document doc = txtChat.getDocument();
	//Style base = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
	private JScrollPane scrollPane;
	
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
								} else {
									cli.sendMessage(message);
									txtEnter.setText("");
								}
							}
						}
				});
				txtEnter.setForeground(new Color(255, 255, 255));
				txtEnter.setBackground(new Color(51, 51, 255));
				txtEnter.setTabSize(20);
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
				txtEnter.grabFocus();			
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
}
