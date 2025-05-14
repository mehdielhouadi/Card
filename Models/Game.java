package Models;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static Models.GameServerUtils.sendToPlayers;

public class Game {

    static List<Card> table;

    static int MAX_ROUNDS = 100;
    static boolean started = false;

    public static boolean isStarted() {
        return started;
    }

    static Player player1;
    static Player player2;

    static void start(){
        Game.table = new ArrayList<>();
        started = true;
    }

    static void printTable(List<Card> cardsPickedByPlayer1, List<Card> cardsPickedByPlayer2) {
        sendToPlayers("table : {");
        sendToPlayers("    " + player1.name + " cards : [");
        AtomicInteger i = new AtomicInteger(1);
        cardsPickedByPlayer1.stream().map(card -> i.get() == 1 ?
                "card" + i.getAndIncrement() + ":" + card.value + "; " :
                "card" + i.getAndIncrement() + " ")
                .forEach(GameServerUtils::sendWONewLine);
        System.out.println("],");
        sendToPlayers("    " + player2.name + " cards : [");
        AtomicInteger j = new AtomicInteger(1);
        cardsPickedByPlayer2.stream().map(card -> j.get() == 1 ?
                "card" + j.getAndIncrement() + ":" + card.value + "; " :
                "card" + j.getAndIncrement() + " ")
                .forEach(GameServerUtils::sendWONewLine);
        System.out.print("]");
        System.out.println("}");
    }

}
