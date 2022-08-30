package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @LiZhu
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
       // If user input is empty, return error message and exit.
        if (args.length == 0) {
            Utils.exitWithError("Please enter a command.");
        }
        String firstArg = args[0];

        if (!Repository.GITLET_DIR.exists() && !firstArg.equals("init")) {
            Utils.exitWithError("Not in an initialized Gitlet directory.");
        }

        Gitlet gitlet = new Gitlet();

        switch(firstArg) {
            /** "init", "add", "commit", "rm", "log", "global-log",
             * "find", "status", "checkout", "branch", "rm-branch", "reset", "merge" */
            case "init":
                validateNumArgs(args, 1);
                gitlet.init();
                break;
            case "add":
                validateNumArgs(args, 2);
                gitlet.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                gitlet.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                gitlet.rm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                gitlet.log();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                gitlet.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                gitlet.find(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                gitlet.status();
                break;
            case "checkout":
                switch (args.length){
                    case 3:
                        if (args[1].equals("--")){
                            gitlet.checkoutFile(args[2]);
                        }
                        break;
                    case 4:
                        if (args[2].equals("--")){
                            gitlet.checkoutFileWithCommit(args[1],args[3]);
                        }
                        break;
                    case 2:
                        gitlet.checkoutBranch(args[1]);
                        break;
                }
                break;
            case "branch":
                validateNumArgs(args, 2);
                gitlet.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                gitlet.rmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                gitlet.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                gitlet.merge(args[1]);
                break;
            default:
                Utils.exitWithError("No command with that name exists.");
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            Utils.exitWithError("Incorrect operands.");
        }
    }
}
