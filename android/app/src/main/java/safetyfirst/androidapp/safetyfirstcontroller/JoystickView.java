package safetyfirst.androidapp.safetyfirstcontroller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    //Variables for the construction of the circles
    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    private JoystickListener joystickCallback;
    private final int ratio = 5;
    private long currentTime = System.currentTimeMillis();
    private long previousTime;
    private final int TIME_DELAY = 100;

    //The size of the circles on the UI
    private void setupDimensions(){
        centerX = getWidth()/2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight())/3;
        hatRadius = Math.min(getWidth(), getHeight())/5;
    }

    //The constructors for the the object
    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }
    //Second constructor
    public JoystickView(Context context, AttributeSet attributes, int style){
        super(context, attributes, style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }
    //Third constructor
    public JoystickView(Context context, AttributeSet attributes){
        super(context, attributes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    //Drawing the actual joystick for the UI
    private void drawJoystick(float newX, float newY){
        if(getHolder().getSurface().isValid()){
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            colors.setARGB(255, 50, 50,50);
            myCanvas.drawCircle(centerX, centerY, baseRadius, colors);
            colors.setARGB(255,0,0,255);
            myCanvas.drawCircle(newX, newY,hatRadius, colors);
            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder){
        setupDimensions();
        drawJoystick(centerX, centerY);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){

    }

    //When there is movement in the area of the joystick then this boolean method will run
    //Identifying what kind of movement is occurring
    public boolean onTouch(View v, MotionEvent e) {
        previousTime = System.currentTimeMillis();
        if(v.equals(this)){
            if(e.getAction() != e.ACTION_UP) {
                float displacement = (float) Math.sqrt((Math.pow(e.getX()-centerX,2))+Math.pow(e.getY()-centerY,2));
                if(displacement < baseRadius) {
                    drawJoystick(e.getX(), e.getY());
                    if (previousTime - currentTime > TIME_DELAY) { //Check if set time has passed since the last onJoystickMoved call to prevent mqtt flooding
                        currentTime = previousTime;
                        joystickCallback.onJoystickMoved((e.getX() - centerX) / baseRadius, (e.getY() - centerY) / baseRadius, getId());
                    }
                }else{
                    float ratio = baseRadius / displacement;
                    float constrainedX = centerX + (e.getX()-centerX)*ratio;
                    float constrainedY = centerY + (e.getY()-centerY)*ratio;
                    drawJoystick(constrainedX,constrainedY);
                    if (previousTime - currentTime > 100) { //Check if set time has passed since the last onJoystickMoved call to prevent mqtt flooding
                        currentTime = previousTime;
                        joystickCallback.onJoystickMoved(((constrainedX-centerX)/baseRadius),((constrainedY-centerY)/baseRadius),getId());
                    }
                }
            }else {
                //If there is no interaction with the joystick area then it will relocate to the center
                //and make the car stop
                drawJoystick(centerX, centerY);
                joystickCallback.onJoystickMoved(0, 0, getId());
            }
        }
        return true;
    }

    //It will analyze the coordinates of where the joystick has been moved and carry these attributes
    //and send them to the onJoystickMoved method
    public interface JoystickListener{
        void onJoystickMoved(float xPercent, float yPercent, int id);
    }
}


