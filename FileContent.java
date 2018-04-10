import java.io.Serializable;

/**
 * Created by Darryl Pinto on 3/12/2018.
 *
 * FileContent class represents the file that is transferred in Chord
 */
public class FileContent implements Serializable {

    String name_of_file;
    int target_node;

    /**
     * Constructor for FileContent
     * @param name name of the file
     * @param target_node The id of target node
     */
    FileContent(String name, int target_node) {
        this.name_of_file = name;
        this.target_node = target_node;
    }
}
