import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


/**
 * Created by Darryl Pinto on 3/08/2018.
 */
public class Node implements Runnable {
    private static final int n = 4;
    private static final int N = (int) Math.pow(2, n);

    static FingerTable fingerTable = new FingerTable();
    static int ID;

    public static void main(String[] args) throws IOException {

        //Server IP
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

        // Node thread to listen for FingerTable Updates
        new Thread(new Node()).start();
        // NodeListener Thread
        new Thread(new NodeListener(ID)).start();


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
                    Socket socExit = new Socket(host, 6001);
                    ObjectOutputStream exitOutput = new ObjectOutputStream(socExit.getOutputStream());
                    exitOutput.writeInt(ID);
                    exitOutput.flush();
                    System.out.println("Transferring data");

                    ObjectInputStream exitStatus = new ObjectInputStream(socExit.getInputStream());
                    exitStatus.readUTF();
                    System.out.println("User " + ID + " has stopped");
                    socExit.close();
                    System.exit(0);

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

        int target = Math.abs(name.hashCode() % N);
        System.out.println("Hash("+name+")%"+N+"="+target);
        InetAddress target_ip = null;
        try {
            InetAddress potential_target = InetAddress.getLocalHost();
            boolean target_present = false;

            for (int i = 0; i < fingerTable.table.length; i++) {

                synchronized (fingerTable) {
                    if (fingerTable.table[i][1] == target) {
                        target_ip = fingerTable.ip[i];
                        target_present = true;
                    }
                }

                if (target_present) {
                    System.out.println("Target present at "+target);

                    sendFileToTarget(target_ip, name, target);
                    return;
                }

//                synchronized (fingerTable) {
//
//                    if (fingerTable.table[i][1] < target) {
//                        potential_target = fingerTable.ip[i];
//
//                    }
//                }


            }
            if (!target_present) {
                target_ip = potential_target;
            }

            // JUMP

        } catch (IOException e) {
            System.out.println("Message: " + e.getMessage());

        }

    }

    private static void sendFileToTarget(InetAddress target_ip, String name, int target) throws IOException {
        // open socket, send data and return

        // FileContent transfer socket
        Socket socket = new Socket(target_ip, 8000+ ID);
        System.out.println("Connection request sent to "+ target_ip);
        // 5
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        FileContent fc = new FileContent(name, target);
        output.writeObject(fc);
        output.flush();

        System.out.println("FileContent sent to "+target_ip+":"+ fc.name_of_file);
        socket.close();

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
        fingerTableUpdate();
    }
}
