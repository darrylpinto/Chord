import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by Darryl Pinto on 3/11/2018.
 */
public class FingerTable implements Serializable {

    int[][] table;
    InetAddress[] ip;
    int k;

    public FingerTable(int k, int[][] table, InetAddress[] ip) {
        this.k = k;
        this.table = table;
        this.ip = ip;
    }

    public FingerTable() {
        this.table = null;
        this.k = -1;
        this.ip = null;
    }

    public String toString() {

        String str = "k = " + this.k;

        str += "\ni\t\tk+2^i\t\tsuccessor\n";


        for (int[] row : table) {
            for (int a : row) {
                str += a + "\t\t\t";

            }
            str += "\n";
        }

        return str;
    }

}
