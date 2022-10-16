# Gitlet
# Description: a command-line program using Java, which is a simplified version of GIT, namely a version control system.

# Objects:
    1. Blob: an object to store the content of files.
    2. Commit: an object that represents a commit.
    3. Staging: an object that represents the statging area, including files to be added and removed in a commit.

# Repository Structure:
- .gitlet
    - objects: dirctory to store objects
        - commits: directiry to store commits
        - blobs: directory to store blobs
    - refs: directory to store heads and branches
        - heads: directory to store the heads of each branch
        - remotes: directory to store the heads of remote branches
    - INDEX: file that stores staging area
    - HEAD: file that stores the HEAD pointer

# Commands:
    1. init: java gitlet.Main init
    2. add: java gitlet.Main add [file name]
    3. commit: java gitlet.Main commit [message]
    4. rm: java gitlet.Main rm [file name]
    5. log: java gitlet.Main log
    6. global-log: java gitlet.Main global-log
    7. find: java gitlet.Main find [commit message]
    8. status: java gitlet.Main status
    9. checkout: 
        a. java gitlet.Main checkout -- [file name]
        b. java gitlet.Main checkout [commit id] -- [file name]
        c. java gitlet.Main checkout [branch name]
    10. branch: java gitlet.Main branch [branch name]
    11. rm-branch: java gitlet.Main rm-branch [branch name]
    12. reset: java gitlet.Main reset [commit id]
    13. merge: java gitlet.Main merge [branch name]
