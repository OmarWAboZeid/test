package org.example.versioningTools;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Git implements VersionControl {
    public Git() { /* NO-OP */ }
    public static void main(String[] args) throws IOException {
        // Create the git repository with init
        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.init().setDirectory(new File(".")).call()) {
            System.out.println("Created repository: " + git.getRepository().getDirectory());
            File myFile = new File(git.getRepository().getDirectory().getParent(), "testfile");
            if (!myFile.createNewFile()) {
                throw new IOException("Could not create file " + myFile);
            }

            // run the add-call
            git.add().addFilepattern("testfile").call();

            git.commit().setMessage("Initial commit").call();
            System.out.println("Committed file " + myFile + " to repository at " + git.getRepository().getDirectory());
            // Create a few branches for testing
            for (int i = 0; i < 10; i++) {
                git.checkout().setCreateBranch(true).setName("new-branch" + i).call();
            }
            // List all branches
            List<Ref> call = git.branchList().call();
            for (Ref ref : call) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }

            // Create a few new files
            for (int i = 0; i < 10; i++) {
                File f = new File(git.getRepository().getDirectory().getParent(), "testfile" + i);
                f.createNewFile();
                if (i % 2 == 0) {
                    git.add().addFilepattern("testfile" + i).call();
                }
            }

            Status status = git.status().call();

            Set<String> added = status.getAdded();
            for (String add : added) {
                System.out.println("Added: " + add);
            }
            Set<String> uncommittedChanges = status.getUncommittedChanges();
            for (String uncommitted : uncommittedChanges) {
                System.out.println("Uncommitted: " + uncommitted);
            }

            Set<String> untracked = status.getUntracked();
            for (String untrack : untracked) {
                System.out.println("Untracked: " + untrack);
            }

            // Find the head for the repository
            ObjectId lastCommitId = git.getRepository().resolve(Constants.HEAD);
            System.out.println("Head points to the following commit :" + lastCommitId.getName());
        } catch (NoHeadException e) {
            throw new RuntimeException(e);
        } catch (AmbiguousObjectException e) {
            throw new RuntimeException(e);
        } catch (UnmergedPathsException e) {
            throw new RuntimeException(e);
        } catch (NoFilepatternException e) {
            throw new RuntimeException(e);
        } catch (IncorrectObjectTypeException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        } catch (AbortedByHookException e) {
            throw new RuntimeException(e);
        } catch (CheckoutConflictException e) {
            throw new RuntimeException(e);
        } catch (RefNotFoundException e) {
            throw new RuntimeException(e);
        } catch (RefAlreadyExistsException e) {
            throw new RuntimeException(e);
        } catch (InvalidRefNameException e) {
            throw new RuntimeException(e);
        } catch (WrongRepositoryStateException e) {
            throw new RuntimeException(e);
        } catch (ConcurrentRefUpdateException e) {
            throw new RuntimeException(e);
        } catch (NoMessageException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit(String filename, String versionName) {
        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(new File("."))) {
            git.checkout().setName(versionName).call();
            git.add().addFilepattern(filename).call();
            git.commit().setMessage("Version " + versionName).call();

        } catch (NoHeadException e) {
            throw new RuntimeException(e);
        } catch (UnmergedPathsException e) {
            throw new RuntimeException(e);
        } catch (NoFilepatternException e) {
            throw new RuntimeException(e);
        } catch (WrongRepositoryStateException e) {
            throw new RuntimeException(e);
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        } catch (ConcurrentRefUpdateException e) {
            throw new RuntimeException(e);
        } catch (AbortedByHookException e) {
            throw new RuntimeException(e);
        } catch (NoMessageException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<String> getCommitMessages() {

        List<String> commitMessages = new ArrayList<>();
        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(new File("."))) {
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit commit : commits) {
                commitMessages.add(commit.getShortMessage());
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return commitMessages;
    }

    @Override
    public String getVersion(String commitMessage) {
        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(new File("."))) {
            Repository repository = git.getRepository();
            Iterable<RevCommit> commits = git.log().call();

            RevCommit chosenCommit = null;
            for (RevCommit commit : commits) {
                System.out.println("Commit: " + commit.getName() + ", Message: " + commit.getShortMessage());
                if (commit.getShortMessage().equals(commitMessage)) {
                    chosenCommit = commit;
                    break;
                }
            }

            if (chosenCommit == null) return null;
            // Get the commit's tree
            RevTree tree = chosenCommit.getTree();


            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);

                while (treeWalk.next()) {
                    if (treeWalk.getPathString().equals("hamada.txt")) {
                        // Read the content of the file
                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);
                        byte[] bytes = loader.getBytes();
                        String content = new String(bytes, StandardCharsets.UTF_8);
                        System.out.println("File content at commit " + chosenCommit.getShortMessage() + ":\n" + content);
                        return content;
                    }
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoHeadException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void commitVersion(String fileContent, String desiredVersion) {

        // Write content to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("hamada.txt"))) {
            writer.write(fileContent);
            System.out.println("Content has been written to the file successfully.");
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }

        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(new File("."))) {
            String remoteUri = "https://github.com/OmarWAboZeid/hamadahat.git"; // Replace with your remote repository URI
            git.add().addFilepattern("hamada.txt").call();
            git.commit().setMessage(desiredVersion).call();
//            pushToRemote(git, remoteUri);
            pushToRemote2(git, remoteUri);

        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
//        List<String> versions = new ArrayList<>();
//        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(new File("."))) {
//            Iterable<RevCommit> commits = git.log().call();
//            for (RevCommit commit : commits) {x§
//                versions.add(commit.getShortMessage());
//            }
//        } catch (IOException | GitAPIException e) {
//            e.printStackTrace();
//        }
//
//        if (versions.get(0).equals(desiredVersion)) return;
//
//        try (org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.open(new File("."))) {
//            Iterable<RevCommit> commits = git.log().call();
//            for (RevCommit commit : commits) {
//                if (commit.getShortMessage().equals(desiredVersion)) {
//                    CherryPickResult cherryPickResult = git.cherryPick().setContentMergeStrategy(ContentMergeStrategy.THEIRS).include(commit).call();
//                    if (cherryPickResult.getStatus().name().equals("FAILED")) {
//
//                    }
//                }
//            }
//        } catch (IOException | GitAPIException e) {
//            e.printStackTrace();
//        }
    }

    private void pushToRemote2(org.eclipse.jgit.api.Git git, String httpUrl) throws URISyntaxException, GitAPIException {
        // add remote repo:
        RemoteAddCommand remoteAddCommand = git.remoteAdd();
        remoteAddCommand.setName("origin");
        remoteAddCommand.setUri(new URIish(httpUrl));
        // you can add more settings here if needed
        remoteAddCommand.call();

        // push to remote:
        PushCommand pushCommand = git.push();
        pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("OmarWAboZeid", System.getenv("GIT_PASS")));
        // you can add more settings here if needed
        Iterator<PushResult> res = (Iterator<PushResult>) pushCommand.call().iterator();
        while(res.hasNext()) {
            for (RemoteRefUpdate r : res.next().getRemoteUpdates()) {
                System.out.println("message: " + r.getMessage());
                System.out.println("status: " + r.getStatus());
                System.out.println("srcRef: " + r.getSrcRef());
            }
        }

    }

    private void pushToRemote(org.eclipse.jgit.api.Git git, String remoteUri) throws URISyntaxException, GitAPIException {
        git.remoteSetUrl().setRemoteUri(new URIish(remoteUri)).setUriType(RemoteSetUrlCommand.UriType.PUSH);

        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("OmarWAboZeid", System.getenv("GIT_PASS"));

        // Push the local repository to the remote
        PushCommand pushCommand = git.push();
        pushCommand.setCredentialsProvider(credentialsProvider);
        pushCommand.setRemote(remoteUri);
        Iterator<PushResult> res = (Iterator<PushResult>) pushCommand.call().iterator();
        while(res.hasNext()) {
            for (RemoteRefUpdate r : res.next().getRemoteUpdates()) {
                System.out.println("message: " + r.getMessage());
                System.out.println("status: " + r.getStatus());
                System.out.println("srcRef: " + r.getSrcRef());
            }
        }

        System.out.println("Pushed to remote repo: " + remoteUri);
    }
}