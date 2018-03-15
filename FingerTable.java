import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by Darryl Pinto on 3/11/2018.
 */
public class FingerTable implements Serializable {

    int[][] table;
    InetAddress[] ip;
    int k;

    FingerTable(int k, int[][] table, InetAddress[] ip) {
        this.k = k;
        this.table = table;
        this.ip = ip;
    }

    FingerTable() {
        this.table = null;
        this.k = -1;
        this.ip = null;
    }

    public String toString() {

        StringBuilder str = new StringBuilder("k = " + this.k);

        str.append("\ni\t\tk+2^i\t\tsuccessor\n");


        for (int[] row : table) {
            for (int a : row) {
                str.append(a).append("\t\t\t");

            }
            str.append("\n");
        }

        return str.toString();
    }

}
