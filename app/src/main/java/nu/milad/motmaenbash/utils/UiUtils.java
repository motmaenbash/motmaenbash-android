package nu.milad.motmaenbash.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;

import androidx.collection.LruCache;
import androidx.core.content.res.ResourcesCompat;

import nu.milad.motmaenbash.R;

public class UiUtils {

    private static final LruCache<String, Typeface> sTypeFaceCache = new LruCache<>(12);
    private static String TYPEFACE_NAME;
    private static Typeface sTypeFace;

    public static Typeface getTypeFace(Context context) {

        TYPEFACE_NAME = "vazir_matn_regular.ttf";

        sTypeFace = sTypeFaceCache.get(TYPEFACE_NAME);

        if (sTypeFace == null) {
            Typeface sTypeFace = ResourcesCompat.getFont(context, R.font.vazirmatn_medium);
            sTypeFaceCache.put(TYPEFACE_NAME, sTypeFace);
        }
        return sTypeFace;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


}
