import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Darryl Pinto on 3/11/2018.
 */
public class RegistrationServer {
    static final private int N = 16;
    static final private int serverPort = 6000;

    HashMap<String, Socket> connectionMap = new HashMap<String, Socket>();

    public static void main(String[] args) throws IOException {

        ServerSocket serverSock = new ServerSocket(6000);

        while(true) {
            Socket soc = serverSock.accept();
            ServerOperation sop = new ServerOperation(soc);
            new Thread(sop).start();
            //1

        }
    }

}

class ServerOperation implements Runnable{

    Socket socket;

    ServerOperation(Socket socket){
        this.socket = socket;
    }
    @Override
    public void run() {

        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            String abc = input.readUTF();
            System.out.println(abc);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}