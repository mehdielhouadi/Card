package Models;

public class Card {

    Integer value;
    Integer index;

    public Card(Integer value, Integer index) {
        this.value = value;
        this.index = index;
    }

    public Card(Integer value) {
        this.value = value;
    }

    public Card() {
    }

    @Override
    public String toString() {
        return "Card{" +
                "value=" + value +
                ", index=" + index +
                '}';
    }
}
