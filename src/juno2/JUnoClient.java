package juno2;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
	// TODO Auto-generated method stub
	EventQueue.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		try {
		    JUnoClient client = new JUnoClient();
		    client.setVisible(true);

		} catch (Exception e) {
		    // TODO: handle exception
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
	// TODO Auto-generated constructor stub
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
	// TODO Auto-generated method stub
	try {
	    protocol = new Protocol(this);
	} catch (Exception e) {
	    // TODO: handle exception
	    System.err.println("Error in protocol setup!");
	    e.printStackTrace();
	}
    }

    @Override
    public void giveMessage(JSONObject jsonM) {
	// TODO Auto-generated method stub
	JSONObject message = jsonM;
	if (message.getString("type").equals("chat")) {// could be "message"
	    handleMessages(message);
	}

    }

    public void handleMessages(JSONObject jsonM) {
	JSONObject message = jsonM;
	System.out.println(message.toString());
	messageArea.append(message.getString("fromUser") + ": " + message.getString("message") + "\n");
    }

    private void initChat() {
	// TODO Auto-generated method stub
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
	// TODO Auto-generated method stub
	JPanel gamePanel = new JPanel(new BorderLayout());
	JButton startButton = new JButton("Start Game");
	startButton.addActionListener(e -> startGame());

	// tests whether or not the picture of the card comes through
	Card cardTest1 = new Card(Card.Color.RED, Card.Value.ZERO);
	gamePanel.add(cardTest1, "Center");
	gamePanel.add(startButton, "South");
	contentPanel.add(gamePanel, "Center");

    }

    protected void sendMessage() {
	// TODO Auto-generated method stub
	System.out.println("Execution of sendText() complete.");
	String messageSend;
	messageSend = sendArea.getText();
	JSONObject message = new JSONObject();
	message.put("type", "chat");
	message.put("message", messageSend);
	protocol.sendMessage(message);
	messageArea.append(username + ": " + messageSend + "\n");
	sendArea.setText("");
    }

    @Override
    public void setUsername(String user) {
	// TODO Auto-generated method stub
	if (!userIsSet) {
	    this.username = user;
	}
    }

    private void startGame() {
	// TODO Auto-generated method stub
	messageArea.append("Sending Request for a New Game");
	JSONObject message = new JSONObject();
	message.put("type", "startGame");
	protocol.sendMessage(message);
    }

}
