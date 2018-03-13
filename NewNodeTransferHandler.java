import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Darryl Pinto on 3/13/2018.
 */
public class NewNodeTransferHandler implements Runnable {

    int guid;

    public NewNodeTransferHandler(int guid) {
        this.guid = guid;
    }

    @Override
    public void run() {
        newNodeDataRequest();
    }

    private void newNodeDataRequest() {

        try {
            ServerSocket serverSocket = new ServerSocket(6500 + guid);

            while (true) {

                Socket socket = serverSocket.accept();
                File dir = new File("" + guid);
                File file = new File("" + guid + File.separator + "Content.csv");

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                ArrayList<Integer> remoteRange = (ArrayList<Integer>) input.readObject();
                System.out.println("Received REMOTE RANGE:"+remoteRange);
                ArrayList<Integer> localRange = Node.range;
                System.out.println("LOCAL RANGE:"+localRange);

                if (!dir.exists()) {
                    System.out.println("No data present at node " + guid);
                    output.writeBoolean(false);
                    output.flush();
                } else if (!file.exists()) {
                    System.out.println("No data present at node " + guid);
                    output.writeBoolean(false);
                    output.flush();

                } else {
                    output.writeBoolean(true);
                    output.flush();

                    HashSet<Integer> remoteHS = new HashSet<>();
                    HashSet<Integer> localHS = new HashSet<>();

                    for (int i = remoteRange.get(0); i <= remoteRange.get(1); i++) {
                        remoteHS.add(i % Node.N);
                    }

                    for (int i = localRange.get(0); i <= localRange.get(1); i++) {
                        localHS.add(i % Node.N);
                    }

                    FileReader fr = new FileReader(file);
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    String dataToSend = "";
                    String dataToKeep = "";

                    while ((line = br.readLine()) != null) {

                        if (line.equals("\n")) {

                        }
                        else {
                            String[] values = line.trim().split(",");
                            if (localHS.contains(Integer.parseInt(values[0]))) {
                                dataToKeep += line + "\n";

                            } else if (remoteHS.contains(Integer.parseInt(values[0]))) {
                                dataToSend += line + "\n";
                            } else {
                                System.out.println("Ideally SHOULD NOT COME HERE");
                            }

                        }

                    }
                    br.close();
                    fr.close();

                    FileWriter writer = new FileWriter(file);
                    writer.write(dataToKeep);
                    writer.flush();

                    System.out.println(dataToKeep + "written to file");
                    writer.close();

                    System.out.println("Data transfer because of node " +
                            "added to the network");
                    System.out.println(dataToSend + " sent to newly added node");
                    output.writeUTF(dataToSend);
                    output.flush();

                    socket.close();
                }


            }


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
