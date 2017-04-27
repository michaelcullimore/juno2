package juno2;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
    private static final long serialVersionUID = -2020051570862766716L;

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

    private boolean userIsSet = false;
    private boolean gameStarted = false;

    private Hand handEast;
    private Hand handNorth;
    private Hand handWest;
    private Hand playerHandSouth;

    private HashMap<String, Hand> hands;

    private JButton sendButton;
    private JButton startButton;

    private JPanel contentPane;
    private JPanel cardPane;
    private JPanel controlPanel;
    private JPanel discardPile;
    private JPanel messagePane;

    private JScrollPane messageScroll;
    private JScrollPane inputScroll;
    private JScrollPane scrollSouth;

    private JTextArea messageArea;
    private JTextArea messageInputArea;

    private Protocol protocol;

    private String username;

    public JUnoClient2() {
	connectToServer();

	this.setTitle("JUno - " + username);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(100, 100, 900, 600);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	contentPane.setLayout(new BorderLayout());
	setContentPane(contentPane);

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

    private void drawCard() {
	JSONObject message = new JSONObject();
	message.put("action", "dealCard");
	message.put("module", "juno");
	JSONObject dealCard = new JSONObject();
	dealCard.put("type", "application");
	dealCard.put("message", message);
	protocol.sendMessage(dealCard);
	System.out.println("recieved: " + dealCard);
    }

    public Hand getHandEast() {
	return handEast;
    }

    public Hand getHandNorth() {
	return handNorth;
    }

    public Hand getHandWest() {
	return handWest;
    }

    public Hand getPlayerHandSouth() {
	return playerHandSouth;
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
	    case ("application"): {
		handleApplication(jsonMessage);
		break;
	    }
	    case ("error"): {
		printMessage(jsonMessage.getString("message"));
		break;
	    }
	    }
	}
	if (jsonMessage.has("action")) {
	    String action = jsonMessage.getString("action");
	    if (action.equals("dealCard")) {
		handleDealCard(jsonMessage);
	    }
	}
    }

    private void handleApplication(JSONObject jsonM) {
	JSONObject jsonMessage = jsonM;
	JSONObject message = jsonMessage.getJSONObject("message");
	if (message.has("type")) {
	    String type = message.getString("type");
	    switch (type) {
	    case ("reset"): {
		resetCardPane();
		break;
	    }
	    }
	}
	if (message.has("action")) {
	    String action = message.getString("action");
	    switch (action) {
	    case ("playCard"): {
		handlePlayCard(message);
		break;
	    }
	    case ("startCard"): {
		if (message.has("players")) {
		    JSONArray playersMessage = message.getJSONArray("players");
		    for (Object p : playersMessage) {
			if (p instanceof JSONObject) {
			    JSONObject player = (JSONObject) p;
			    int numCards = (int) player.get("cards");
			    for (int i = 0; i < numCards; i++) {
				handleCardDealt(player.getString("username"));
			    }
			}
		    }
		    printMessage(message.getString("turn") + "'s turn.");
		}
		JSONObject cardMessage = new JSONObject(message.getString("card"));
		Card.Color color = Card.Color.valueOf(cardMessage.getString("color"));
		Card.Value value = Card.Value.valueOf(cardMessage.getString("value"));
		Card card = new Card(color, value);
		updateDiscardPile(card);
		break;
	    }
	    case ("cardDealt"): {
		String player = message.getString("user");
		if (!player.equals(this.username)) {
		    handleCardDealt(player);
		}
		break;
	    }
	    case ("turn"): {
		String trackTurn = message.getString("user");
		if (trackTurn.equals(username)) {
		    printMessage("It's your turn.");
		} else {
		    printMessage("It's " + trackTurn + "'s turn.");
		}
		setTurn(message);
		break;
	    }
	    case ("callUno"): {
		String calledUno = message.getString("user");
		if (calledUno.equals(username)) {
		    printMessage("UNO!!!!!!");
		} else {
		    printMessage(calledUno + "called UNO!!!");
		}
		break;
	    }
	    case ("win"): {
		String winner = message.getString("username");
		if (winner.equals(username)) {
		    printMessage("You Win!!!");
		} else {
		    printMessage(winner + " won!");
		}
		break;
	    }
	    case ("quit"): {
		String quitter = message.getString("username");
		if (quitter.equals(username)) {
		    printMessage("You quit the game.");
		    resetCardPane();
		} else {
		    printMessage(quitter + " quit the game.");
		}
		break;
	    }
	    }
	}
    }

    private void handleCardDealt(String player) {
	if (hands.containsKey(player)) {
	    Hand hand = hands.get(player);
	    hand.addCard(new Card(hand.getOrientation()));
	    cardPane.updateUI();
	    return;
	} else if (!hands.containsValue(handNorth)) {
	    hands.put(player, handNorth);
	    handNorth.addCard(new Card(handNorth.getOrientation()));
	    return;
	} else if (!hands.containsValue(handWest)) {
	    hands.put(player, handWest);
	    handWest.addCard(new Card(handWest.getOrientation()));
	    cardPane.updateUI();
	    return;
	} else if (!hands.containsValue(handEast)) {
	    hands.put(player, handEast);
	    handEast.addCard(new Card(handEast.getOrientation()));
	    cardPane.updateUI();
	    return;
	}
	System.err.println("There are too many players in the game...");
    }

    public void handleChat(JSONObject jsonM) {
	JSONObject jsonMessage = jsonM;
	System.out.println(jsonMessage.toString());
	printMessage(jsonMessage.getString("fromUser") + ": " + jsonMessage.getString("message"));
    }

    public void handleDealCard(JSONObject jsonM) {
	JSONObject jsonMessage = new JSONObject(jsonM.getString("card"));
	Card.Color color = Card.Color.valueOf(jsonMessage.getString("color"));
	Card.Value value = Card.Value.valueOf(jsonMessage.getString("value"));
	Card card = new Card(color, value);
	card.addActionListener(e -> {
	    if (card.getColor() == Card.Color.WILD) {
		String[] colors = { "BLUE", "GREEN", "RED", "YELLOW" };
		String wildColor = "cancel";
		Object selection = JOptionPane.showInputDialog(null, "Choose a new Color.", "Color Selection",
			JOptionPane.QUESTION_MESSAGE, null, colors, colors[0]);
		wildColor = (String) selection;
		if (!(wildColor == null)) {
		    card.setColor(Card.Color.valueOf(wildColor));
		    playCard(card.getValue().toString(), card.getColor().toString());
		}
	    } else {
		playCard(card.getValue().toString(), card.getColor().toString());
	    }
	});
	placeCard(card);
    }

    public void handlePlayCard(JSONObject message) {
	JSONObject cardMessage = new JSONObject(message.getString("card"));
	String player = message.getString("user");
	Card.Color color = Card.Color.valueOf(cardMessage.getString("color"));
	Card.Value value = Card.Value.valueOf(cardMessage.getString("value"));
	Card card = new Card(color, value);
	updateDiscardPile(card);
	if (player.equals(this.username)) {
	    getPlayerHandSouth().removeCard(card);
	} else {
	    if (hands.containsKey(player)) {
		Hand hand = hands.get(player);
		hand.removeBlankCard();
	    }
	}
    }

    private void handleWhois(JSONObject jsonM) {
	JSONArray usernames = jsonM.getJSONObject("message").getJSONArray("username");
	if (usernames.length() == 1) {
	    printMessage("You're the only one here... Get some more people to play");
	}
	printMessage("User Online: ");
	for (int i = 0; i < usernames.length(); i++) {
	    String whois = (i + 1 + ": " + usernames.getJSONObject(i).get("username").toString());
	    String module = usernames.getJSONObject(i).getString("username").toString();
	    if (!module.isEmpty()) {
		whois += "module: " + module;
		printMessage(whois);
	    }
	}
    }

    private void initGameArea() {
	cardPane = new JPanel(new BorderLayout());

	// Player(South) hand
	playerHandSouth = new Hand(Card.CardOrientation.UP);
	playerHandSouth.setUserName(username);
	scrollSouth = new JScrollPane(playerHandSouth, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	cardPane.add(scrollSouth, "South");
	hands = new HashMap<>();
	hands.put(username, playerHandSouth);
	cardPane.updateUI();

	// East hand
	handEast = new Hand(Card.CardOrientation.LEFT);
	handEast.setLayout(new BoxLayout(handEast, BoxLayout.PAGE_AXIS));
	JScrollPane scrollEast = new JScrollPane(handEast, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	cardPane.add(scrollEast, "East");
	cardPane.updateUI();

	// North hand
	handNorth = new Hand(Card.CardOrientation.UP);
	handNorth.setLayout(new BoxLayout(handNorth, BoxLayout.LINE_AXIS));
	JScrollPane scrollNorth = new JScrollPane(handNorth, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	cardPane.add(scrollNorth, "North");
	cardPane.updateUI();

	// West hand
	handWest = new Hand(Card.CardOrientation.RIGHT);
	handWest.setLayout(new BoxLayout(handWest, BoxLayout.PAGE_AXIS));
	JScrollPane scrollWest = new JScrollPane(handWest, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	cardPane.add(scrollWest, "West");
	cardPane.updateUI();

	// Discard Pile
	discardPile = new JPanel();
	discardPile.setLayout(new GridBagLayout());
	cardPane.add(discardPile, "Center");
	contentPane.add(cardPane, "Center");
	cardPane.updateUI();

	// Control Panel
	controlPanel = new JPanel();
	controlPanel.setLayout(new FlowLayout());

	// Start Button
	JButton startButton = new JButton("Start New Game");
	startButton.addActionListener(e -> sendStartGame());
	controlPanel.add(startButton);

	// Join Button
	JButton joinButton = new JButton("Join Game");
	joinButton.addActionListener(e -> sendJoinGame());
	controlPanel.add(joinButton);

	// Reset Button
	JButton resetButton = new JButton("Reset Game");
	resetButton.addActionListener(e -> sendResetGame());
	controlPanel.add(resetButton);

	// Call Uno Button
	JButton callUnoButton = new JButton("Call Uno!");
	callUnoButton.addActionListener(e -> sendCallUno());
	controlPanel.add(callUnoButton);

	// Draw Card Button
	JButton drawCardButton = new JButton("Draw Card");
	drawCardButton.addActionListener(e -> drawCard());
	controlPanel.add(drawCardButton);

	// Quit Button
	JButton quitButton = new JButton("Quit Game");
	quitButton.addActionListener(e -> sendQuitGame());
	controlPanel.add(quitButton);
	controlPanel.updateUI();

	cardPane.add(controlPanel, "North");
	contentPane.add(cardPane, "Center");
    }

    private void initMessages() {
	messagePane = new JPanel();
	messagePane.setLayout(new BoxLayout(messagePane, BoxLayout.Y_AXIS));

	messageArea = new JTextArea();
	messageArea.setEditable(false);
	messageArea.setWrapStyleWord(true);
	messageArea.setLineWrap(true);
	messageArea.setFont(new Font("Arial", Font.PLAIN, 12));
	messagePane.add(messageArea, "North");

	messageScroll = new JScrollPane(messageArea);
	messageScroll.setSize(150, 200);
	messageScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	messagePane.add(messageScroll, "Center");

	messageInputArea = new JTextArea(3, 15);
	messageInputArea.setEditable(true);
	messageInputArea.setWrapStyleWord(true);
	messageInputArea.setLineWrap(true);
	messageInputArea.setFont(new Font("Arial", Font.PLAIN, 12));

	inputScroll = new JScrollPane(messageInputArea);
	inputScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	messagePane.add(inputScroll);
	messageInputArea.requestFocusInWindow();
	messageInputArea.addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getModifiers() == KeyEvent.CTRL_MASK) {
		    sendMessage();
		}
	    }
	});

	sendButton = new JButton("Send");
	messagePane.add(sendButton);
	sendButton.addActionListener(e -> sendMessage());

	startButton = new JButton("Start Game");
	startButton.addActionListener(e -> startGame());
	messagePane.add(startButton);

	contentPane.add(messagePane, "West");

	messagePane.setVisible(true);
    }

    private void placeCard(Card c) {
	playerHandSouth.add(c);
	scrollSouth.getViewport().setViewPosition(new java.awt.Point(20000, 0));
	cardPane.updateUI();
    }

    private void playCard(String col, String val) {
	JSONObject cardMessage = new JSONObject();
	cardMessage.put("color", col);
	cardMessage.put("value", val);

	JSONObject action = new JSONObject();
	action.put("action", "playCard");
	action.put("card", cardMessage);
	action.put("module", "juno");

	JSONObject message = new JSONObject();
	message.put("type", "application");
	message.put("message", action);

	protocol.sendMessage(message);

	System.out.println("sent: " + message);
    }

    public void printMessage(String message) {
	messageArea.append(message + "\n");
	messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    public void resetCardPane() {
	cardPane.removeAll();
	initGameArea();
	cardPane.updateUI();
    }

    private void sendCallUno() {
	JSONObject unoMessage = new JSONObject();
	unoMessage.put("type", "application");
	JSONObject message = new JSONObject();
	message.put("action", "callUno");
	message.put("user", username);
	message.put("module", "juno");
	unoMessage.put("message", message);
	protocol.sendMessage(unoMessage);
    }

    private void sendJoinGame() {
	resetCardPane();
	printMessage("Attempting to join a game...\n");
	JSONObject message = new JSONObject();
	message.put("type", "application");
	JSONObject action = new JSONObject();
	action.put("action", "joinGame");
	action.put("module", "juno");
	message.put("message", action);
	protocol.sendMessage(message);
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

	// whisper command
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

    public void sendQuitGame() {
	JSONObject message = new JSONObject();
	message.put("type", "application");
	JSONObject action = new JSONObject();
	action.put("action", "quit");
	action.put("module", "juno");
	message.put("message", action);
	protocol.sendMessage(message);
    }

    private void sendResetGame() {
	JSONObject message = new JSONObject();
	message.put("type", "application");
	JSONObject action = new JSONObject();
	action.put("action", "reset");
	action.put("module", "juno");
	message.put("message", action);
	System.out.println("sent: " + message);
	protocol.sendMessage(message);
    }

    private void sendStartGame() {
	resetCardPane();
	printMessage("New game request sent...\n");
	JSONObject message = new JSONObject();
	message.put("type", "application");
	JSONObject action = new JSONObject();
	action.put("action", "startGame");
	action.put("module", "juno");
	message.put("message", action);
	protocol.sendMessage(message);
    }

    private void setTurn(JSONObject jsonM) {
	JSONObject jsonMessage = jsonM;
	String user = jsonMessage.getString("user");
	if (hands.containsKey(user)) {
	    for (Entry<String, Hand> h : hands.entrySet()) {
		h.getValue().resetTurnHighlight();
	    }
	    hands.get(user).setTurnHighlight();
	}
    }

    @Override
    public void setUsername(String user) {
	if (!userIsSet) {
	    this.username = user;
	}
    }

    private void startGame() {
	resetCardPane();

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

    public void updateDiscardPile(Card card) {
	discardPile.removeAll();
	discardPile.add(card);
	discardPile.updateUI();
    }

}
