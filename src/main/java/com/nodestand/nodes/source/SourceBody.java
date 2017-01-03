package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.MajorVersion;

import java.util.HashSet;
import java.util.Set;

public class SourceBody extends ArgumentBody {

    private String url;

    public SourceBody() {}

    @Override
    public SourceNode constructNode() {
        return new SourceNode(this);
    }

    @Override
    public Set<String> getMajorVersionsFromBodyText() {
        return new HashSet<>();
    }

    public SourceBody(String title, String qualifier, Author author, String url) {
        this(title, qualifier, author, url, null);
    }

    public SourceBody(String title, String qualifier, Author author, String url, MajorVersion majorVersion) {
        super(title, qualifier, author, majorVersion);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) throws ImmutableNodeException {
        this.url = url;
    }
}
