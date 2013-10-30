package com.example.balloonpop;

//GameView class

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback{

	private static final int D_INITIAL_BALLONS = 5;
	private SurfaceHolder 	mHolder;
	private GameLoopThread mGameLoopThread;

	private Bitmap mBallons;
	private Bitmap mBitmap_Star;
	private Group  mGroup;
	
	private Shader mShader;
	private Paint  mPaintShader;
	
	private int 	mScore = 0;
	private Paint	 mPaintText;
	
	//private String info = "";
	private float[] acc = new float[3];
	
	private SoundPool soundPool;
	private int soundID;	

	public GameView(Context context) {
		super(context);

		//mGameLoopThread = new mGameLoopThread(this);
		mHolder = getHolder();
		mHolder.addCallback(this);

		mBitmap_Star = BitmapFactory.decodeResource(context.getResources(), R.drawable.star);
		mBallons = BitmapFactory.decodeResource(context.getResources(), R.drawable.balloons);

		mGroup = new Group();
		mPaintText = new Paint();
		mPaintText.setColor(Color.WHITE);
		mPaintText.setTextSize(32);
				
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundID = soundPool.load(context, R.raw.waterballoon, 1);
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder mHolder) {
		boolean retry = true;
		mGameLoopThread.setRunning(false);
		while (retry) {
			try {
				mGameLoopThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder mHolder) {	
		mGameLoopThread = new GameLoopThread(this);
		mGameLoopThread.setRunning(true);
		mGameLoopThread.start();
		
		mShader = new LinearGradient(0, 0, 0, getHeight(), Color.rgb(0, 0, 64), Color.BLACK, TileMode.CLAMP);
		mPaintShader = new Paint(); 
		mPaintShader.setShader(mShader); 
		
		initGroup();
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder mHolder, int format, int width, int height) {
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (getHolder()) {
						
			if( event.getActionMasked() == MotionEvent.ACTION_DOWN || 
				event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
					
					//int touchCounter = event.getPointerCount(); 
					//for (int pointerIndex = 0; pointerIndex < touchCounter; pointerIndex++ ){
						//float x = event.getX(pointerIndex);
						//float y = event.getY(pointerIndex);
						
						float x = event.getX(event.getActionIndex());
						float y = event.getY(event.getActionIndex());
						//Removing Balloon if clicked
						int index = mGroup.getClickedIndex(x, y);
						if (index != -1){
							mGroup.remove(index);
							mScore++;
						}else {
							//Adding Balloon if clicking outside
							MainActivity main = (MainActivity)getContext();
							if (main.isConnected() == false) {
								addBalloon();
							} else {
								main.sendMessage(MainActivity.D_ADD_BALLON);
							}

							//Adding Star if clicking outside
							Star star = new Star(mBitmap_Star, x, y);
							mGroup.add(star);
						}
					//}
				
			}
			
			if (mGroup.size() == 0)
				initGroup();
		}
		//return super.onTouchEvent(event);
		return true;
	}
	
	private void initGroup() {
		//Initial mBallons
		Balloon[] balloon = new Balloon[D_INITIAL_BALLONS];
		for (int i = 0; i < D_INITIAL_BALLONS; i++){
			balloon[i] = new Balloon(mBallons, getHeight(), getWidth());
			mGroup.add(balloon[i]);
		}
	}
	
	public void addBalloon(){
		Balloon balloon = new Balloon(mBallons, getHeight(), getWidth());
		mGroup.add(balloon);	
	}
	
	public void myDraw(Canvas canvas) {
		
		
		
		//Commit: Adding gradient to BackGround
		//canvas.drawColor(Color.BLACK);											//<-- Commented
		canvas.drawRect(new RectF(0, 0, getWidth(), getHeight()), mPaintShader); 	//<-- Added
				
		canvas.drawText("Score: "+mScore, 30, 30, mPaintText);
		mGroup.drawAll(canvas);
		
		//info = String.format(Locale.US, "Acc: X=%.2f, Y=%.2f, Z=%.2f", acc[0], acc[1], acc[2]);
		//canvas.drawText(info, 80, 80, mPaintText);
	}
	
	public void setAcceleration(float[] values){
		acc[0] = values[0];
		acc[1] = values[1];
		acc[2] = values[2];
		
		for (int i = 0; i < mGroup.size(); i++){
			if (mGroup.get(i).getIdentification() == Balloon.D_BALLOON_ID) {
				((Balloon)mGroup.get(i)).addWind(acc[0]);
			}
		}
	}
}
