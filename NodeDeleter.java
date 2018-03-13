import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Darryl Pinto on 3/13/2018.
 */

public class NodeDeleter implements Runnable {
    int guid;

    public NodeDeleter(int id) {

        this.guid = id;
    }

    @Override
    public void run() {
        listenForDeleteTransfer();
    }

    private void listenForDeleteTransfer() {

        try {
            ServerSocket serverSocket = new ServerSocket(9000 + guid);
            while (true) {

                Socket socket = serverSocket.accept();

                ObjectInputStream data_input = new ObjectInputStream(socket.getInputStream());

                String data = data_input.readUTF();

                File dir = new File("" + guid);
                if (dir.mkdir()) {
                    System.out.println("----New directory created:" + dir);
                }
                File file = new File("" + guid + "\\Content.csv");

                if (file.createNewFile()) {

                    FileWriter writer = new FileWriter(file);

                    writer.write(data);
                    System.out.println("Data transfer because of node " +
                            "quitting the network");
                    System.out.println(data + "written to file");
                    writer.close();
                } else {

                    FileWriter writer = new FileWriter(file, true);
                    writer.write(data);
                    System.out.println("Data transfer because of node" +
                            " quitting the network");
                    System.out.println(data + "appended to file");
                    writer.close();
                }


            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
