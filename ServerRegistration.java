import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Darryl Pinto on 3/12/2018.
 * <p>
 * Server Registration class registers new nodes
 * This class is running on other thread waiting for new updates
 */

class ServerRegistration implements Runnable {

    Socket socket;

    ServerRegistration(Socket socket) {
        this.socket = socket;
    }

    /**
     * The run method runs a thread that is waiting for registration
     */
    @Override
    public void run() {
        String _guid = "";
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        try {

            //1
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            _guid = input.readUTF();

            int guid = Integer.parseInt(_guid);

            if (!Server.nodeSuccessor.containsKey(guid)) {

                System.out.println("Invalid GUID:" + guid);
                output.writeUTF("Q"); // Exit Condition
                output.flush();
                socket.close();

            } else {

                System.out.println("Node connected:" + guid);
                output.writeUTF("Works");
                output.flush();
                Server.connectionMap.put(guid, socket);
                Server.onlineNodes.put(guid, true);
                Server.computeTables();
                Server.sendTables();

                int next = Server.nextNode(guid);
                output.writeInt(next);
                output.flush();

                InetAddress nextIP = Server.connectionMap.get(next).getInetAddress();
                output.writeObject(nextIP);
                output.flush();

                this.socket.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {

            System.out.println("Invalid GUID:" + _guid);
            try {
                output.writeUTF("Q");
                output.flush();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }
}