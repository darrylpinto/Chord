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

    NewNodeTransferHandler(int guid) {
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
                Thread.sleep(1000);

                if (!dir.exists()) {

                    output.writeBoolean(false);
                    output.flush();
                } else if (!file.exists()) {

                    output.writeBoolean(false);
                    output.flush();
                } else {

                    output.writeBoolean(true);
                    output.flush();

                    HashSet<Integer> remoteHS = new HashSet<>();

                    for (int i = remoteRange.get(0); i <= remoteRange.get(1); i++) {
                        remoteHS.add(i % Node.N);
                    }


                    FileReader fr = new FileReader(file);
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    StringBuilder dataToSend = new StringBuilder();
                    StringBuilder dataToKeep = new StringBuilder();

                    while ((line = br.readLine()) != null) {

                        if (!line.equals("\n")) {
                            String[] values = line.trim().split(",");
                            if (remoteHS.contains(Integer.parseInt(values[0]))) {
                                dataToSend.append(line).append("\n");
                            } else {
                                dataToKeep.append(line).append("\n");

                            }

                        }

                    }
                    br.close();
                    fr.close();

                    FileWriter writer = new FileWriter(file);
                    writer.write(dataToKeep.toString());
                    writer.flush();

                    System.out.println("Data transfer because of node added to the network");
                    System.out.println("Data written to local file:\n----\n" + dataToKeep + "----");
                    writer.close();
                    System.out.println("Data sent to newly added node:\n----\n" + dataToSend + "----");

                    output.writeUTF(dataToSend.toString());
                    output.flush();

                    socket.close();
                }


            }


        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
