import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


/**
 * Created by Darryl Pinto on 3/08/2018.
 */
public class Node implements Runnable {
    public final static Object object = new Object();
    static final int n = 4;
    static final int N = (int) Math.pow(2, n);
    static FingerTable fingerTable = new FingerTable();
    static int guid;
    static ArrayList<Integer> range = new ArrayList<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        String host;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Server IP");
        host = sc.next();
        ArrayList temp = Node.range;
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
        Node.guid = Integer.parseInt(guid);

        // Node thread to listen for FingerTable Updates
        new Thread(new Node()).start();
        new Thread(new FileTransferHandler(Node.guid)).start();
        new Thread(new QuitHandler(Node.guid)).start();
        new Thread(new NodeRetriever(Node.guid)).start();
        new Thread(new NewNodeTransferHandler(Node.guid)).start();


        int successor = input.readInt();
        InetAddress successorIP = (InetAddress) input.readObject();

        Socket socNext = new Socket(successorIP, 6500 + successor);
        ObjectOutputStream outputNext = new ObjectOutputStream(socNext.getOutputStream());
        ObjectInputStream inputNext = new ObjectInputStream(socNext.getInputStream());


        synchronized (object) {
            object.wait();
            outputNext.writeObject(Node.range);
            outputNext.flush();

        }

        boolean dataTransferStatus = inputNext.readBoolean();

        if (dataTransferStatus) {
            String dataReceived = inputNext.readUTF();

            File dir = new File("" + guid);
            if (dir.mkdir()) {
                System.out.println("----New directory created:" + dir);
            }

            File file = new File("" + guid + File.separator + "Content.csv");

            if (file.createNewFile()) {

                FileWriter writer = new FileWriter(file);

                // csv will contain: target node, name_of_file
                writer.write(dataReceived);
                writer.flush();

                System.out.println(dataReceived + "written to the file");
                writer.close();
            } else {

                FileWriter writer = new FileWriter(file, true);
                writer.write(dataReceived);
                writer.flush();

                System.out.println(dataReceived + "appended to the file");
                writer.close();
            }


        } else {
            System.out.println("No data found at " + successor);
            System.out.println("Transfer completed");
        }


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
                        System.out.println(fingerTable);
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
                    if (retrieve(retrieve_name)) {
                        System.out.println(retrieve_name + " present in DHT Chord");
                    } else
                        System.out.println(retrieve_name + " NOT present in DHT Chord");


                    break;

            }
        }
    }

    static boolean retrieve(String name) throws IOException {
        int target = Math.abs(name.hashCode() % N);

        if (target == guid) {
            File dir = new File("" + guid);

            if (!dir.exists()) {
                return false;
            } else {
                File file = new File("" + guid + File.separator + "Content.csv");
                if (!file.exists()) {
                    return false;
                } else {
                    FileReader fr = new FileReader(file);
                    BufferedReader br = new BufferedReader(fr);
                    String line;

                    while ((line = br.readLine()) != null) {
                        String[] values = line.trim().split(",");
                        if (values[1].equals(name)) {
                            return true;
                        }
                    }
                    return false;

                }
            }

        }
        int successor = -1;
        System.out.println("Hash(" + name + ")%" + N + "=" + target);
        InetAddress target_ip = null;

        int minimum = Integer.MAX_VALUE;
        boolean target_present = false;
        int kplus2i = -1;

        try {
            InetAddress potential_target_ip = InetAddress.getLocalHost();
            for (int i = 0; i < fingerTable.table.length; i++) {

                // base Case
                synchronized (fingerTable) {
                    if (target - fingerTable.table[i][1] == 0) {
                        target_ip = fingerTable.ip[i];
                        target_present = true;
                        successor = fingerTable.table[i][2];
                    }
                }

                if (target_present) {

                    return toRetrieve(target_ip, name, successor, true);
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
                        kplus2i = fingerTable.table[i][1];

                    }
                }

            }

            target_ip = potential_target_ip;

            System.out.printf("Rerouting Search for '%s' to node %d at %s\n",
                    name, successor, target_ip);

            if (target > kplus2i && target < successor) {
                return toRetrieve(target_ip, name, successor, true);
            } /*else if (target > kplus2i && target > successor) {
                return toRetrieve(target_ip, name, successor, true);
            } else if (target < kplus2i && target < successor) {
                return toRetrieve(target_ip, name, successor, true);
            }*/ else {
                return toRetrieve(target_ip, name, successor, false);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean toRetrieve(InetAddress target_ip, String name,
                                      int successor, boolean b) throws IOException {

        Socket socket = new Socket(target_ip, 10000 + successor);
        System.out.println("Connection request sent to port " + (10000 + successor));

        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeUTF(name);
        output.flush();

        output.writeBoolean(b);
        output.flush();


        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
        boolean result = input.readBoolean();

        socket.close();

        return result;

    }


    static void route(String name) throws IOException {

        int target = Math.abs(name.hashCode() % N);
        if (target == guid) {
            File dir = new File("" + guid);
            if (dir.mkdir()) {
                System.out.println("----New directory created:" + dir);
            }

            File file = new File("" + guid + File.separator + "Content.csv");

            if (file.createNewFile()) {

                FileWriter writer = new FileWriter(file);

                // csv will contain: target node, name_of_file
                writer.write(target + "," + name + "\n");
                writer.flush();
                System.out.printf("'%d,%s' written to file %s\n",
                        target, name, file.getName());
                writer.close();
            } else {

                FileWriter writer = new FileWriter(file, true);
                writer.write(target + "," + name + "\n");
                writer.flush();

                System.out.printf("'%d,%s' appended to file %s\n",
                        target, name, file.getName());
                writer.close();
            }
            return;

        }

        int successor = -1;
        System.out.println("Hash(" + name + ")%" + N + "=" + target);
        InetAddress target_ip = null;

        int minimum = Integer.MAX_VALUE;
        boolean target_present = false;
        int kplus2i = -1;
        try {
            InetAddress potential_target_ip = InetAddress.getLocalHost();


            for (int i = 0; i < fingerTable.table.length; i++) {

                // base Case
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
                        kplus2i = fingerTable.table[i][1];

                    }
                }

            }

            target_ip = potential_target_ip;

            System.out.printf("Rerouting filename %s to node %d at %s\n",
                    name, successor, target_ip);

            if (target > kplus2i && target < successor) {
                sendFileToTarget(target_ip, name, target, successor, true);
            } /*else if (target > kplus2i && target > successor) {
                sendFileToTarget(target_ip, name, target, successor, true);
            } else if (target < kplus2i && target < successor) {
                sendFileToTarget(target_ip, name, target, successor, true);
            }*/ else {
                sendFileToTarget(target_ip, name, target, successor, false);
            }


        } catch (IOException e) {
            System.out.println("Message: " + e.getMessage());

        }

    }

    private static void sendFileToTarget(InetAddress target_ip, String name, int target,
                                         int successor, boolean b) throws IOException {
        // open socket, send data and return

        // FileContent transfer socket
        Socket socket = new Socket(target_ip, 8000 + successor);
        System.out.println("Connection request sent to port " + (8000 + successor));

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

    static void quit(String host) throws IOException {
        System.out.println("Preparing to Stop");

        File dir = new File("" + guid);
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
            fr.close();

            if (Files.deleteIfExists(files.get(0).toPath())) {
                System.out.println("Data in " + files.get(0) + " sent to " + nextSuccessor + " and deleted locally");
            } else {
                System.out.println("Error in DELETE");

            }
            ObjectOutputStream data_output = new ObjectOutputStream(socNext.getOutputStream());
            data_output.writeUTF(data);
            data_output.flush();

            socNext.close();

        }
        Socket socExit = new Socket(host, 6001);
        ObjectOutputStream exitOutput = new ObjectOutputStream(socExit.getOutputStream());
        exitOutput.writeInt(guid);
        exitOutput.flush();

        ObjectInputStream exitStatus = new ObjectInputStream(socExit.getInputStream());
        exitStatus.readUTF();
        System.out.println("User " + guid + " has stopped");
        socExit.close();
        System.exit(0);
    }


    public static void fingerTableUpdate() {

        try {
            ServerSocket serverSoc = new ServerSocket(7000 + guid);

            while (true) {
                ArrayList temp = Node.range;

                Socket socket = serverSoc.accept();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                Object obj = input.readObject();

                synchronized (fingerTable) {
                    fingerTable = (FingerTable) obj;
                    System.out.println(">>>Updated FingerTable");
                }

                synchronized (object) {
                    Node.range = (ArrayList<Integer>) input.readObject();
                    object.notify();
                }

                socket.close();
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
