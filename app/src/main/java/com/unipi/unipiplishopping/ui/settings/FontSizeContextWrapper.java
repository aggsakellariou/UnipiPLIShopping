package com.unipi.unipiplishopping.ui.settings;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;

public class FontSizeContextWrapper extends ContextWrapper {

    public FontSizeContextWrapper(Context base) {
        super(base);
    }

    // Method to change the font size of the app
    public static ContextWrapper wrap(Context context, float fontSize) {
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();
        configuration.fontScale = fontSize;
        context = context.createConfigurationContext(configuration);

        return new FontSizeContextWrapper(context);
    }
}