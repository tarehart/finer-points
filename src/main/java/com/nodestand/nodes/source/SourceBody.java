package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionHelper;

public class SourceBody extends ArgumentBody {

    private String url;

    public SourceBody() {}

    @Override
    public SourceNode constructNode(VersionHelper versionHelper) {
        return new SourceNode(this, VersionHelper.startBuild(author));
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

    public void setUrl(String url) throws ImmutableNodeException {
        if (!isDraft()) {
            throw new ImmutableNodeException("Cannot edit a url unless the source is in draft mode. Must create a new version.");
        }
        this.url = url;
    }
}
