package uk.me.redmonds.contactsync;

import android.view.*;
import android.view.View.OnTouchListener;

public class ButtonTouch implements OnTouchListener
{
    public boolean onTouch(View v, MotionEvent event) {
        /*if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.color.pressed_redmond);
        } else {
            v.setBackgroundResource(R.color.dark);
        }*/

        return false;
    }
}