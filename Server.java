import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Darryl Pinto on 3/11/2018.
 */


class ServerOperation implements Runnable {

    Socket socket;

    ServerOperation(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String _guid ="";
        try {

            //1
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());


            _guid = input.readUTF();


            int guid = Integer.parseInt(_guid);

            if (!Server.nodeNeighbors.containsKey(guid)) {
                System.out.println("Invalid GUID:" + guid);
                socket.close();
            } else {
                System.out.println("Node connected:" + guid);
                Server.connectionMap.put(guid, socket);
                Server.onlineNodes.put(guid, true);
                Server.computeTables();

                System.out.println(Server.tableMap);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch(NumberFormatException e){
            System.out.println("Invalid GUID:" + _guid);

        }
    }
}

public class Server {
    private static final int n = 4;
    private static final int N = (int) Math.pow(2,n);

    private static final int serverPort = 6000;

    static HashMap<Integer, Integer> nodeNeighbors = new HashMap<>();
    static ConcurrentHashMap<Integer, Socket> connectionMap = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, FingerTable> tableMap = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, Boolean> onlineNodes = new ConcurrentHashMap<>();


    public static void main(String[] args) {


        for (int i = 0; i <N; i++) {

            nodeNeighbors.put(i, (i + 1) % N);
        }

        ServerSocket serverSock = null;
        try {

            serverSock = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.getMessage();
        }

        while (true) {
            Socket soc = null;
            try {
                soc = serverSock.accept();

                new Thread(new ServerOperation(soc)).start();
            } catch (IOException e) {
                e.getMessage();
            }
        }
    }


    public static void computeTables() {

        for (int i = 0; i < N; i++) {

            if (onlineNodes.containsKey(i)) {
                computeEachTable(i);

            }
        }

    }

    public static void computeEachTable(int k) {


        int[][] table = new int[n][3];
        for (int i = 0; i < n; i++) {

            table[i][0] = i;
            table[i][1] = (k + (int) Math.pow(2, i)) % 16;

            table[i][2] = findSuccessor(table[i][1]);   // successor

        }

        tableMap.put(k, new FingerTable(k,table));

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
}
