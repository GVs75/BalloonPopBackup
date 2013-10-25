package com.example.balloonpop;

import android.graphics.Bitmap;

public class Star extends AnimatedBitmap {

	public static final int D_STAR_ID = 2;
	private Bitmap 	star 			= null;
	private float 	star_scale  	= 1f;
	private float 	star_speed  	= 0.1f;
	private float 	max_star_scale  = 1.3f;
	private float	x, y;
	private float	delta_x, delta_y;
	
	public Star(Bitmap bmp, float x, float y) {
		star = bmp;
		setBitmap(D_STAR_ID, star, false);
		//setPosition(x - star.getWidth()/2, y - star.getHeight()/2);
		
		this.x = x;
		this.y = y;		
		delta_x = star.getWidth()/2;
		delta_y = star.getHeight()/2;

	}
	
	@Override
	public void update() {

		if (star_scale <= 0){
			stop();
			return;
		}
	
		star_scale += star_speed;
		if (star_scale >= max_star_scale)
			star_speed = -star_speed;
		
		setPosition(x - delta_x*star_scale, y - delta_y*star_scale);
		setScale(star_scale,star_scale);
		//setAlpha(star_scale);

		super.update();
	}
}
