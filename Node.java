import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
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

        String host;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Server IP");
        host = sc.next();

        Socket soc = new Socket(host, 6000);

        // 1
        ObjectOutputStream output = new ObjectOutputStream(soc.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(soc.getInputStream());

        System.out.println("Enter GUID:");
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
        new Thread(new NodeListener(ID)).start();
        new Thread(new NodeDeleter(ID)).start();


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
                    quit(host);
                    break;

                case "c":
                case "C":
                    System.out.println("Enter fileName:");
                    String name = sc.next();
                    route(name);
                    break;

                case "r":
                case "R":
                    System.out.println("Enter fileName to retrieve:");
                    String retrieve_name = sc.next();


                    break;

            }
        }
    }


    static void quit(String host) throws IOException {
        System.out.println("Preparing to Stop");

        File dir = new File("" + ID);
        if (dir.exists()) {
            InetAddress nextTarget;
            int nextSuccessor;
            synchronized (fingerTable) {
                nextSuccessor = fingerTable.table[0][2];
                nextTarget = fingerTable.ip[0];

            }
            Socket socNext = new Socket(nextTarget, 9000 + nextSuccessor);
            ArrayList<File> files = new ArrayList<>(Arrays.asList(dir.listFiles()));

            FileReader fr = new FileReader(files.get(0));
            BufferedReader br = new BufferedReader(fr);

            String line;
            String data = "";
            while ((line = br.readLine()) != null) {
                data += line;
                data += "\n";
            }

            br.close();

            if (files.get(0).delete()) {
                System.out.println("File sent to " + nextSuccessor + " and deleted locally");
            }
            ObjectOutputStream data_output = new ObjectOutputStream(socNext.getOutputStream());
            data_output.writeUTF(data);
            data_output.flush();

            socNext.close();

        }
        Socket socExit = new Socket(host, 6001);
        ObjectOutputStream exitOutput = new ObjectOutputStream(socExit.getOutputStream());
        exitOutput.writeInt(ID);
        exitOutput.flush();

        ObjectInputStream exitStatus = new ObjectInputStream(socExit.getInputStream());
        exitStatus.readUTF();
        System.out.println("User " + ID + " has stopped");
        socExit.close();
        System.exit(0);
    }

    static void route(String name) {

        int target = Math.abs(name.hashCode() % N);
        int successor = -1;
        System.out.println("Hash(" + name + ")%" + N + "=" + target);
        InetAddress target_ip = null;

        int minimum = Integer.MAX_VALUE;
        boolean target_present = false;
        try {
            InetAddress potential_target_ip = InetAddress.getLocalHost();

            for (int i = 0; i < fingerTable.table.length; i++) {

                synchronized (fingerTable) {
                    if (target - fingerTable.table[i][1] == 0) {
                        target_ip = fingerTable.ip[i];
                        target_present = true;
                        successor = fingerTable.table[i][2];
                    }
                }

                if (target_present) {
                    System.out.println("Target present at " + successor);
                    System.out.printf("Preparing to send filename %s to %d \n",
                            name, successor);
                    sendFileToTarget(target_ip, name, target, successor, true);
                    return;
                }

                synchronized (fingerTable) {

                    int difference = target - fingerTable.table[i][1];

                    if (difference < 0) {
                        difference += 16;
                    }

                    if (difference < minimum) {
                        minimum = difference;
                        potential_target_ip = fingerTable.ip[i];
                        successor = fingerTable.table[i][2];

                    }
                }

            }

            target_ip = potential_target_ip;

            System.out.printf("Rerouting filename %s to node %d at %s\n",
                    name, successor, target_ip);

            sendFileToTarget(target_ip, name, target, successor, false);


        } catch (IOException e) {
            System.out.println("Message: " + e.getMessage());

        }

    }

    private static void sendFileToTarget(InetAddress target_ip, String name, int target,
                                         int successor, boolean b) throws IOException {
        // open socket, send data and return

        // FileContent transfer socket
        Socket socket = new Socket(target_ip, 8000 + successor);
        System.out.println("Connection request sent to port" + (8000 + successor));

        // 5
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        FileContent fc = new FileContent(name, target);
        output.writeObject(fc);
        output.flush();

        output.writeBoolean(b);
        output.flush();

        System.out.println("FileContent sent to node " + successor
                + " filename: " + fc.name_of_file);
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
