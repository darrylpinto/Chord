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

    //static final int fileTransferPort = 8000;
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
            while(true){
//                System.out.println("NodeListener listening at port:"+(8000 + guid));
                Socket socket = serverSocket.accept();

                //5
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                FileContent fc = (FileContent) input.readObject();
                System.out.println("->FileContent received:"+ fc.name_of_file);
                File dir = new File(""+guid);
                if(dir.mkdir()){
                    System.out.println("----New directory created:"+dir);
                }
                File file = new File(""+guid + "//Content.csv");

                if (file.createNewFile()) {
                    System.out.println("-->File is created:" + file);
                    FileWriter writer = new FileWriter(file);

                    // csv will contain: target node, name_of_file
                    writer.write(fc.target_node + "," + fc.name_of_file + "\n");
                    writer.close();
                } else {
                    System.out.println("-->File already exists.");
                    FileWriter writer = new FileWriter(file, true);
                    writer.write(fc.target_node + "," + fc.name_of_file + "\n");
                    writer.close();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
