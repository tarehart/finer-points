package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.User;
import com.nodestand.nodes.version.Build;

public class SourceBody extends ArgumentBody {

    public String url;

    public SourceBody() {}

    @Override
    public ArgumentNode constructNode(Build build) {
        return new SourceNode(this, build);
    }

    public SourceBody(String title, User author, String url) {
        super(title, author);
        this.url = url;
    }
}
