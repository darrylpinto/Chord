import java.io.Serializable;

/**
 * Created by Darryl Pinto on 3/11/2018.
 */
public class FingerTable implements Serializable{

    int[][] table;
    int k;

    public FingerTable(int k, int[][] table) {
        this.k = k;
        this.table = table;
    }

    public FingerTable() {

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
