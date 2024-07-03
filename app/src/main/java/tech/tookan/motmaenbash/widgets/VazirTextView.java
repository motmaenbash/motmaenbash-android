package tech.tookan.motmaenbash.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import tech.tookan.motmaenbash.utils.UiUtils;


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
        setTypeface(UiUtils.getTypeFace(getContext()),
                Typeface.NORMAL);
    }
}
