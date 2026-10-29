package android.content;

import android.content.res.Resources;

public final class Context {
    private final Resources resources;

    public Context(Resources resources) {
        this.resources = resources;
    }

    public Resources getResources() {
        return resources;
    }
}
