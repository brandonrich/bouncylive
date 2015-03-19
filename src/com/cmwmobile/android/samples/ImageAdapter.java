/**
 * Copyright CMW Mobile.com, 2011.
 */
package com.cmwmobile.android.samples;

import com.infinitedonuts.bouncylive.R;
import com.infinitedonuts.bouncylive.R.styleable;

import android.content.Context;
import android.content.res.TypedArray;

import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * The ImageAdapter class is responsible for the layout of a single gallery
 * item. 
 * @author Casper Wakkers
 */
public class ImageAdapter extends BaseAdapter {
	private Context context = null;

	// Background theme resourceId.
	private int backgroundResourceId = -1;

	// Array with icons.
	private int[] iconIds = null;

	/**
	 * ImageAdapter default constructor. Retrieves the background
	 * resourceId.
	 * @param context used to initialize this adapter.
	 * @param ids array with icon id's.
	 */
	public ImageAdapter(Context context, int[] ids) {
		this.context = context;
		this.iconIds = ids;

		TypedArray typedArray = context.obtainStyledAttributes(
			R.styleable.GalleryTheme);

		backgroundResourceId = typedArray.getResourceId(
			R.styleable.GalleryTheme_android_galleryItemBackground, 0);

		typedArray.recycle();
	}
	/**
	 * {@inheritDoc}
	 */
	public int getCount() {
		return iconIds.length;
	}
	/**
	 * {@inheritDoc}
	 */
	public Object getItem(int position) {
		return position;
	}
	/**
	 * {@inheritDoc}
	 */
	public long getItemId(int position) {
		return position;
	}
	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View view, ViewGroup viewGroup) {
		ImageView imageView = new ImageView(context);

		imageView.setBackgroundResource(backgroundResourceId);
		imageView.setImageResource(iconIds[position]);
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);

		return imageView;
	}
}
