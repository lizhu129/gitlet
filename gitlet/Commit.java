package gitlet;


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeMap;

import static gitlet.Utils.sha1;


/** Represents a gitlet commit object.
 *
 *  @LiZhu
 */
public class Commit implements Serializable, Dumpable {
    private String UID;
    private String date;
    private String commitMessage;
    private String parentID;
    private TreeMap<String, String> fileMap; // <filename, UID>
    private String secondParent;
    private boolean split = false;
    private HashSet<String> branches;

    public Commit(String commitMessage, String parentID, TreeMap<String, String> fileMap) {
        this.date = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy").format(new Date()) + " -0800";
        this.commitMessage = commitMessage;
        this.parentID = parentID;
        this.fileMap = fileMap;
        this.UID = sha1(this.date + this.commitMessage + this.parentID);
        this.secondParent = null;
        this.branches = new HashSet<>();
    }

    public Commit() {
        this.date = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy").format(new Date(0)) + " -0800";
        this.commitMessage = "initial commit";
        this.parentID = null;
        this.fileMap = new TreeMap<>();
        this.UID = sha1(this.date + this.commitMessage);
    }

    public String getUID() {
        return UID;
    }

    public String getDate() {
        return date;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public String getParentID() {
        return parentID;
    }

    public TreeMap<String, String> getFileMap() {
        return fileMap;
    }

    public String getSecondParent() { return secondParent;}

    public boolean isSplit() { return split; }

    public HashSet<String> getBranches() { return branches; }

    public void setSplit(boolean split) { this.split = split; }

    public void setSecondParent(String secondParent) { this.secondParent = secondParent; }

    public void print() {
        System.out.println("===");
        System.out.println("commit " + this.getUID());
        if (this.secondParent != null) {
            System.out.println("Merge: " + this.parentID.substring(0, 7) + " " + this.secondParent.substring(0, 7));
        }
        System.out.println("Date: " + this.getDate());
        System.out.println(this.getCommitMessage());
        System.out.println();
    }


    // For testing purpose
    @Override
    public void dump() {
        System.out.println("msg: "+ this.commitMessage);
        System.out.println("parent: "+this.parentID);
        System.out.println("2nd parent: "+this.secondParent);
        System.out.println("hash: "+this.UID);
        System.out.println("Map Size: "+this.fileMap.size());
        System.out.println("isSplit: "+ this.isSplit());
    }

}
