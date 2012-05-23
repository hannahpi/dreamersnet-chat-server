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
	Style base = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
	//Style base = StyleContext.getDefaultStyleConteTextPane txtChat;
	private JScrollPane scrollPane;
	
	
	

	/**
	 * Launch the application.
	 */
	public static void run(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppWindow window = new AppWindow(cli);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Launch the application.
	 */
	public static void run() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppWindow window = new AppWindow(cli);
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
		//output = new PrintStream(new ChatOutput(txtChat));
		initialize();
		cli = c;
	}

	public OutputStream getOutputStream() {
		return (output);
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 973, 653);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JTextArea txtEnter = new JTextArea();
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
		
		txtChat.setForeground(new Color(255, 255, 240));
		txtChat.setEditable(false);
		txtChat.setBackground(Color.BLACK);
		
		scrollPane = new JScrollPane(txtChat);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		txtEnter.setBackground(new Color(51, 51, 255));
		txtEnter.setTabSize(20);
		frame.getContentPane().add(txtEnter, BorderLayout.SOUTH);
	}
	
	public void print(String str) {
		Document doc = txtChat.getDocument();
		try {
			doc.insertString(doc.getLength(), str, null);
		} catch (Exception e) { System.out.println("txt Print error : " + e); }
		txtChat.setCaretPosition(doc.getLength());	
	}
	
	public void println(String str) {
		Document doc = txtChat.getDocument();
		try {
			doc.insertString(doc.getLength(), str + "\n", null);
		} catch (Exception e) { System.out.println("txt Print error : " + e); }
		txtChat.setCaretPosition(doc.getLength());
	}

}
