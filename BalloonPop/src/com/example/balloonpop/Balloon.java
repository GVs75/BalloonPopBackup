package com.example.balloonpop;

import java.util.Random;
import android.graphics.Bitmap;

public class Balloon extends AnimatedBitmap {

	public static final int D_BALLOON_ID = 1;
	private int id;
	private Bitmap balloons;
	private Bitmap balloon 	= null;
	private int IMG_PIXELS;
	private int disp_height, disp_width;
	
	private float 	x, y;
	private double 	angle_step;
	private float 	angle;
	private int 	speed_y;
	private int 	angle_max;
	
	private float 	balloon_scale;
	
	public Balloon(Bitmap bmp, int display_height, int display_width) {
		balloons 	= bmp;
		
		IMG_PIXELS 	= balloons.getWidth()/4;		
		
		Random r 		= new Random();
		
		// Initializing Fields/
		disp_height 	= display_height;
		disp_width  	= display_width;		
		
		//Get random Balloon from Balloons bitmap matrix
		id 				= r.nextInt(16);
					
		// Initializing x, y position
		x 				= r.nextInt(disp_width - IMG_PIXELS);
		y 				= disp_height;
		
		// Initializing scale
		balloon_scale 	= ((float)r.nextInt(6))/6f + 0.4f;
		
		// Initializing speed
		speed_y 		= (int)(balloon_scale * 10);
		
		// Initializing rotation
		angle_step 		= ((double)r.nextInt(628))/100;
		angle_max 		= r.nextInt(60);
		
		// Initializing the Bitmap
		int bx = (id % 4)*IMG_PIXELS;
		int by = (id / 4)*IMG_PIXELS;
		
		balloon = Bitmap.createBitmap(balloons, bx, by, IMG_PIXELS, IMG_PIXELS, null, false);
		
		setBitmap(D_BALLOON_ID, balloon, true);
	}
	
	public void addWind(float w){
		
		x += w*balloon_scale;
		if (x < 0)
			x = 0;
		if (x > disp_width - IMG_PIXELS)
			x = disp_width - IMG_PIXELS;
	}
	
	@Override
	public void update() {
		
		//Updating position
		y = y - speed_y;
		if (y < 0 - balloon.getHeight())
		{	stop();
			return;
		}
		
		setPosition(x,y);

		angle = (float)Math.sin(angle_step)*angle_max;
		angle_step += 0.05;
		setRotateCenter(angle);
		
		setScale(balloon_scale, balloon_scale);
		
		super.update();
	}
}
