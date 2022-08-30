package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Utils.*;

/** Represents a gitlet blob object.
 *
 *  @LiZhu
 */
public class Blob implements Serializable {

    private String UID;
    private String filename;
    private byte[] content;

    public Blob(String filename, File file) {
        this.filename = filename;
        this.content = readContents(file);
        this.UID = sha1(this.content);
    }

    public String getUID() {
        return UID;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getContent() {
        return content;
    }

    public void storeBlob() {
        File file = join(Repository.BLOB_DIR, this.UID);
        try {
            file.createNewFile();
            writeObject(file, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
