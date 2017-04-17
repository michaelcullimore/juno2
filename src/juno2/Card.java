package juno2;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Card extends JLabel {

    public static enum Color {
	RED, YELLOW, BLUE, GREEN
    };

    public static enum Value {
	ZERO("00"), ONE("01"), TWO("02"), THREE("03"), FOUR("04"), FIVE("05"), SIX("06"), SEVEN("07"), EIGHT(
		"08"), NINE("09"), SKIP("S"), DRAW2("D2"), REVERSE("R"), WILD("00"), WILDD4("D4");

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
	// TODO Auto-generated method stub

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
	// TODO Auto-generated method stub
	ImageIcon cardImage = new ImageIcon(getClass().getResource("/resources/cardImages/red-00.png"));
	setIcon(cardImage);
    }

    private void setValue(Value value) {
	this.value = value;
    }
}
