import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Darryl Pinto on 3/12/2018.
 */

class ServerRegistration implements Runnable {

    Socket socket;

    ServerRegistration(Socket socket) {
        this.socket = socket;
    }

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

            if (!Server.nodeNeighbors.containsKey(guid)) {
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