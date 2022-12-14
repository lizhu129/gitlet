package gitlet;

import java.io.File;
import java.util.List;

/** A debugging class whose main program may be invoked as follows:
 *      java gitlet.DumpObj FILE...
 *  where each FILE is a file produced by Utils.writeObject (or any file
 *  containing a serialized object).  This will simply read FILE,
 *  deserialize it, and call the dump method on the resulting Object.
 *  The object must implement the gitlet.Dumpable interface for this
 *  to work.  For example, you might define your class like this:
 *
 *        import java.io.Serializable;
 *        import java.util.TreeMap;
 *        class MyClass implements Serializeable, Dumpable {
 *            ...
 *            @Override
 *            public void dump() {
 *               System.out.printf("size: %d%nmapping: %s%n", _size, _mapping);
 *            }
 *            ...
 *            int _size;
 *            TreeMap<String, String> _mapping = new TreeMap<>();
 *        }
 *
 *  As illustrated, your dump method should print useful information from
 *  objects of your class.
 *  @author P. N. Hilfinger
 */
public class DumpObj {

    /** Deserialize and apply dump to the contents of each of the files
     *  in FILES. */
    public static void main(String... files) {
        File file = new File("/Users/lizhu/test-gitlet/.gitlet/objects/commits");
        List<String> a = Utils.plainFilenamesIn(file);
        for (String fileName : a) {
            Dumpable obj = Utils.readObject(Utils.join(file, fileName),
                                            Dumpable.class);
            obj.dump();
            System.out.println("---");
        }
    }
}

