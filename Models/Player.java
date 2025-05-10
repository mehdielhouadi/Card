package Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Player {

    String name;

    List<Card> hand;

    int numberOfShuffles = 0;

    int numberOfShufflesAfterExceeded = 0;

    static final int MAX_SHUFFLES = 5;

    static final int MAX_SHUFFLES_AFTER_EXCEEDED = 1;

    boolean exceededShuffles = false;

    // picks 24 random cards from deck
    List<Card> instantiateHand(int playerNumber) {
        hand = new ArrayList<>(playerNumber == 1 ? Deck.allCards.subList(0,24) : Deck.allCards.subList(24,48));
        return hand;
    }

    // puts in table and removes from hand
    Card putInTable() {

        if (this.exceededShuffles) {
            System.out.println(this.name + " you have : " + this.hand.size() + " cards");
            System.out.println("you have "+ (MAX_SHUFFLES_AFTER_EXCEEDED - this.numberOfShufflesAfterExceeded) +" shuffle left");
            return cardPickedAfterExceededShuffles();
        }
        else {
            System.out.println(this.name + " you have : " + this.hand.size() + " cards");
            System.out.println("you have " + (MAX_SHUFFLES - this.numberOfShuffles) + " shuffles left");
            return cardPicked();
        }
    }


    Card cardPicked() {
        //this.hand.stream().map(card -> (hand.indexOf(card) + 1) + ":" + card.value).forEach(s -> System.out.print(s + " "));
        System.out.println("Current card : "+this.hand.get(0).value);
        Scanner scanner = new Scanner(System.in);
        System.out.println("press p to play card " + this.hand.get(0).value);
        if (MAX_SHUFFLES - this.numberOfShuffles > 0) System.out.println("press s to shuffle");
        String choice = scanner.nextLine();
        Card firstCard = this.hand.get(0);
        // if still has more shuffles
        if (MAX_SHUFFLES - this.numberOfShuffles > 0) {
            switch (choice) {
                case "p" -> {
                    Game.table.add(firstCard);
                    this.hand.remove(0);
                }
                case "s" -> {
                    if (numberOfShuffles < MAX_SHUFFLES){
                        Collections.shuffle(this.hand);
                        numberOfShuffles++;
                        return putInTable();
                    }
                    else {
                        exceededShuffles = true;
                        System.out.println("limit number of shuffles exceeded, first was card of value "
                                + firstCard + "was put in table ");
                        Game.table.add(firstCard);
                        this.hand.remove(0);
                    }
                }
                default -> {
                    System.out.println("invalid choice, please play your card or shuffle");
                    return putInTable();
                }
            }
            return firstCard;
        }
        // if no more shuffles
        else {
            switch (choice) {
                case "p" -> {
                    Game.table.add(firstCard);
                    this.hand.remove(0);
                    exceededShuffles = true;
                }
                default -> {
                    System.out.println("invalid choice, please play your card");
                    return putInTable();
                }
            }
        }
        return firstCard;
    }
    Card cardPickedAfterExceededShuffles() {

        //this.hand.stream().map(card -> (hand.indexOf(card) + 1) + ":" + card.value).forEach(s -> System.out.print(s + " "));
        System.out.println("Current card : "+this.hand.get(0).value);
        Scanner scanner = new Scanner(System.in);
        System.out.println("press p to play card " + this.hand.get(0).value);
        if (MAX_SHUFFLES_AFTER_EXCEEDED - this.numberOfShufflesAfterExceeded > 0) System.out.println("press s to shuffle");
        String choice = scanner.nextLine();
        Card firstCard = this.hand.get(0);

        if (MAX_SHUFFLES_AFTER_EXCEEDED - this.numberOfShufflesAfterExceeded > 0) {
            switch (choice) {
                case "p" -> {
                    Game.table.add(firstCard);
                    this.hand.remove(0);
                }
                case "s" -> {
                    if (numberOfShufflesAfterExceeded < MAX_SHUFFLES_AFTER_EXCEEDED){
                        Collections.shuffle(this.hand);
                        numberOfShufflesAfterExceeded++;
                        return putInTable();
                    }
                    else {
                        exceededShuffles = true;
                        System.out.println("limit number of shuffles exceeded, first was card of value "
                                + firstCard + "was put in table ");
                        Game.table.add(firstCard);
                        this.hand.remove(0);
                    }
                }
                default -> {
                    System.out.println("invalid choice, please play your card or shuffle");
                    return putInTable();
                }
            }
        }
        // if no more shuffles
        else {
            switch (choice) {
                case "p" -> {
                    Game.table.add(firstCard);
                    this.hand.remove(0);
                }
                default -> {
                    System.out.println("invalid choice, please play your card");
                    return putInTable();
                }
            }
            this.numberOfShufflesAfterExceeded = 0;
        }
        return firstCard;
    }

    // picks table and clears it
    List<Card> pickTable() {
        List<Card> tablePicked = new ArrayList<>();
        this.hand.addAll(Game.table);
        tablePicked.addAll(Game.table);
        Game.table.clear();
        System.out.println(this.name + " picked the table");
        return tablePicked;
    }

    public Player(String name) {
        this.name = name;
    }

    public Player() {
    }

    // put in table 4 cards in war and removes them from player's hand,
    // if not enough cards for a player, puts only one (the player will lose it)
    List<Card> putInMiddleInWar(){
        if (this.hand.size() < 4) {
            Game.table.add(hand.get(0));
            Card card = hand.get(0);
            hand.remove(0);
            return List.of(card);
        }

        List<Card> cardsPut = new ArrayList<>();
        for(int i = 3; i >= 0; i--) {
            Game.table.add(hand.get(i));
            cardsPut.add(hand.get(i));
            this.hand.remove(i);
        }
        return cardsPut;
    }



}
