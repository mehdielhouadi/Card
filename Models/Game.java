package Models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Game {

    static List<Card> table;

    static int MAX_ROUNDS = 100;

    static Player player1 = new Player("Angi");
    static Player player2 = new Player("Mehdi");

    static void start(){
        Game.table = new ArrayList<>();
    }

    static void printTable(List<Card> cardsPickedByPlayer1, List<Card> cardsPickedByPlayer2) {
        System.out.println("table : {");
        System.out.print("    " + player1.name + " cards : [");
        AtomicInteger i = new AtomicInteger(1);
        cardsPickedByPlayer1.stream().map(card -> {
            return i.get() == 1 ?
                    "card" + i.getAndIncrement() + ":" + card.value + "; " :
                    "card" + i.getAndIncrement() + " ";
                })
                .forEach(System.out::print);
        System.out.println("],");
        System.out.print("    " + player2.name + " cards : [");
        AtomicInteger j = new AtomicInteger(1);
        cardsPickedByPlayer2.stream().map(card -> {
                    return j.get() == 1 ?
                            "card" + j.getAndIncrement() + ":" + card.value + "; " :
                            "card" + j.getAndIncrement() + " ";
                })
                .forEach(System.out::print);
        System.out.print("]");
        System.out.println("}");
    }

}
