package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

/** Represents the gitlet staging area.
 *
 *  @LiZhu
 */
public class Staging implements Serializable {
    public TreeMap<String, String> stagingAdd; // <filename, UID>
    public TreeMap<String, String> stagingRemove;

    public Staging() {
        this.stagingAdd = new TreeMap<>();
        this.stagingRemove = new TreeMap<>();
    }

    public void clearStage(){
        stagingAdd.clear();
        stagingRemove.clear();
    }
}
