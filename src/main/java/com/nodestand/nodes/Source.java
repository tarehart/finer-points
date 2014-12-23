package com.nodestand.nodes;

public class Source extends ArgumentNode {
    private static final String TYPE = "source";

    public String url;

    public Source() {}

    public Source(String title, User author, String url) {
        super(title, author);
        this.url = url;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
