package juno2;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import juno2.Card.CardOrientation;

public class Hand extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 2437792967833168998L;

    private ArrayList<Card> cards;
    private CardOrientation orientation;
    private String username;

    public Hand(CardOrientation co) {
	cards = new ArrayList<>();
	setOrientation(co);
    }

    public void addCard(Card ac) {
	add(ac);
	cards.add(ac);
	updateUI();
    }

    public CardOrientation getOrientation() {
	return orientation;
    }

    public String getUsername() {
	return username;
    }

    public void removeBlankCard() {
	if (!cards.isEmpty()) {
	    this.remove(cards.get(0));
	    cards.remove(0);
	}

    }

    public void removeCard(Card card) {
	System.out.println("removing card via removeCard()");
	int index = 0;
	for (Card c : cards) {
	    if (card.equals(c)) {
		this.remove(cards.remove(index));
		this.updateUI();
		break;
	    }
	    index++;
	}
    }

    public void resetTurnHighlight() {
	this.setBackground(null);

    }

    private void setOrientation(CardOrientation co) {
	orientation = co;
    }

    public void setTurnHighlight() {
	this.setBackground(Color.LIGHT_GRAY);
    }

    public void setUserName(String user) {
	this.username = user;
	this.setBorder(new TitledBorder(username));
    }

}
