package nu.milad.motmaenbash.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import nu.milad.motmaenbash.R;
import nu.milad.motmaenbash.utils.UiUtils;


public class VazirTextView extends AppCompatTextView {

    public VazirTextView(Context context) {
        super(context);
        if (!isInEditMode())
            setFont();
    }

    public VazirTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            setFont();
    }

    public VazirTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode())
            setFont();
    }

    public void setFont() {
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.vazirmatn);
        if (typeface != null) {
            setTypeface(typeface);
        } else {
            setTypeface(UiUtils.getTypeFace(getContext()), Typeface.NORMAL);
        }
    }
}