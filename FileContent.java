import java.io.Serializable;

/**
 * Created by Darryl Pinto on 3/12/2018.
 */
public class FileContent implements Serializable {

    String name_of_file;
    int target_node;

    FileContent(String name, int target_node) {
        this.name_of_file = name;
        this.target_node = target_node;
    }
}
