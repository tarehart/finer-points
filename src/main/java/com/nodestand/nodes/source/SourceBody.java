package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionHelper;

public class SourceBody extends ArgumentBody {

    private String url;

    public SourceBody() {}

    @Override
    public SourceNode constructNode(VersionHelper versionHelper) {
        return new SourceNode(this, versionHelper.startBuild(this));
    }

    public SourceBody(String title, User author, String url) {
        this(title, author, url, null);
    }

    public SourceBody(String title, User author, String url, MajorVersion majorVersion) {
        super(title, author, majorVersion);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
