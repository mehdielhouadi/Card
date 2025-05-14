package Models;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Models.Game.player1;
import static Models.Game.player2;
import static Models.GameServerUtils.closeAll;
import static Models.GameServerUtils.sendToPlayer1;
import static Models.GameServerUtils.sendToPlayer2;
import static Models.GameServerUtils.sendToPlayers;
import static Models.GameServerUtils.sendWONewLine;

public class GameServer {
    private static ServerSocket serverSocket;
    static public void initServer() {
        try {
            serverSocket = new ServerSocket(1234);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static List<GameServerConnection> gameServerConnections = new ArrayList<>();
    private static GameServerConnection gameServerConnection1;
    private static GameServerConnection gameServerConnection2;
    public static ExecutorService executorService = Executors.newFixedThreadPool(4);
    public static GameServerConnection getGameServerConnection1() {
        return gameServerConnection1;
    }
    public static GameServerConnection getGameServerConnection2() {
        return gameServerConnection2;
    }
    public static List<GameServerConnection> getGameServerConnections() {
        return gameServerConnections;
    }

    static class GameServerConnection implements Callable<String> {
        private static int numberOfPlayers = 0;
        private Socket socket;
        private BufferedReader bReader;
        private BufferedWriter bWriter;
        private static Player player1;
        private static Player player2;

        public Socket getSocket() {
            return socket;
        }

        public BufferedReader getbReader() {
            return bReader;
        }

        public BufferedWriter getbWriter() {
            return bWriter;
        }

        public GameServerConnection(Socket socket) throws IOException {
            numberOfPlayers++;
            this.socket = socket;
            this.bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String name = bReader.readLine();
            Player player = new Player(name);
            if (numberOfPlayers == 1) {
                GameServer.gameServerConnection1 = this;
                player1 = player;
                Game.player1 = player1;
            }
            if (numberOfPlayers == 2) {
                GameServer.gameServerConnection2 = this;
                player2 = player;
                Game.player2 = player2;
            }
        }

        public static void connectPlayers() throws InterruptedException {
            while (numberOfPlayers < 2) {
                try {
                    Socket socket = serverSocket.accept();
                    new GameServerConnection(socket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            gameServerConnections = List.of(gameServerConnection1, gameServerConnection2);
        }

        @Override
        public String call() {
            if (socket.isConnected()) {
                try {
                    if (Game.isStarted()) {
                        String playerChoice = bReader.readLine();
                        return playerChoice;
                    }
                    else {
                        return "wait for game to start";
                    }
                } catch (IOException e) {
                    closeAll(socket, bWriter, bReader);
                }
            }
            return "player choice error";
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        initServer();
        GameServer.GameServerConnection.connectPlayers();
        Collections.shuffle(Deck.allCards);
        player1.instantiateHand(1);
        player2.instantiateHand(2);
        sendToPlayers("Game has started\n");
        int round = 0;

        A : while (round <= Game.MAX_ROUNDS) {
            System.out.println();
            round++;
            if (round <= Game.MAX_ROUNDS) sendToPlayers("this is round " + round + "\n");
            if (round > Game.MAX_ROUNDS) {
                sendToPlayers("exceeded number of rounds");
                sendToPlayers(player1.hand.size() > player2.hand.size() ? player1.name + " won" : player2.name + " won");
                break;
            }

            if(player1.hand.isEmpty()) {
                sendToPlayers(player2.name + " won");
                break A;
            }

            if(player2.hand.isEmpty()) {
                sendToPlayers(player1.name + " won");
                break A;
            }
            Game.start();

            sendToPlayer1("YOUR_TURN");
            Card cardPickedByPlayer1 = player1.putInTable();
            sendToPlayers("card picked " + cardPickedByPlayer1.value);
            sendToPlayers("\n");
            sendToPlayer2("YOUR_TURN");
            Card cardPickedByPlayer2 = player2.putInTable();
            sendToPlayers("card picked " + cardPickedByPlayer2.value);
            sendToPlayers("\n");
            sendWONewLine("current table : { ") ;
            Game.table.stream().map(card -> card.value)
                    .forEach(integer -> sendWONewLine(integer + " "));
            sendToPlayers("}");

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


}
