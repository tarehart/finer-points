package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.Build;
import com.nodestand.nodes.version.VersionHelper;

public class SourceBody extends ArgumentBody {

    public String url;

    public SourceBody() {}

    @Override
    public SourceNode constructNode(VersionHelper versionHelper) {
        return new SourceNode(this, versionHelper.beginBodyBuild(this));
    }

    public SourceBody(String title, User author, String url) {
        super(title, author);
        this.url = url;
    }
}
