package com.z.startwar;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.Toast;
import com.z.startwar.view.StarWarView;

import java.io.IOException;

public class MyActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    private static final float SCROLL_ANIM_DURATION = 30000;    // 30毫秒

    private StarWarView mStarWarView;
    private boolean mScrolling;
    private SeekBar mSeekBar;
    private ValueAnimator mScrollAnimator;
    private MediaPlayer mMediaPlayer;
    private boolean mIsPlay=false;
    Uri mUri;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mMediaPlayer = new MediaPlayer();

        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mStarWarView = (StarWarView) findViewById(R.id.starwar_view);
        mStarWarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mScrolling) {
                    animateScroll();
                    if (mUri == null) {
                        playMusic();
                    } else {
                        pauseMusic();
                    }
                } else {
                    pauseMusic();
                    stopScrollAnimation();
                }
            }
        });

        Toast.makeText(this, "点击一下开始或暂停",
                Toast.LENGTH_SHORT)
                .show();
    }

    //开始播放音乐
    private void playMusic() {
        mMediaPlayer.reset();
        mUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.nice);
        try {
            mMediaPlayer.setDataSource(this, mUri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mIsPlay=true;
        } catch (IOException e) {
            Toast.makeText(this, "播放器错误", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //暂停或继续播放音乐
    private void pauseMusic() {
        if (mIsPlay) {
            mIsPlay=false;
            mMediaPlayer.pause();
        } else {
            mIsPlay=true;
            mMediaPlayer.start();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mStarWarView.setScrollPosition(progress / 100000f);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mScrolling) {
            stopScrollAnimation();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    //播放字幕动画
    private void animateScroll() {
        mScrolling = true;
        mScrollAnimator = ObjectAnimator.ofInt(mSeekBar, "progress", mSeekBar.getProgress(), mSeekBar.getMax());
        mScrollAnimator.setDuration(
                (long) (SCROLL_ANIM_DURATION * (1 - (float) mSeekBar.getProgress() / mSeekBar.getMax())));
        mScrollAnimator.setInterpolator(new LinearInterpolator());
        mScrollAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mScrolling = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mScrollAnimator.start();
    }

    //暂停字幕动画
    private void stopScrollAnimation() {
        mScrolling=false;
        if (mScrollAnimator != null) {
            mScrollAnimator.cancel();
            mScrollAnimator = null;
        }
    }
}
