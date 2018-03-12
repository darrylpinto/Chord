import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


/**
 * Created by Darryl Pinto on 3/11/2018.
 */
public class Node implements Runnable {
    private static final int n = 4;
    private static final int N = (int) Math.pow(2, n);

    static FingerTable fingerTable = new FingerTable();
    static int ID;

    public static void main(String[] args) throws IOException {


        String host = "LocalHost";
        Scanner sc = new Scanner(System.in);
        Socket soc = new Socket(host, 6000);

        // 1
        ObjectOutputStream output = new ObjectOutputStream(soc.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(soc.getInputStream());

        System.out.println("Enter GUID: ");
        String guid = sc.next();
        output.writeUTF(guid);
        output.flush();

        String status = input.readUTF();
        if (status.equalsIgnoreCase("Q")) {
            System.out.println("Exiting..");
            System.exit(10);
        }
        ID = Integer.parseInt(guid);

        new Thread(new Node()).start();

        while (true) {
            String str = "p - print FingerTable\n";
            str += "q - quit\n";
            str += "c - create File\n";
            str += "r - Retrieve File";

            System.out.println(str);
            String choice = sc.next();

            switch (choice) {
                case "p":
                case "P":
                    synchronized (fingerTable) {
                        System.out.println(fingerTable); // Get latest fingerTable
                    }
                    break;
                case "q":
                case "Q":
                    System.out.println("Preparing to Stop");
                    // Transfer data
                    break;
                case "c":
                case "C":
                    System.out.println("Enter fileName:");
                    String name = sc.next();
                    reRoute(name);
                    break;
                case "r":
                case "R":
                    break;

            }
        }
    }

    private static void reRoute(String name) {
        int hash = name.hashCode() % N;
    }

    public static void fingerTableUpdate() {

        try {
            ServerSocket serverSoc = new ServerSocket(7000 + ID);

            while (true) {

                Socket socket = serverSoc.accept();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                Object obj = input.readObject();

                synchronized (fingerTable) {
                    fingerTable = (FingerTable) obj;
                    System.out.println(">>>Updated FingerTable");
                }

            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        System.out.println("In run method");
        fingerTableUpdate();
    }
}
