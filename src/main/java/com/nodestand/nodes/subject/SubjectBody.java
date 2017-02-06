package com.nodestand.nodes.subject;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.MajorVersion;

import java.util.HashSet;
import java.util.Set;

public class SubjectBody extends ArgumentBody {

    private String url;

    public SubjectBody() {}

    @Override
    public SubjectNode constructNode() {
        return new SubjectNode(this);
    }

    @Override
    public Set<String> getMajorVersionsFromBodyText() {
        return new HashSet<>();
    }

    public SubjectBody(String title, String qualifier, Author author, String url) {
        this(title, qualifier, author, url, null);
    }

    public SubjectBody(String title, String qualifier, Author author, String url, MajorVersion majorVersion) {
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
