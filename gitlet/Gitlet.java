package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Contains all the .gitlet command methods.
 *  @LiZhu
 */

public class Gitlet implements Serializable {

    public void init() {
        if (GITLET_DIR.exists()) {
            exitWithError("A Gitlet version-control system already exists in the current directory.");
        }
        createRepository();
        Commit initCommit = new Commit();
        setHead("master");
        saveCommit(initCommit, "master");
        Staging stagingArea = new Staging();
        writeObject(INDEX, stagingArea);
    }

    public void add(String filename) {
        File file = join(CWD, filename);
        if (!file.exists()) {
            exitWithError("File does not exist.");
        }
        Blob blob = new Blob(filename, file);
        Staging stagingArea = readObject(INDEX, Staging.class);
        /** Get the current commit */
        Commit currCommit = getCurrentCommit();
        if (currCommit.getFileMap().containsKey(filename)) {
            if (!currCommit.getFileMap().get(filename).equals(blob.getUID())) {
                stagingArea.stagingAdd.put(filename, blob.getUID());
                blob.storeBlob();
            }
            if (stagingArea.stagingRemove.containsKey(filename)) {
                stagingArea.stagingRemove.remove(filename);
            }
        } else {
            stagingArea.stagingAdd.put(filename, blob.getUID());
            blob.storeBlob();
        }
        writeObject(INDEX, stagingArea);
    }

    public void rm(String filename) {
        File file = join(CWD, filename);
        /** Get staging area */
        Staging stagingArea = readObject(INDEX, Staging.class);
        /** Get current commit */
        Commit currCommit = getCurrentCommit();
        Blob blob = new Blob(filename, file);
        if (stagingArea.stagingAdd.containsKey(filename)) {
            stagingArea.stagingAdd.remove(filename);
        } else if (currCommit.getFileMap().containsKey(filename)) {
            stagingArea.stagingRemove.put(filename, blob.getUID());
            restrictedDelete(filename);
        } else if (!currCommit.getFileMap().containsKey(filename)) {
            stagingArea.stagingRemove.put(filename, blob.getUID());
        } else {
            exitWithError("No reason to remove the file.");
        }
        writeObject(INDEX, stagingArea);
    }


    public void commit(String message) {
        Staging stagingArea = readObject(INDEX, Staging.class);
        if (stagingArea.stagingAdd.isEmpty() && stagingArea.stagingRemove.isEmpty()) {
            exitWithError("No changes added to the commit.");
        }
        if (message.isEmpty()) {
            exitWithError("Please enter a commit message.");
        }
        Commit currCommit = getCurrentCommit();
        Commit newCommit = new Commit(message, currCommit.getUID(), currCommit.getFileMap());
        stageCommit(newCommit);
        saveCommit(newCommit, currBranch());
        clearStaging(stagingArea);
    }

    public void log() {
        Commit c = getCurrentCommit();
        while (c.getParentID() != null) {
            c.print();
            c = readObject(join(COMMIT_DIR, c.getParentID()), Commit.class);
        }
        c.print();
    }

    public void globalLog() {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        for (String s : commits) {
            Commit c = readObject(join(COMMIT_DIR, s), Commit.class);
            c.print();
        }
    }

    public void find(String message) {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        int count = 0;
        for (String s : commits) {
            Commit c = readObject(join(COMMIT_DIR, s), Commit.class);
            if (c.getCommitMessage().equals(message)) {
                System.out.println(c.getUID());
                count++;
            }
        }
        if (count == 0) {
            error("Found no commit with that message.");
        }
    }

    public void status() {
        /** Branches */
        System.out.println("=== Branches ===");
        System.out.println("*" + currBranch());
        List<String> heads = plainFilenamesIn(HEADS_DIR);
        for (String s : heads) {
            if (!s.equals(currBranch())) {
                System.out.println(s);
            }
        }
        System.out.println();

        /** Staged Files */
        Staging stagingArea = readObject(INDEX, Staging.class);
        System.out.println("=== Staged Files ===");
        for (String s : stagingArea.stagingAdd.keySet()) {
            System.out.println(s);
        }
        System.out.println();

        /** Removed Files */
        System.out.println("=== Removed Files ===");
        for (String s : stagingArea.stagingRemove.keySet()) {
            System.out.println(s);
        }
        System.out.println();

        /** Modifications not staged for commit */
        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit currCommit = getCurrentCommit();
        // Tracked in the current commit, changed in the working directory, but not staged;
        for (String s : plainFilenamesIn(CWD)) {
            File file = join(CWD, s);
            Blob blob = new Blob(s, file);
            if (currCommit.getFileMap().containsKey(s)) {
                if (!currCommit.getFileMap().get(s).equals(blob.getUID()) && !stagingArea.stagingAdd.containsKey(s)) {
                    System.out.println(s + " (modified)");
                }
            }
            // Staged for addition, but with different contents than in the working directory
            if (stagingArea.stagingAdd.containsKey(s) && !stagingArea.stagingAdd.get(s).equals(blob.getUID())) {
                System.out.println(s + " (modified)");
            }
        }
        // Staged for addition, but deleted in the working directory
        for (String s : stagingArea.stagingAdd.keySet()) {
            if (!join(CWD, s).exists()) {
                System.out.println(s + " (deleted)");
            }
        }
        // Not staged for removal, but tracked in the current commit and deleted from the working directory
        for (String s : currCommit.getFileMap().keySet()) {
            File file = join(CWD, s);
            if (!stagingArea.stagingRemove.containsKey(s) && !join(CWD, s).exists()) {
                System.out.println(s + " (deleted)");
            }
        }
        System.out.println();

        /** Untracked files */
        System.out.println("=== Untracked Files ===");
        for (String s : plainFilenamesIn(CWD)) {
            if (!stagingArea.stagingAdd.containsKey(s) && !currCommit.getFileMap().containsKey(s)) {
                System.out.println(s);
            }
        }
        System.out.println();
    }

    public void checkoutFile(String filename) {
        Commit currCommit = getCurrentCommit();
        if (!currCommit.getFileMap().containsKey(filename)) {
            exitWithError("File does not exist in that commit.");
        }
        String blobUID = currCommit.getFileMap().get(filename);
        copyFile(blobUID);
    }

    public void checkoutFileWithCommit(String commitID, String filename) {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        String s = getFullCommitID(commits, commitID);
        if (s == null) {
            exitWithError("No commit with that id exists.");
        }
        Commit commit = readObject(join(COMMIT_DIR, s), Commit.class);
        if (!commit.getFileMap().containsKey(filename)) {
            exitWithError("File does not exist in that commit.");
        }
        String blobUID = commit.getFileMap().get(filename);
        copyFile(blobUID);
    }

    public void checkoutBranch(String branchName) {
        List<String> branches = plainFilenamesIn(HEADS_DIR);
        if (!branches.contains(branchName)) {
            exitWithError("No such branch exists.");
        }
        if (currBranch().equals(branchName)) {
            exitWithError("No need to checkout the current branch.");
        }
        Commit currCommit = getCurrentCommit();
        setHead(branchName);
        Commit checkout = getCurrentCommit();
        Staging stagingArea = readObject(INDEX, Staging.class);
        checkUntracked(currCommit, checkout, stagingArea);
        checkoutFiles(currCommit, checkout);
        clearStaging(stagingArea);
    }

    public void branch(String branchName) {
        List<String> branches = plainFilenamesIn(HEADS_DIR);
        if (branches.contains(branchName)) {
            exitWithError("A branch with that name already exists.");
        }
        File file = join(HEADS_DIR, branchName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Commit currCommit = getCurrentCommit();
        currCommit.setSplit(true);
        currCommit.getBranches().add(branchName);
        currCommit.getBranches().add(currBranch());
        writeObject(join(COMMIT_DIR, currCommit.getUID()), currCommit);
        writeContents(file, currCommit.getUID());
    }

    public void rmBranch(String branchName) {
        List<String> branches = plainFilenamesIn(HEADS_DIR);
        if (!branches.contains(branchName)) {
            exitWithError("A branch with that name does not exist.");
        }
        if (currBranch().equals(branchName)) {
            exitWithError("Cannot remove the current branch.");
        }
        File file = join(HEADS_DIR, branchName);
        file.delete();
    }

    public void reset(String commitID) {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        String s = getFullCommitID(commits, commitID);
        if (s == null) {
            exitWithError("No commit with that id exists.");
        }
        Commit currCommit = getCurrentCommit();
        Commit resetCommit = readObject(join(COMMIT_DIR, s), Commit.class);
        Staging stagingArea = readObject(INDEX, Staging.class);
        checkUntracked(currCommit, resetCommit, stagingArea);
        checkoutFiles(currCommit, resetCommit);
        writeContents(join(HEADS_DIR, currBranch()), s);
        clearStaging(stagingArea);
    }

    public void merge(String branchName) {
        Staging stagingArea = readObject(INDEX, Staging.class);
        if (!stagingArea.stagingAdd.isEmpty() || !stagingArea.stagingRemove.isEmpty()) {
            exitWithError("You have uncommitted changes.");
        }
        List<String> branches = plainFilenamesIn(HEADS_DIR);
        if (!branches.contains(branchName)) {
            exitWithError("A branch with that name does not exist.");
        }
        String currBranch = currBranch();
        if (currBranch.equals(branchName)) {
            exitWithError("Cannot merge a branch with itself.");
        }
        Commit currCommit = getCurrentCommit();
        Commit given = readObject(join(COMMIT_DIR, readContentsAsString(join(HEADS_DIR, branchName))), Commit.class);
        checkUntracked(currCommit, given, stagingArea);

        /** Get split point */
        Commit split = given;
        while (!split.isSplit() && !split.getBranches().contains(currBranch)) {
            split = readObject(join(COMMIT_DIR, split.getParentID()), Commit.class);
        }

        if (split.getUID().equals(given.getUID())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (split.getUID().equals(currCommit.getUID())) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        for (String s : given.getFileMap().keySet()) {
            if (split.getFileMap().containsKey(s) && currCommit.getFileMap().containsKey(s)) {
                if (!split.getFileMap().get(s).equals(given.getFileMap().get(s)) &&
                        split.getFileMap().get(s).equals(currCommit.getFileMap().get(s))) {
                    copyFile(given.getFileMap().get(s));
                    stagingArea.stagingAdd.put(s, given.getFileMap().get(s));
                }
            }
            if (!split.getFileMap().containsKey(s) && !currCommit.getFileMap().containsKey(s)) {
                copyFile(given.getFileMap().get(s));
                stagingArea.stagingAdd.put(s, given.getFileMap().get(s));
            }
            if (currCommit.getFileMap().containsKey(s)) {
                if (!currCommit.getFileMap().get(s).equals(given.getFileMap().get(s))) {
                    mergeConflict(currCommit, given, s, stagingArea);
                }
            }
        }

        for (String s : split.getFileMap().keySet()) {
            if (currCommit.getFileMap().containsKey(s)) {
                if (currCommit.getFileMap().get(s).equals(split.getFileMap().get(s)) &&
                !given.getFileMap().containsKey(s)) {
                    restrictedDelete(join(CWD, s));
                    stagingArea.stagingRemove.remove(s);
                }
            }
            if (given.getFileMap().containsKey(s) && !currCommit.getFileMap().containsKey(s)) {
                if (!split.getFileMap().get(s).equals(given.getFileMap().get(s))) {
                    mergeConflict(currCommit, given, s, stagingArea);
                }
            }
            if (currCommit.getFileMap().containsKey(s) && !given.getFileMap().containsKey(s)) {
                if (!split.getFileMap().get(s).equals(currCommit.getFileMap().get(s))) {
                    mergeConflict(currCommit, given, s, stagingArea);
                }
            }
        }
        writeObject(INDEX,stagingArea);
        Commit newCommit = new Commit("Merged "+ branchName +" into "+ currBranch +".",
                currCommit.getUID(), currCommit.getFileMap());
        newCommit.setSecondParent(given.getUID());
        stageCommit(newCommit);
        saveCommit(newCommit, currBranch);
        clearStaging(stagingArea);
    }


    /** Helper methods */

    private void saveCommit(Commit commit, String branch) {
        /** Save commit as an object in commits folder */
        File file = join(COMMIT_DIR, commit.getUID());
        try {
            file.createNewFile();
            writeObject(file, commit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /** Store current branch head */
        File branchname = join(HEADS_DIR, branch);
        if (!branchname.exists()) {
            try {
                branchname.createNewFile();
                writeContents(branchname, commit.getUID());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(branchname, commit.getUID());
    }

    /** Update fileMap in the current Commit */
    private void stageCommit(Commit commit) {
        Staging stagingArea = readObject(INDEX, Staging.class);
        for (String a : stagingArea.stagingAdd.keySet()) {
            commit.getFileMap().put(a, stagingArea.stagingAdd.get(a));
        }
        for (String a : stagingArea.stagingRemove.keySet()) {
            commit.getFileMap().remove(a);
        }
    }

    private Commit getCurrentCommit() {
        String currCommitID = readContentsAsString(join(GITLET_DIR, readContentsAsString(HEAD)));
        return readObject(join(COMMIT_DIR, currCommitID), Commit.class);
    }

    private void copyFile(String blobUID) {
        File file = join(BLOB_DIR, blobUID);
        Blob blob = readObject(file, Blob.class);
        File newFile = join(CWD, blob.getFilename());
        if (newFile.exists()) {
            writeContents(newFile, blob.getContent());
        }
        try {
            newFile.createNewFile();
            writeContents(newFile, blob.getContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkUntracked(Commit curr, Commit target, Staging sa) {
        for (String s : plainFilenamesIn(CWD)) {
            if (!sa.stagingAdd.containsKey(s) && !curr.getFileMap().containsKey(s) && target.getFileMap().containsKey(s)) {
                exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
    }

    private void checkoutFiles(Commit curr, Commit target) {
        for (String s : target.getFileMap().values()) {
            copyFile(s);
        }
        for (String s : curr.getFileMap().keySet()) {
            if (!target.getFileMap().containsKey(s)) {
                restrictedDelete(join(CWD, s));
            }
        }
    }

    private String getFullCommitID (List<String> commits, String commitID) {
        if (commitID.length() == 6) {
            for (String s : commits) {
                if (s.contains(commitID)) { return s; }
            }
            return null;
        } else if (commitID.length() == 40) {
            if (commits.contains(commitID)) {
                return commitID;
            } else { return null; }
        } else { return null; }
    }

    private void clearStaging(Staging sa) {
        sa.clearStage();
        writeObject(INDEX, sa);
    }

    private void setHead(String branch) {
        writeContents(HEAD, join("refs", "heads", branch).getPath());
    }

    private String currBranch() {
        return new File(readContentsAsString(HEAD)).getName();
    }

    private void mergeConflict(Commit currCommit, Commit given, String s, Staging sa) {
        message("Encountered a merge conflict.");
        byte[] currentBytes = "".getBytes();
        byte[] givenBytes = "".getBytes();
        if (currCommit.getFileMap().containsKey(s)) {
            currentBytes = readContents(join(BLOB_DIR, currCommit.getFileMap().get(s)));
        }
        if (given.getFileMap().containsKey(s)) {
            givenBytes = readContents(join(BLOB_DIR, given.getFileMap().get(s)));
        }
        File file = join(CWD, s);
        writeContents(file, "<<<<<<< HEAD\n",currentBytes,"=======\n",givenBytes,">>>>>>>\n");
        Blob blob = new Blob(s, file);
        blob.storeBlob();
        sa.stagingAdd.put(s, blob.getUID());
    }

}

