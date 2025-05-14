package Models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
        try {
            if (this.exceededShuffles) {
                sendToPlayerInTurn(this.name + " you have : " + this.hand.size() + " cards");
                sendToPlayerInTurn("you have "+ (MAX_SHUFFLES_AFTER_EXCEEDED - this.numberOfShufflesAfterExceeded) +" shuffle left");
                return cardPickedAfterExceededShuffles();
            }
            else {
                sendToPlayerInTurn(this.name + " you have : " + this.hand.size() + " cards");
                sendToPlayerInTurn("you have " + (MAX_SHUFFLES - this.numberOfShuffles) + " shuffles left");
                return cardPicked();
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    Card cardPicked() throws IOException, ExecutionException, InterruptedException {

        //this.hand.stream().map(card -> (hand.indexOf(card) + 1) + ":" + card.value).forEach(s -> System.out.print(s + " "));
        sendToPlayerNotInTurn("waiting for the other player to play");
        sendToPlayerInTurn("Current card : " + this.hand.get(0).value);
        sendToPlayerInTurn("press p to play card " + this.hand.get(0).value);
        if (MAX_SHUFFLES - this.numberOfShuffles > 0) sendToPlayerInTurn("press s to shuffle");
        String choice = "";
        if (this.equals(Game.player1)) {
            choice = GameServer.executorService.submit(GameServer.getGameServerConnection1()).get();
        }
        if (this.equals(Game.player2)) {
            choice = GameServer.executorService.submit(GameServer.getGameServerConnection2()).get();
        }
        Card firstCard = this.hand.get(0);
        // if still has more shuffles
        if (MAX_SHUFFLES - this.numberOfShuffles > 0) {
            switch (choice) {
                case "p" -> {
                    Game.table.add(firstCard);
                    this.hand.remove(0);
                }
                case "s" -> {
                    Collections.shuffle(this.hand);
                    numberOfShuffles++;
                    keepRound();
                    return putInTable();
                }
                default -> {
                    GameServerUtils.sendToPlayers("invalid choice, please play your card or shuffle");
                    keepRound();
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
                    GameServerUtils.sendToPlayers("invalid choice, please play your card");
                    keepRound();
                    return putInTable();
                }
            }
        }
        sendToPlayerInTurn("card picked " + firstCard.value +"\n");
        return firstCard;
    }
    Card cardPickedAfterExceededShuffles() throws ExecutionException, InterruptedException {
        //this.hand.stream().map(card -> (hand.indexOf(card) + 1) + ":" + card.value).forEach(s -> System.out.print(s + " "));
        GameServerUtils.sendToPlayers("Current card : "+this.hand.get(0).value);
        GameServerUtils.sendToPlayers("press p to play card " + this.hand.get(0).value);
        if (MAX_SHUFFLES_AFTER_EXCEEDED - this.numberOfShufflesAfterExceeded > 0) GameServerUtils.sendToPlayers("press s to shuffle");
        String choice = "";
        if (this.equals(Game.player1)) {
            choice = GameServer.executorService.submit(GameServer.getGameServerConnection1()).get();
        }
        if (this.equals(Game.player2)) {
            choice = GameServer.executorService.submit(GameServer.getGameServerConnection2()).get();
        }
        Card firstCard = this.hand.get(0);

        if (MAX_SHUFFLES_AFTER_EXCEEDED - this.numberOfShufflesAfterExceeded > 0) {
            switch (choice) {
                case "p" -> {
                    Game.table.add(firstCard);
                    this.hand.remove(0);
                }
                case "s" -> {
                    Collections.shuffle(this.hand);
                    numberOfShufflesAfterExceeded++;
                    keepRound();
                    return putInTable();
                }
                default -> {
                    GameServerUtils.sendToPlayers("invalid choice, please play your card or shuffle");
                    keepRound();
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
                    GameServerUtils.sendToPlayers("invalid choice, please play your card");
                    keepRound();
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
        GameServerUtils.sendToPlayers(this.name + " picked the table");
        return tablePicked;
    }
    public Player(String name) {
        this.name = name;
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

    public void keepRound() {
        if (this.equals(Game.player1)) {
            GameServerUtils.sendToPlayer1("YOUR_TURN");
        }
        if (this.equals(Game.player2)) {
            GameServerUtils.sendToPlayer2("YOUR_TURN");
        }
    }
    public void sendToPlayerInTurn(String msg){
        if (this.equals(Game.player1)) {
            GameServerUtils.sendToPlayer1(msg);
        } else {
            GameServerUtils.sendToPlayer2(msg);
        }
    }

    public void sendToPlayerNotInTurn(String msg){
        if (this.equals(Game.player1)) {
            GameServerUtils.sendToPlayer2(msg);
        } else {
            GameServerUtils.sendToPlayer1(msg);
        }
    }

}
