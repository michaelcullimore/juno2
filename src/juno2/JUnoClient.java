package juno2;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

import junoServer.Protocol;
import junoServer.Receivable;

public class JUnoClient extends JFrame implements Receivable {

    /**
     *
     */
    private static final long serialVersionUID = -4953493595169902287L;

    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		try {
		    JUnoClient client = new JUnoClient();
		    client.setVisible(true);

		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    private JPanel contentPanel;
    private JTextArea messageArea;
    private JTextArea sendArea;
    private Protocol protocol;
    private String username;

    private boolean userIsSet = false;

    public JUnoClient() {
	connectToServer();

	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(200, 200, 800, 600);
	contentPanel.setLayout(new BorderLayout());
	contentPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
	setContentPane(contentPanel);

	initGameArea();
	initChat();
    }

    private void connectToServer() {
	try {
	    protocol = new Protocol(this);
	} catch (Exception e) {
	    System.err.println("Error in protocol setup!");
	    e.printStackTrace();
	}
    }

    @Override
    public void giveMessage(JSONObject jsonM) {
	JSONObject jsonMessage = jsonM;
	if (jsonMessage.getString("type").equals("chat")) {// could be "message"
	    handleMessages(jsonMessage);
	}

    }

    public void handleMessages(JSONObject jsonM) {
	JSONObject jsonMessage = jsonM;
	System.out.println(jsonMessage.toString());
	messageArea.append(jsonMessage.getString("fromUser") + ": " + jsonMessage.getString("message") + "\n");
    }

    private void initChat() {
	JPanel sendPanel = new JPanel(new FlowLayout());
	sendArea = new JTextArea(3, 20);
	sendArea.setEditable(true);
	sendArea.setWrapStyleWord(true);
	sendArea.setLineWrap(true);
	JScrollPane sendScroll = new JScrollPane(sendArea);
	sendScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	sendPanel.add(sendScroll);
	sendArea.addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiers() == KeyEvent.CTRL_MASK) {
		    sendMessage();
		}
	    }
	});

	JPanel messagePanel = new JPanel(new BorderLayout());
	messageArea = new JTextArea();
	messageArea.setEditable(true);
	messageArea.setWrapStyleWord(true);
	messageArea.setLineWrap(true);
	JScrollPane messageScroll = new JScrollPane(messageArea);
	messageScroll.setSize(getSize());//
	messageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	messagePanel.add(messageScroll, "Center");

	JButton send = new JButton("Send");
	sendPanel.add(send);
	send.addActionListener(e -> sendMessage());

	messagePanel.add(sendPanel, "South");
	contentPanel.add(messagePanel, "West");
	messagePanel.setVisible(true);
	messageArea.requestFocusInWindow();
    }

    private void initGameArea() {
	JPanel gamePanel = new JPanel(new BorderLayout());
	JButton startButton = new JButton("Start Game");
	startButton.addActionListener(e -> startGame());

	// tests whether or not the picture of the card comes through
	Card cardTest1 = new Card(Card.Color.RED, Card.Value.ZERO);
	gamePanel.add(cardTest1, "Center");
	gamePanel.add(startButton, "South");
	contentPanel.add(gamePanel, "Center");

    }

    public void printMessage(String message) {
	messageArea.append(message + "\n");
	messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    private void sendMessage() {
	JSONObject jsonMessage = new JSONObject();

	String messageSend;
	messageSend = sendArea.getText();

	if (messageSend == "\\whois") {
	    jsonMessage.put("type", "whois");
	    return;
	}

	jsonMessage.put("type", "chat");

	String whisperRegex = "(?<=^|(?<=[^a-zA-Z0-9-_\\\\.]))@([A-Za-z][A-Za-z0-9_]+)";
	Matcher matcher = Pattern.compile(whisperRegex).matcher(messageSend);
	if (matcher.find()) {
	    System.out.println(matcher.group(0));
	    StringBuilder whisperRecipient = new StringBuilder(matcher.group(0));
	    whisperRecipient.deleteCharAt(0);
	    System.out.println(whisperRecipient);
	    jsonMessage.put("username", whisperRecipient);
	    messageSend += " (Whispered from " + username + ")";
	}

	jsonMessage.put("message", messageSend);
	System.out.println("Execution of sendText() complete.");
	protocol.sendMessage(jsonMessage);
	printMessage(username + ": " + messageSend + "\n");
	sendArea.setText("");
    }

    @Override
    public void setUsername(String user) {
	if (!userIsSet) {
	    this.username = user;
	}
    }

    private void startGame() {
	JSONObject jsonMessage = new JSONObject();
	printMessage("Sending Request for a New Game");
	jsonMessage.put("type", "startGame");
	protocol.sendMessage(jsonMessage);
    }

}
