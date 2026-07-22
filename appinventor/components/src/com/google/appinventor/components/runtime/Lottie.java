package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Context;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.airbnb.lottie.*;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.animation.Animator;
import android.content.Context;
import android.view.View;
import com.airbnb.lottie.LottieAnimationView;
import java.io.InputStream;
import java.io.IOException;

@DesignerComponent(
        version = 1,
        description = "A component to use Lottie animations in your apps",
        category = ComponentCategory.ANIMATION,
        nonVisible = true,
        iconName = "images/lottie.png")

@SimpleObject(external = true)
@UsesLibraries(libraries = "lottie.jar")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.READ_EXTERNAL_STORAGE")

public class LottieAnimation extends AndroidNonvisibleComponent {

    //Activity and Context
    private Context context;
    private Activity activity;
    private FrameLayout lottieanimview;
    private LottieAnimationView lottieAnimationView;

    public LottieAnimation(ComponentContainer container){
        super(container.$form());
        this.activity = container.$context();
        this.context = container.$context();
    }

    @SimpleFunction(description = "Initializes Lottie")
    public void Initialize(AndroidViewComponent lottieView, boolean loopEnabled, float speed){
        FrameLayout lottieAnimView = (FrameLayout) lottieView.getView();
        LottieAnimationView lottieAnimationView = new LottieAnimationView(context);
        lottie.loop(loopEnabled);
        lottie.setSpeed(speed);

        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
                AnimationCancelled();
            }

            public void onAnimationEnded(Animator animator) {
                AnimationEnded();
            }

            public void onAnimationRepeated(Animator animator) {
                AnimationRepeated();
            }

            public void onAnimationStarted(Animator animator) {
                AnimationStarted();
            }
        });

        lottie.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AnimationClicked();
            }
        });
            lottieAnimView.addView(lottieAnimationView, 0, new LayoutParams(-1, -1)); 
    }
    

    @SimpleEvent()
    public void AnimationCancelled(){
        EventDispatcher.dispatchEvent(this, "AnimationCancelled");
    }
    @SimpleEvent()
    public void AnimationEnded(){
        EventDispatcher.dispatchEvent(this, "AnimationEnded");
    }
    @SimpleEvent()
    public void AnimationRepeated(){
        EventDispatcher.dispatchEvent(this, "AnimationRepeated");
    }
    @SimpleEvent()
    public void AnimationStarted(){
        EventDispatcher.dispatchEvent(this, "AnimationStarted");
    }
    @SimpleEvent()
    public void AnimationClicked(){
        EventDispatcher.dispatchEvent(this, "AnimationClicked");
    }
    @SimpleFunction
    public void PlayAnimation(){
    lottie.playAnimation();
    }
     @DesignerProperty(defaultValue = "FILE URL/NAME HERE", editorType = "string")
    @SimpleProperty(description = "Sets the Json file source")
    public final void Source(String fileUrl) {
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            lottie.setAnimationFromUrl(file);
        } else {
            try {
            InputStream inputStream = context.getAssets().open(fileUrl);
            byte[] lottieJson = new byte[inputStream.available()];
            inputStream.read(lottieJson);
            inputStream.close();
            String json = new String(lottieJson, "UTF-8");
                lottie.setAnimationFromJson(json);
        } catch (IOException e) {
        }
    }
    }
    @SimpleFunction
    public void CancelAnimation() {
        lottie.cancelAnimation();
    }
    @SimpleFunction
    public void PauseAnimation() {
        lottie.pauseAnimation();
    }

    @SimpleFunction
    public void ResumeAnimation() {
        lottie.resumeAnimation();
    }
    @SimpleProperty(description = "sets start frame of an animation")
    public final void StartFrame(int startFrame) {
        lottie.setMinFrame(startFrame);
    }
}
