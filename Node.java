import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;


/**
 * Created by Darryl Pinto on 3/11/2018.
 */
public class Node {

    public static void main(String[] args) throws IOException {

        String host = "LocalHost";
        Scanner sc = new Scanner(System.in);
        Socket soc = new Socket(host, 6000);

        // 1
        ObjectOutputStream output = new ObjectOutputStream(soc.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(soc.getInputStream());

        System.out.println("Enter GUID: ");
        String username = sc.next();
        output.writeUTF(username);
        output.flush();



    }
}
