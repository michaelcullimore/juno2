package juno2;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Card extends JButton {

    static enum CardOrientation {
	LEFT, RIGHT, UP
    }

    static enum Color {
	RED, YELLOW, BLUE, GREEN, WILD
    };

    static enum Value {
	ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, SKIP, DRAW2, REVERSE, WILD, WILDD4
    };

    /**
     *
     */
    private static final long serialVersionUID = 8335247149697385961L;;

    private CardOrientation orientation;
    private Color color;
    private Value value;

    Card(CardOrientation o) {
	setOrietation(o);
	setBackImage();

    }

    Card(Color color, Value value) {
	setColor(color);
	setValue(value);
	setImage();
	setBorder(BorderFactory.createEmptyBorder());
    }

    public boolean equals(Card card) {
	return ((this.getColor().equals(card.getColor())) && ((this.getValue().equals(card.getValue()))));

    }

    Color getColor() {
	return color;
    }

    private CardOrientation getOrientation() {
	return orientation;
    }

    Value getValue() {
	return value;
    }

    @Override
    public int hashCode() {
	return super.hashCode();
    }

    private void setBackImage() {
	ImageIcon cardImage = new ImageIcon(
		getClass().getResource("/images/back" + "-" + getOrientation().toString().toLowerCase() + ".png"));
	setIcon(cardImage);
    }

    void setColor(Color color) {
	this.color = color;
    }

    private void setImage() {
	ImageIcon cardImage = new ImageIcon(
		getClass().getResource("/images/" + getColor().toString() + "-" + getValue().toString() + ".png"));
	setIcon(cardImage);
    }

    private void setOrietation(CardOrientation orientation) {
	this.orientation = orientation;
    }

    private void setValue(Value value) {
	this.value = value;
    }

}
