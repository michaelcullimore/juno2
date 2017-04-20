package juno2;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Card extends JLabel {

    public static enum Color {
	RED, YELLOW, BLUE, GREEN
    }

    public static enum Value {
	ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, SKIP, DRAW2, REVERSE, WILD, WILDD4
    };

    /**
     *
     */
    private static final long serialVersionUID = 3156449895446853791L;;

    private Color color;

    private Value value;

    Card(Color color, Value value) {
	setColor(color);
	setValue(value);
	setImage();
    }

    private Color getColor() {
	return color;
    }

    private Value getValue() {
	return value;
    }

    private void setColor(Color color) {
	this.color = color;
    }

    private void setImage() {
	ImageIcon cardImage = new ImageIcon(
		getClass().getResource("/images/" + getColor().toString() + "-" + getValue().toString() + ".png"));
	setIcon(cardImage);
    }

    private void setValue(Value value) {
	this.value = value;
    }
}
