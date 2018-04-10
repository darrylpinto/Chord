import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Darryl Pinto on 3/13/2018.from the system
 *
 * This class is running on other thread waiting for a node to quit
 * The data is transferred from the quitting node to this node
 */

public class QuitHandler implements Runnable {
    int guid;

    /**
     * Constructor for QuitHandler
     * @param id Node id
     */
    public QuitHandler(int id) {

        this.guid = id;
    }

    @Override
    public void run() {
        listenForDeleteTransfer();
    }

    /**
     * Method that writes data received from the quitting node
     */
    private void listenForDeleteTransfer() {

        try {
            ServerSocket serverSocket = new ServerSocket(9000 + guid);
            while (true) {

                Socket socket = serverSocket.accept();
                ObjectInputStream data_input = new ObjectInputStream(socket.getInputStream());

                String data = data_input.readUTF();

                File dir = new File("" + guid);
                dir.mkdir();
                File file = new File("" + guid + File.separator + "Content.csv");

                if (file.createNewFile()) {

                    FileWriter writer = new FileWriter(file);

                    writer.write(data);
                    writer.flush();

                    System.out.println("Data transfer because of node " +
                            "quitting the network");

                    System.out.println("Data written to file:\n---\n" + data + "---");
                    writer.close();

                } else {

                    FileWriter writer = new FileWriter(file, true);
                    writer.write(data);
                    writer.flush();

                    System.out.println("Data transfer because of node" +
                            " quitting the network");

                    System.out.println("Data appended to file:\n---\n" + data + "---");

                    writer.close();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
