import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Darryl Pinto on 3/12/2018.
 * <p>
 * Class to handle transfer of files when a
 * file is created and routed to the target node
 */
public class FileTransferHandler implements Runnable {

    int guid;

    /**
     * Constructor of FileTransferHandler
     *
     * @param guid node id
     */
    FileTransferHandler(int guid) {
        this.guid = guid;
    }

    /**
     * Run method to run a thread that waits for create request
     */
    @Override
    public void run() {
        listenForFileTransfer();
    }

    /**
     * method that waits for create request
     */
    private void listenForFileTransfer() {

        try {
            ServerSocket serverSocket = new ServerSocket(8000 + guid);

            while (true) {
                Socket socket = serverSocket.accept();

                //5
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                FileContent fc = (FileContent) input.readObject();
                System.out.println("->FileContent received:" + fc.name_of_file);

                boolean target = input.readBoolean();

                if (target) {
                    File dir = new File("" + guid);
                    dir.mkdir();

                    File file = new File("" + guid + File.separator + "Content.csv");

                    if (file.createNewFile()) {

                        FileWriter writer = new FileWriter(file);

                        // csv will contain: target node, name_of_file
                        writer.write(fc.target_node + "," + fc.name_of_file + "\n");
                        writer.flush();

                        System.out.printf("Record written to file:\n---\n%d,%s\n---\n",
                                fc.target_node, fc.name_of_file);

                        writer.close();
                    } else {

                        FileWriter writer = new FileWriter(file, true);

                        writer.write(fc.target_node + "," + fc.name_of_file + "\n");
                        writer.flush();

                        System.out.printf("Record appended to file:\n---\n%d,%s\n---\n",
                                fc.target_node, fc.name_of_file);

                        writer.close();
                    }

                } else {

                    Node.route(fc.name_of_file);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
