package com.nodestand.nodes;

public class Source extends ArgumentNode {
    private static final String TYPE = "source";

    public String url;

    public Source() {}

    public Source(String title, String url) {
        super(title);
        this.url = url;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
