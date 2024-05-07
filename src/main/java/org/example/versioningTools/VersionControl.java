package org.example.versioningTools;

import java.util.List;

public interface VersionControl {
    void commit(String filename, String versionName);

    public List<String> getCommitMessages();

    public String getVersion(String version);

    void commitVersion(String fileContent, String desiredVersion);
}



