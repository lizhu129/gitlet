package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

/** Represents the gitlet staging area.
 *
 *  @LiZhu
 */
public class Staging implements Serializable {
    private TreeMap<String, String> stagingAdd; // <filename, UID>
    private TreeMap<String, String> stagingRemove;

    public Staging() {
        this.stagingAdd = new TreeMap<>();
        this.stagingRemove = new TreeMap<>();
    }

    public void clearStage(){
        stagingAdd.clear();
        stagingRemove.clear();
    }

    public TreeMap<String, String> getStagingAdd() {
        return stagingAdd;
    }

    public TreeMap<String, String> getStagingRemove() {
        return stagingRemove;
    }
}
