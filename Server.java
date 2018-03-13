import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Darryl Pinto on 3/08/2018.
 */


public class Server implements Runnable {
    private static final int n = 4;
    private static final int N = (int) Math.pow(2, n);

    private static final int serverPort = 6000;

    static HashMap<Integer, Integer> nodeNeighbors = new HashMap<>();
    static ConcurrentHashMap<Integer, Socket> connectionMap = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, FingerTable> tableMap = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, Boolean> onlineNodes = new ConcurrentHashMap<>();

    public static void main(String[] args) throws UnknownHostException {

        deleteFiles();
        System.out.println("Server IP:" + InetAddress.getLocalHost());
        new Thread(new Server()).start();

        for (int i = 0; i < N; i++) {
            nodeNeighbors.put(i, (i + 1) % N);
        }

        ServerSocket serverSock = null;
        try {
            System.out.println("Starting Server");
            serverSock = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.getMessage();
        }

        while (true) {
            Socket soc = null;
            try {
                soc = serverSock.accept();
                new Thread(new ServerRegistration(soc)).start();

            } catch (IOException e) {
                e.getMessage();
            }
        }
    }

    private static void deleteFiles() {

        for (int i = 0; i < N; i++) {
            File file = new File(i + File.separator + "Content.csv");
            if (file.exists())
                file.delete();

        }
    }

    public static void computeTables() {

        for (int i = 0; i < N; i++) {

            if (onlineNodes.containsKey(i)) {
                computeEachTable(i);
            }
        }
    }

    private static void computeEachTable(int k) {

        int[][] table = new int[n][3];
        InetAddress[] ip = new InetAddress[n];
        for (int i = 0; i < n; i++) {

            table[i][0] = i;
            table[i][1] = (k + (int) Math.pow(2, i)) % 16;

            table[i][2] = findSuccessor(table[i][1]);   // successor
            ip[i] = connectionMap.get(table[i][2]).getInetAddress();
        }

        tableMap.put(k, new FingerTable(k, table, ip));

    }

    private static int findSuccessor(int no) {
        int iter = no;

        if (onlineNodes.containsKey(iter)) {
            return iter;
        } else {

            iter = nodeNeighbors.get(iter % N);
            while (true) {
                if (onlineNodes.containsKey(iter)) {
                    return iter;
                }
                iter = nodeNeighbors.get(iter);
            }

        }
    }

    public static void sendTables() {

        for (int i = 0; i < N; i++) {
            if (onlineNodes.containsKey(i) && onlineNodes.get(i)) {
                Socket soc = connectionMap.get(i);
                try {
                    Socket socFinger = new Socket(soc.getInetAddress(), 7000 + i);

                    ObjectOutputStream obj = new ObjectOutputStream(socFinger.getOutputStream());
                    obj.writeObject(tableMap.get(i));
                    obj.flush();

                    socFinger.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }


    }

    public static void exitUser() {

        try {
            ServerSocket serverSoc = new ServerSocket(6001);

            while (true) {

                Socket socket = serverSoc.accept();

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                int userToExit = input.readInt();
                System.out.println("Exit Activated:" + userToExit);

                tableMap.remove(userToExit);
                onlineNodes.remove(userToExit);

                computeTables();
                sendTables();

                ObjectOutputStream exitStatus = new ObjectOutputStream(socket.getOutputStream());
                exitStatus.writeUTF("EXIT");
                exitStatus.flush();
                System.out.println("User left:" + userToExit);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        exitUser();
    }
}
