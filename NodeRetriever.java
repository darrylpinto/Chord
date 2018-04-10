import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Darryl Pinto on 3/13/2018.
 * <p>
 * Class to handle file retrieval request
 */
public class NodeRetriever implements Runnable {
    int guid;

    /**
     * Constructor of NodeRetriever
     *
     * @param guid node id
     */
    public NodeRetriever(int guid) {
        this.guid = guid;
    }

    /**
     * Run method to run a thread that waits for retrieve request
     */
    @Override
    public void run() {
        retrieve();
    }

    /**
     * Method that waits for retrieve request
     */
    private void retrieve() {

        try {
            ServerSocket serverSocket = new ServerSocket(10000 + guid);
            ObjectOutputStream output = null;
            while (true) {

                Socket socket = serverSocket.accept();
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                String name = input.readUTF();
                boolean target = input.readBoolean();
                output = new ObjectOutputStream(socket.getOutputStream());

                if (target) {
                    File dir = new File("" + guid);

                    if (!dir.exists()) {
                        output.writeBoolean(false);
                        output.flush();

                    } else {

                        File file = new File("" + guid + File.separator + "Content.csv");

                        if (!file.exists()) {
                            output.writeBoolean(false);
                            output.flush();

                        } else {

                            FileReader fr = new FileReader(file);
                            BufferedReader br = new BufferedReader(fr);
                            String line;
                            boolean flag = false;

                            while ((line = br.readLine()) != null) {
                                String[] values = line.trim().split(",");

                                if (values[1].equals(name)) {
                                    output.writeBoolean(true);
                                    output.flush();
                                    flag = true;
                                    break;

                                }

                            }

                            if (flag)
                                continue;

                            output.writeBoolean(false);
                            output.flush();


                        }
                    }
                } else {

                    boolean result = Node.retrieve(name);
                    output.writeBoolean(result);
                    output.flush();

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
