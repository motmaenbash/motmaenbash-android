package nu.milad.motmaenbash.utils;

import android.content.Context;
import android.graphics.Typeface;

import androidx.collection.LruCache;
import androidx.core.content.res.ResourcesCompat;

import nu.milad.motmaenbash.R;

public class UiUtils {

    private static final LruCache<String, Typeface> sTypeFaceCache = new LruCache<>(12);

    public static Typeface getTypeFace(Context context) {

        String TYPEFACE_NAME = "vazir_matn_regular.ttf";

        Typeface sTypeFace1 = sTypeFaceCache.get(TYPEFACE_NAME);

        if (sTypeFace1 == null) {
            Typeface sTypeFace = ResourcesCompat.getFont(context, R.font.vazirmatn_medium);
            sTypeFaceCache.put(TYPEFACE_NAME, sTypeFace);
        }
        return sTypeFace1;
    }

}
