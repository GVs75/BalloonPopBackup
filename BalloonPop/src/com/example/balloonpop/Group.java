package com.example.balloonpop;

import java.util.ArrayList;

import android.graphics.Canvas;

public class Group extends AnimatedBitmap {
	private ArrayList<AnimatedBitmap> children = new ArrayList<AnimatedBitmap>();
	
	public void drawAll(Canvas canvas) {
		int i = 0;
		int size = children.size();
		while (i < size){	
			if (children.get(i).draw(canvas) == true)
				i++;
			else
			{	
				children.remove(i);
				size = children.size();
			}
		}
	}
	

	public int getClickedIndex(float ax, float ay) {
		
		for (int i = children.size() - 1; i >= 0 ; i--){
			if (children.get(i).isClicked(ax, ay) == true)
				return i;
		}
		return -1;
	}

	public void add(int location, AnimatedBitmap object) {
		children.add(location, object);
	}

	public boolean add(AnimatedBitmap object) {
		return children.add(object);
	}

	public void clear() {
		children.clear();
	}

	public AnimatedBitmap get(int location) {
		return children.get(location);
	}

	public AnimatedBitmap remove(int location) {
		return children.remove(location);
	}
	
	public boolean remove(Object object) {
		return children.remove(object);
	}

	public int size() {
		return children.size();
	}	
}
