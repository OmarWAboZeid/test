package org.example.versioningTools;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.List;

class Github implements VersionControl {
    public static void main(String[] args) throws IOException {
        String username = "OmarWAboZeid";
        String accessToken = "ad";
        GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
        //        Collections<GHRepository> repositories =

        for(GHRepository r : github.getUser(username).getRepositories().values()) {
            System.out.println("r: "+ r.getName());
        }

        String repoName = "hamadahat";
        GHRepository repo = github.createRepository(repoName).create();
        repo.addCollaborators(github.getUser("abayer"),github.getUser("rtyler"));
        //        repo.delete();


    }


    @Override
    public void commit(String filename, String versionName) {
        
    }

    @Override
    public List<String> getCommitMessages() {
        return null;
    }

    @Override
    public String getVersion(String commitMessage) {
        return null;
    }

    @Override
    public void commitVersion(String fileContent, String desiredVersion) {

    }
}


