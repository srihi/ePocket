package com.sharad.epocket;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sharad.utils.CircleButton;


public class CategoryActivity extends ActionBarActivity {
    public static final int UNASSIGNED = -1;
    private Toolbar mToolbar;
    private RelativeLayout mColorPicker;
    private RelativeLayout mIconPicker;
    private EditText mLabelText;

    private CircleButton mColorButtons[];
    private CircleButton mIconButtons[];

    private int mColorId = UNASSIGNED;
    private int mIconId = UNASSIGNED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        initToolbar();
        setupColorPicker();
        setupIconPicker();

        mLabelText = (EditText) findViewById(R.id.label_text);

        FloatingActionButton myFab = (FloatingActionButton) this.findViewById(R.id.fabButton);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mColorId == UNASSIGNED) {
                    mColorPicker.startAnimation(getBlinkAnimation());
                } else if(mIconId == UNASSIGNED) {
                    mIconPicker.startAnimation(getBlinkAnimation());
                } else {
                    /* Handle update and exit */
                    onBackPressed();
                }
            }
        });
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public Animation getBlinkAnimation(){
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(600);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);

        return animation;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_delete:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mLabelText.isFocused()) {
                Rect outRect = new Rect();
                mLabelText.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    mLabelText.clearFocus();
                    //
                    // Hide keyboard
                    //
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mLabelText.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void setupColorPicker() {
        mColorPicker = (RelativeLayout) findViewById(R.id.colorPicker);

        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mColorPicker.removeAllViews();
        mColorPicker.addView(relativeLayout, params);

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        relativeLayout.addView(linearLayout, params1);

        int[] palette = getResources().getIntArray(R.array.light_palette);
        mColorButtons = new CircleButton[palette.length];
        for (int i = 0; i < (palette.length/6); i++) {
            LinearLayout linearLayout1 = new LinearLayout(this);
            linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.addView(linearLayout1, params1);
            for (int j = 0; j < 6; j++) {
                CircleButton cb = newPaletteButton(palette[i * 6 + j]);
                mColorButtons[i * 6 + j] = cb;
                linearLayout1.addView(cb);
            }
        }
    }

    private CircleButton newPaletteButton(final int color) {
        final CircleButton cb = new CircleButton(this);
        int size = getResources().getDimensionPixelSize(R.dimen.color_circle_diameter);
        cb.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        cb.setColor(color);
        cb.setChecked(false);
        cb.setButtonType(CircleButton.CIRCLE_BUTTON_TYPE_COLOR);
        cb.setImageResource(R.drawable.ic_check_white_24dp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int[] attrs = new int[]{android.R.attr.selectableItemBackgroundBorderless};
            cb.setBackground(obtainStyledAttributes(attrs).getDrawable(0));
        }
        cb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View button) {
                int[] location = new int[2];
                cb.getLocationOnScreen(location);
                mColorId = cb.isChecked() ? UNASSIGNED : color;
                updateColorGroup(cb);
            }
        });
        return cb;
    }

    private void updateColorGroup(CircleButton cb) {
        for(int i=0; i<mColorButtons.length; i++) {
            if(mColorButtons[i] != cb) {
                mColorButtons[i].setChecked(false);
            }
        }
    }

    private void setupIconPicker() {
        mIconPicker = (RelativeLayout) findViewById(R.id.iconPicker);

        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mIconPicker.removeAllViews();
        mIconPicker.addView(relativeLayout, params);

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        linearLayout.setOrientation(LinearLayout.VERTICAL);
        relativeLayout.addView(linearLayout, params1);

        TypedArray ids = getResources().obtainTypedArray(R.array.icon_resource);

        mIconButtons = new CircleButton[ids.length()];
        for (int i = 0; i < (ids.length()/6); i++) {
            LinearLayout linearLayout1 = new LinearLayout(this);
            linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.addView(linearLayout1, params1);
            for (int j = 0; j < 6; j++) {
                int index = ids.getResourceId(i * 6 + j, R.mipmap.ic_card_giftcard_black_24dp);
                CircleButton ib = newLabelButton(index);
                mIconButtons[i * 6 + j] = ib;
                linearLayout1.addView(ib);
            }
        }
        ids.recycle();
    }

    private CircleButton newLabelButton(final int label) {
        final CircleButton ib = new CircleButton(this);
        int size = getResources().getDimensionPixelSize(R.dimen.color_circle_diameter);
        ib.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        ib.setImageResource(label);
        ib.setColor(getResources().getColor(R.color.primary_dark));
        ib.setChecked(false);
        ib.setButtonType(CircleButton.CIRCLE_BUTTON_TYPE_ICON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int[] attrs = new int[]{android.R.attr.selectableItemBackgroundBorderless};
            ib.setBackground(obtainStyledAttributes(attrs).getDrawable(0));
        } else {
            int[] attrs = new int[]{android.R.attr.selectableItemBackground};
            ib.setBackground(obtainStyledAttributes(attrs).getDrawable(0));
        }

        ib.setColorFilter(getResources().getColor(R.color.icons_dark), getResources().getColor(R.color.icons));
        ib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View button) {
                mIconId = ib.isChecked() ? UNASSIGNED : label;
                updateIconGroup(ib);
            }
        });
        return ib;
    }

    private void updateIconGroup(CircleButton ib) {
        for(int i=0; i<mIconButtons.length; i++) {
            if(mIconButtons[i] != ib) {
                mIconButtons[i].setChecked(false);
            }
        }
    }
}
