package com.example.eventerapp.utils;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.transition.Slide;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.example.eventerapp.R;
import com.example.eventerapp.RoomActivity;

public class SwipeListener implements View.OnTouchListener {

    Integer floorId;

    Integer floors;

    Context context;

    String buildingId;

    RoomActivity currentActivity;

    Window window;

    public SwipeListener(Context context, int floorId, int floors, String buildingId, RoomActivity currentActivity, Window window) {
        this.floorId = floorId;
        this.floors = floors;
        this.context = context;
        this.buildingId = buildingId;
        this.currentActivity = currentActivity;
        this.window = window;
    }

    private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > 100) {
                if (diffX > 0) {
                    previousFloor();
                } else {
                    nextFloor();
                }
            }
            return true;
        }
    });

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void nextFloor() {
        if (floorId >= floors) {
            return;
        }
        currentActivity.overridePendingTransition(R.anim.splashfadeout, R.anim.mainfadein);
        loadActivity(++floorId);
    }

    public void previousFloor() {
        if (floorId <= 1) {
            return;
        }
        currentActivity.overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
        loadActivity(--floorId);
    }

    public void loadActivity(final int id) {

                Intent intent = new Intent(context, RoomActivity.class);
                intent.putExtra("floorId", id);
                intent.putExtra("floors", floors);
                intent.putExtra("id", buildingId);
                context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(currentActivity).toBundle());

    }
}
