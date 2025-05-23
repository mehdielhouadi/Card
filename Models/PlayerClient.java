package Models;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class PlayerClient {

    private final Socket socket;
    private Player player;
    private final BufferedWriter bufferedWriter;
    private final BufferedReader bufferedReader;
    private int n = 0;
    private volatile boolean myTurn = false;


    public PlayerClient(Socket socket) throws IOException {
        n++;
        this.socket = socket;
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your username for the game :");
        String name = sc.nextLine();
        bufferedWriter.write(name);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        this.player = new Player(name);
        if(n == 1) Game.player1 = this.player;
        if (n == 2) Game.player2 = this.player;
    }

    public PlayerClient(Player player, Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        this.player = player;
        this.socket = socket;
        this.bufferedWriter = bufferedWriter;
        this.bufferedReader = bufferedReader;
    }

    public PlayerClient(Socket socket, Player player) throws IOException {
        this.player = player;
        this.socket = socket;
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public Player getPlayer() {
        return player;
    }

    public void sendChoice() throws IOException {
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (socket.isConnected()) {
                try {
                    // could also use synchronization with a lock.wait to release lock and block
                    // and a function tu turn the boolean to true and notify lock then put it to false again
                    while (!myTurn) {
                        Thread.onSpinWait();
                    }
                    // discard all choices typed before the turn

                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    while (in.ready()) {
                        in.readLine();
                    }
                    String choice = sc.nextLine();
                    bufferedWriter.write(choice);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    myTurn = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void listenToServer() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String msgFromServer = bufferedReader.readLine();
                    if (msgFromServer.equals("YOUR_TURN")) {
                        myTurn = true;
                    }
                    if (!msgFromServer.equals("YOUR_TURN")) {
                        System.out.println(msgFromServer);;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost",1234);
        PlayerClient playerClient = new PlayerClient(socket);
        playerClient.listenToServer();
        playerClient.sendChoice();
    }

}
