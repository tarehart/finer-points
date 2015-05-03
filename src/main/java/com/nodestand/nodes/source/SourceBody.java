package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.User;

public class SourceBody extends ArgumentBody {
    private static final String TYPE = "source";

    public String url;

    public SourceBody() {}

    public SourceBody(String title, User author, String url) {
        super(title, author);
        this.url = url;
    }
}
