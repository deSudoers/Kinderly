package com.hackharvard.desudoers.kinderly;

import android.graphics.Bitmap;

public class Card {
    private String line1;
    private String line2;
    private String url;

    public Card(String line1, String line2, String url) {
        this.line1 = line1;
        this.line2 = line2;
        this.url = url;
    }

    public String getLine1() { return line1; }

    public String getLine2() {
        return line2;
    }

    public String getURL() { return url; }

}
