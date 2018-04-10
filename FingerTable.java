import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by Darryl Pinto on 3/11/2018.
 * <p>
 * The FingerTable class represents the finger table at each node
 */
public class FingerTable implements Serializable {

    int[][] table;
    InetAddress[] ip;
    int k;

    /**
     * Constructor of FingerTable
     *
     * @param k     number of rows
     * @param table finger table in the form of matrix
     * @param ip    Ip of the node having the finger table
     */
    FingerTable(int k, int[][] table, InetAddress[] ip) {
        this.k = k;
        this.table = table;
        this.ip = ip;
    }

    /**
     * Another Constructor of FingerTable
     */
    FingerTable() {
        this.table = null;
        this.k = -1;
        this.ip = null;
    }

    /**
     * toString method of FingerTable
     *
     * @return string representation of FingerTable
     */
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
