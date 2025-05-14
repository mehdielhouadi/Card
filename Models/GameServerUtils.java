package Models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;


public class GameServerUtils {
    public static void sendToPlayers(String msg) {
        for (GameServer.GameServerConnection gsc : GameServer.getGameServerConnections()) {
            try {
                gsc.getbWriter().write(msg);
                gsc.getbWriter().newLine();
                gsc.getbWriter().flush();
            } catch (IOException e) {
                closeAll(gsc.getSocket(), gsc.getbWriter(), gsc.getbReader());
            }
        }
    }
    public static void sendWONewLine(String msg) {
        for (GameServer.GameServerConnection gsc : GameServer.getGameServerConnections()) {
            try {
                gsc.getbWriter().write(msg);
                gsc.getbWriter().flush();
            } catch (IOException e) {
                closeAll(gsc.getSocket(), gsc.getbWriter(), gsc.getbReader());
            }
        }
    }
    public static void sendToPlayer1(String msg) {
        GameServer.GameServerConnection gsc = GameServer.getGameServerConnection1();
        try {
            gsc.getbWriter().write(msg);
            gsc.getbWriter().newLine();
            gsc.getbWriter().flush();
        } catch (IOException e) {
            closeAll(gsc.getSocket(), gsc.getbWriter(), gsc.getbReader());
        }

    }
    public static void sendToPlayer2(String msg) {
        GameServer.GameServerConnection gsc = GameServer.getGameServerConnection2();
        try {
            gsc.getbWriter().write(msg);
            gsc.getbWriter().newLine();
            gsc.getbWriter().flush();
        } catch (IOException e) {
            closeAll(gsc.getSocket(), gsc.getbWriter(), gsc.getbReader());
        }
    }
    public static void closeAll(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
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

    public static void printNormalTable() {
        sendWONewLine("\n");
        sendWONewLine("current table : { ") ;
        Game.table.stream().map(card -> card.value)
                .forEach(integer -> sendWONewLine(integer + " "));
        sendToPlayers("}");
    }
}
