package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.join;

/** Repository structure of .gitlet
 *
 *  @LiZhu
 */

public class Repository {

    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Directory to store commits */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File COMMIT_DIR = join(OBJECTS_DIR, "commits");
    /** Directory to store blobs */
    public static final File BLOB_DIR = join(OBJECTS_DIR, "blobs");
    /** Directory to store branch information */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    /** Directory to store remote information */
    public static final File REMOTES_DIR = join(REFS_DIR, "remotes");
    /** File to store current head */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /** File to store staging area */
    public static final File INDEX = join(GITLET_DIR, "index");

    /** Initial setup of .gitlet repository structure */
    static void createRepository() {
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        COMMIT_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        BLOB_DIR.mkdir();
        HEADS_DIR.mkdir();
        REMOTES_DIR.mkdir();
        try {
            INDEX.createNewFile();
            HEAD.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
