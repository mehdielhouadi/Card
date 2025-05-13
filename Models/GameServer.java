package Models;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Models.Game.player1;
import static Models.Game.player2;

public class GameServer {
    private static ServerSocket serverSocket;
    static public void initServer() {
        try {
            serverSocket = new ServerSocket(1234);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static GameServerConnection gameServerConnection1;
    private static GameServerConnection gameServerConnection2;
    public static ExecutorService executorService = Executors.newFixedThreadPool(4);
    public static int turn = 1;
    public static GameServerConnection getGameServerConnection1() {
        return gameServerConnection1;
    }

    public static GameServerConnection getGameServerConnection2() {
        return gameServerConnection2;
    }

    public static List<GameServerConnection> getGameServerConnections() {
        return gameServerConnections;
    }

    private static List<GameServerConnection> gameServerConnections = new ArrayList<>();
    static class GameServerConnection implements Callable<String> {
        private static int numberOfPlayers = 0;
        private Socket socket;
        private BufferedReader bReader;
        private BufferedWriter bWriter;
        private static Player player1;
        private static Player player2;

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
            if (numberOfPlayers == 1) GameServer.gameServerConnection1 = this;
            if (numberOfPlayers == 2) GameServer.gameServerConnection2 = this;
            String name = bReader.readLine();
            if (numberOfPlayers == 1) player1 = new Player(name);
            if (numberOfPlayers == 2) player2 = new Player(name);
            if (numberOfPlayers == 1) Game.player1 = player1;
            if (numberOfPlayers == 2) Game.player2 = player2;
            if (numberOfPlayers == 1) GameServerConnection.player1 = player1;
            if (numberOfPlayers == 2) GameServerConnection.player2 = player2;
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
        public void closeAll(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (socket != null) {
                    socket.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void sendToPlayers(String msg) {
            for (GameServerConnection gsc : GameServer.gameServerConnections) {
                try {
                    gsc.bWriter.write(msg);
                    gsc.bWriter.newLine();
                    gsc.bWriter.flush();
                } catch (IOException e) {
                closeAll(socket, bWriter, bReader);
                }
            }
        }
        public void sendWONewLine(String msg) {
            for (GameServerConnection gsc : GameServer.gameServerConnections) {
                try {
                    gsc.bWriter.write(msg);
                    gsc.bWriter.flush();
                } catch (IOException e) {
                closeAll(socket, bWriter, bReader);
                }
            }
        }
        public void sendToPlayer1(String msg) {
            try {
                gameServerConnection1.bWriter.write(msg);
                gameServerConnection1.bWriter.newLine();
                gameServerConnection1.bWriter.flush();
            } catch (IOException e) {
            closeAll(socket, bWriter, bReader);
            }

        }
        public void sendToPlayer2(String msg) {
            try {
                gameServerConnection2.bWriter.write(msg);
                gameServerConnection2.bWriter.newLine();
                gameServerConnection2.bWriter.flush();
            } catch (IOException e) {
            closeAll(socket, bWriter, bReader);
            }
        }
        @Override
        public String call() {
            if (socket.isConnected()) {
                try {
                    if (Game.isStarted()) {
                        //TODO if this connection.player() is the same as the player in the turn else sendToPlayers wait for your turn
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
        gameServerConnection1.sendToPlayers("Game has started\n");
        int round = 0;

        A : while (round <= Game.MAX_ROUNDS) {
            System.out.println();
            round++;
            if (round <= Game.MAX_ROUNDS) gameServerConnection1.sendToPlayers("this is round " + round + "\n");
            if (round > Game.MAX_ROUNDS) {
                gameServerConnection1.sendToPlayers("exceeded number of rounds");
                gameServerConnection1.sendToPlayers(player1.hand.size() > player2.hand.size() ? player1.name + " won" : player2.name + " won");
                break;
            }

            if(player1.hand.isEmpty()) {
                gameServerConnection1.sendToPlayers(player2.name + " won");
                break A;
            }

            if(player2.hand.isEmpty()) {
                gameServerConnection1.sendToPlayers(player1.name + " won");
                break A;
            }
            Game.start();

            turn = 1;
            Card cardPickedByPlayer1 = player1.putInTable();
            GameServer.getGameServerConnection1().sendToPlayers("card picked " + cardPickedByPlayer1.value);
            gameServerConnection1.sendToPlayers("\n");
            turn = 2;
            Card cardPickedByPlayer2 = player2.putInTable();
            GameServer.getGameServerConnection1().sendToPlayers("card picked " + cardPickedByPlayer2.value);
            gameServerConnection1.sendToPlayers("\n");
            gameServerConnection1.sendWONewLine("current table : { ") ;
            Game.table.stream().map(card -> card.value)
                    .forEach(integer -> GameServer.getGameServerConnection1().sendWONewLine(integer + " "));
            gameServerConnection1.sendToPlayers("}");

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
