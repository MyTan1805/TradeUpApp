package com.example.tradeup.ui.listing;

import android.net.Uri;

// Lớp cha để bọc String (URL) hoặc Uri (file cục bộ)
public abstract class ImageSource {
    private ImageSource() {}

    public static final class ExistingUrl extends ImageSource {
        public final String url;
        public ExistingUrl(String url) { this.url = url; }
    }

    public static final class NewUri extends ImageSource {
        public final Uri uri;
        public NewUri(Uri uri) { this.uri = uri; }
    }
}