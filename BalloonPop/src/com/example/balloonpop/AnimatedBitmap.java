package com.example.balloonpop;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class AnimatedBitmap {
	
	private boolean isRunning 		= false;
	private boolean isClickable		= false;
	private Bitmap 	img 			= null;
	private Paint 	paint;
	private int		identification	= -1;

	private Matrix 	matrix			= new Matrix();	
	private float	rotate_angle 	= 0;
	private float	rotate_x 		= 0;
	private float	rotate_y 		= 0;
	private float	translate_x 	= 0;
	private float	translate_y 	= 0;
	private float	alpha		 	= 1.0f;
	private float	scale_x 		= 1.0f;
	private float	scale_y 		= 1.0f;

	
	public void stop() {
		isRunning = false;
	}

	public boolean getStatus() {
		return isRunning;
	}

	public int getIdentification() {
		return identification;
	}
	
	public void setBitmap(int id, Bitmap bmp, boolean clickable) {
		img 	= bmp;
		paint	= new Paint();
		paint.setFilterBitmap(true);
		isRunning = true;
		isClickable = clickable;
		identification = id;
	}	
	
	public void setPosition(float x, float y){
		translate_x = x;
		translate_y = y;
	}
	
	public void setRotateCenter(float angle){
		rotate_angle 	= angle;
		rotate_x 		= img.getWidth()*scale_x/2;
		rotate_y 		= img.getHeight()*scale_y/2;
	}
	
	public void setRotate(float angle, float x, float y){
		rotate_angle 	= angle;
		rotate_x 		= x;
		rotate_y 		= y;
	}
	public void setAlpha(float a){	
		alpha = a;
	}
	
	public void setScale(float x, float y){
		scale_x 	= x;
		scale_y 	= y;
	}
	
	public void update() {
		paint.setAlpha((int)(alpha*255));
		matrix.setScale(scale_x, scale_y);
		matrix.postRotate(rotate_angle, rotate_x, rotate_y);
		matrix.postTranslate(translate_x, translate_y);
	}
	
	public boolean draw(Canvas canvas) {

		if (isRunning && img != null) {
			
			update();	//During update can be stopped
			
			if (isRunning){
				canvas.drawBitmap(img, matrix, paint);
				return true;
			}
		}
		return false;
	}
	
	public boolean isClicked(float ax, float ay) {
		
		if ( ax > translate_x &&
			 ax < translate_x + (img.getWidth()*scale_x) &&
			 ay > translate_y &&
			 ay < translate_y + (img.getHeight()*scale_y) &&
			 isClickable)
			return true;
		
		return false;
	}	
}
