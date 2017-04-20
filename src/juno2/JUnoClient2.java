package juno2;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONObject;

import junoServer.Protocol;
import junoServer.Receivable;

public class JUnoClient2 extends JFrame implements Receivable {

    /**
     *
     */
    private static final long serialVersionUID = -4953493595169902287L;

    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		try {
		    JUnoClient2 client = new JUnoClient2();
		    client.setVisible(true);

		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    private JPanel contentPane;
    private JTextArea messageArea;
    private JTextArea messageInputArea;
    private Protocol protocol;
    private String username;

    private boolean userIsSet = false;
    private boolean gameStarted = false;
    private JPanel cardPane;
    private JPanel playerHand;
    private JPanel cpuHand1;
    private JPanel cpuHand2;
    private JPanel cpuHand3;
    private JPanel controlPanel;
    private JButton joinGameButton;

    public JUnoClient2() {
	connectToServer();

	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(100, 100, 800, 600);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	contentPane.setLayout(new BorderLayout());
	setContentPane(contentPane);
	contentPane.setLayout(new BorderLayout());

	initGameArea();
	initMessages();
    }

    private void connectToServer() {
	try {
	    protocol = new Protocol(this);
	} catch (IOException e) {
	    System.err.println("Error in protocol setup!!!");
	    e.printStackTrace();
	}
    }

    @Override
    public void giveMessage(JSONObject jsonM) {
	JSONObject jsonMessage = jsonM;
	if (jsonMessage.has("type")) {
	    String type = jsonMessage.getString("type");
	    switch (type) {
	    case ("chat"): {
		handleChat(jsonMessage);
		break;
	    }
	    case ("whois"): {
		handleWhois(jsonMessage);
		break;
	    }
	    }
	}
	if (jsonMessage.has("action")) {
	    String action = jsonMessage.getString("action");
	    switch (action) {
	    case ("dealCard"): {
		handleDealCard(jsonMessage);
		break;
	    }
	    }
	}
    }

    public void handleChat(JSONObject jsonM) {
	JSONObject jsonMessage = jsonM;
	System.out.println(jsonMessage.toString());
	printMessage(jsonMessage.getString("fromUser") + ": " + jsonMessage.getString("message"));
    }

    private void handleDealCard(JSONObject jsonM) {
	JSONObject jsonMessage = new JSONObject(jsonM.getString("card"));
	Card.Color color = Card.Color.valueOf(jsonMessage.getString("color"));
	Card.Value value = Card.Value.valueOf(jsonMessage.getString("value"));
	Card card = new Card(color, value);
	placeCard(card, playerHand);
    }

    private void handleWhois(JSONObject jsonM) {
	JSONArray usernames = jsonM.getJSONObject("message").getJSONArray("username");
	if (usernames.length() == 1) {
	    printMessage("You're the only one here... Get some more people to play");
	}
	printMessage("User Online: ");
	for (int i = 0; i < usernames.length(); i++) {
	    printMessage((i + 1) + ": " + usernames.getJSONObject(i).get("username").toString());
	}
    }

    private void initGameArea() {
	cardPane = new JPanel(new BorderLayout());

	// tests whether or not the picture of the card comes through
	// Card cardTest1 = new Card(Card.Color.RED, Card.Value.ZERO);
	// gamePanel.add(cardTest1, "Center");

	contentPane.add(cardPane, "East");
	controlPanel = new JPanel();
	controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
	cardPane.add(controlPanel, "North");
	playerHand = new JPanel(new FlowLayout());
	cardPane.add(playerHand, "South");
    }

    private void initMessages() {
	JPanel messagePane = new JPanel();
	messagePane.setLayout(new BoxLayout(messagePane, BoxLayout.Y_AXIS));

	messageArea = new JTextArea();
	messageArea.setEditable(false);
	messageArea.setWrapStyleWord(true);
	messageArea.setLineWrap(true);
	messageArea.setFont(new Font("Arial", Font.PLAIN, 16));
	messagePane.add(messageArea, "North");

	JScrollPane messageScroll = new JScrollPane(messageArea);
	messageScroll.setSize(150, 200);
	messageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	messagePane.add(messageScroll, "Center");

	messageInputArea = new JTextArea(3, 15);
	messageInputArea.setEditable(true);
	messageInputArea.setWrapStyleWord(true);
	messageInputArea.setLineWrap(true);
	messageInputArea.setFont(new Font("Arial", Font.PLAIN, 16));

	JScrollPane inputScroll = new JScrollPane(messageInputArea);
	inputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	messagePane.add(inputScroll);//
	messageInputArea.requestFocusInWindow();
	messageInputArea.addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiers() == KeyEvent.CTRL_MASK) {
		    sendMessage();
		}
	    }
	});

	JButton send = new JButton("Send");
	messagePane.add(send);
	send.addActionListener(e -> sendMessage());
	JButton startButton = new JButton("Start Game");
	startButton.addActionListener(e -> startGame());
	messagePane.add(startButton);
	// messagePane.add(messagePane, "South");
	contentPane.add(messagePane, "West");

	joinGameButton = new JButton("Join Game");
	joinGameButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
	    }
	});
	cardPane.add(joinGameButton, BorderLayout.NORTH);
	messagePane.setVisible(true);

    }

    private void placeCard(Card c, JPanel hand) {
	hand.add(c);
	cardPane.updateUI();
    }

    public void printMessage(String message) {
	messageArea.append(message + "\n");
	messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    private void sendMessage() {
	JSONObject jsonMessage = new JSONObject();
	String messageSend;
	messageSend = messageInputArea.getText();

	if (messageSend.equals("\\whois")) {
	    jsonMessage.put("type", "whois");
	    protocol.sendMessage(jsonMessage);
	    messageInputArea.setText("");
	    printMessage("The \"whois\" function has been requested.");
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
	messageInputArea.setText("");
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
	jsonMessage.put("type", "application");
	JSONObject action = new JSONObject();
	if (gameStarted) {
	    action.put("action", "joinGame");
	} else {
	    action.put("action", "startGame");
	}
	action.put("module", "juno");
	jsonMessage.put("message", action);
	protocol.sendMessage(jsonMessage);
    }

}
