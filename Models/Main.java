package Models;

import org.w3c.dom.ls.LSOutput;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import static Models.Game.player1;
import static Models.Game.player2;

public class Main {

    public static void main(String[] args) {

        promptPlayersNames();

        Collections.shuffle(Deck.allCards);
        player1.instantiateHand(1);
        player2.instantiateHand(2);
        System.out.println("Game has started\n");
        int round = 0;

        A : while (round <= Game.MAX_ROUNDS) {
            System.out.println();
            round++;
            if(round <= Game.MAX_ROUNDS) System.out.println("this is round " + round + "\n");
            if (round > Game.MAX_ROUNDS) {
                System.out.println("exceeded number of rounds");
                System.out.println(player1.hand.size() > player2.hand.size() ? player1.name + " won" : player2.name + " won");
                break;
            }

            if(player1.hand.isEmpty()) {
                System.out.println(player2.name + " won");
                break A;
            }

            if(player2.hand.isEmpty()) {
                System.out.println(player1.name + " won");
                break A;
            }
            Game.start();

            Card cardPickedByPlayer1 = player1.putInTable();
            System.out.println("card picked " + cardPickedByPlayer1.value);
            System.out.println();
            Card cardPickedByPlayer2 = player2.putInTable();
            System.out.println("card picked " + cardPickedByPlayer2.value);
            System.out.println();
            System.out.print("current table : { ") ;
            Game.table.stream().map(card -> card.value).forEach(integer -> System.out.print(integer + " "));
            System.out.println("}");

            if(cardPickedByPlayer1.value > cardPickedByPlayer2.value){
                player1.pickTable();
                System.out.println();
            }
            if(cardPickedByPlayer2.value > cardPickedByPlayer1.value){
                player2.pickTable();
                System.out.println();
            }
            if(cardPickedByPlayer1.value == cardPickedByPlayer2.value){
                B : while (true){
                    if(player1.hand.isEmpty()) {
                        System.out.println(player2.name + " won");
                        break A;
                    }
                    if(player2.hand.isEmpty()) {
                        System.out.println(player1.name + " won");
                        break A;
                    }
                    List<Card> cardsPickedByP1 = player1.putInMiddleInWar();
                    List<Card> cardsPickedByP2 = player2.putInMiddleInWar();
                    if (cardsPickedByP1.size() == 1) {
                        Game.printTable(cardsPickedByP1, cardsPickedByP2);
                        player2.pickTable();
                        break B;
                    }
                    else if (cardsPickedByP2.size() == 1) {
                        Game.printTable(cardsPickedByP1, cardsPickedByP2);
                        player1.pickTable();
                        break B;
                    }
                    else {
                        if (cardsPickedByP2.get(0).value > cardsPickedByP1.get(0).value ){
                            Game.printTable(cardsPickedByP1, cardsPickedByP2);
                            player2.pickTable();
                            break B;
                        }
                        else if (cardsPickedByP2.get(0).value < cardsPickedByP1.get(0).value ){
                            Game.printTable(cardsPickedByP1, cardsPickedByP2);
                            player1.pickTable();
                            break B;
                        }
                    }
                }
            }
        }



    }

    private static void promptPlayersNames() {
        Scanner sc = new Scanner(System.in);
        System.out.println("player 1 enter name : ");
        String player1Name = sc.nextLine();
        Game.player1 = new Player(player1Name);
        System.out.println("player 2 enter name : ");
        String player2Name = sc.nextLine();
        Game.player2 = new Player(player2Name);
    }


}
