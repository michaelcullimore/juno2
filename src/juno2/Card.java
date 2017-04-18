package juno2;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Card extends JLabel {

    public static enum Color {
	RED, YELLOW, BLUE, GREEN
    };

    public static enum Value {
	ZERO("0"), ONE("1"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE(
		"9"), SKIP("S"), DRAW2("D2"), REVERSE("R"), WILD("W"), WILDD4("D4");

	private String numVal;

	Value(String numVal) {
	    this.numVal = numVal;
	}

	public String getNumVal() {
	    return numVal;
	}
    };

    private static final long serialVersionUID = 9;

    public static void main(String[] args) {

    }

    private Color color;

    private Value value;

    public Card(Color color, Value value) {
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
	ImageIcon cardImage = new ImageIcon(getClass().getResource("/resources/cardImages/red-00.png"));
	setIcon(cardImage);
    }

    private void setValue(Value value) {
	this.value = value;
    }
}
