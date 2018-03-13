import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Darryl Pinto on 3/12/2018.
 */
public class NodeListener implements Runnable {

    int guid;

    NodeListener(int guid) {
        this.guid = guid;
    }

    @Override
    public void run() {
        listenForFileTransfer();
    }

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
                    if (dir.mkdir()) {
                        System.out.println("----New directory created:" + dir);
                    }

                    File file = new File("" + guid + "\\Content.csv");

                    if (file.createNewFile()) {

                        FileWriter writer = new FileWriter(file);

                        // csv will contain: target node, name_of_file
                        writer.write(fc.target_node + "," + fc.name_of_file + "\n");
                        System.out.printf("'%d,%s' written to file %s\n",
                                fc.target_node, fc.name_of_file, file.getName());
                        writer.close();
                    } else {

                        FileWriter writer = new FileWriter(file, true);
                        writer.write(fc.target_node + "," + fc.name_of_file + "\n");
                        System.out.printf("'%d,%s' appended to file %s\n",
                                fc.target_node, fc.name_of_file, file.getName());

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
