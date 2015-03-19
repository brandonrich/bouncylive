/**
 * Copyright CMW Mobile.com, 2011.
 */
package com.cmwmobile.android.samples;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;

import com.infinitedonuts.bouncylive.R;

/**
 * The GalleryDialogPreference class is a DialogPreference based and provides a
 * gallery preference.
 * @author Casper Wakkers
 */
public class GalleryDialogPreference extends DialogPreference {
	// Layout widgets.
	private Gallery gallery = null;
	private ImageView imageView = null;
	private String selectedPaletteID;

	// Array with icons.
	private int[] iconIds = {};
	
	private String[] paletteIDs;

	/**
	 * The GalleryDialogPreference constructor.
	 * @param context of this preference.
	 * @param attrs custom xml attributes.
	 */
	public GalleryDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
			
		
		 
		Resources res = context.getResources();
		paletteIDs = res.getStringArray(R.array.paletteIDs);
		iconIds = new int[paletteIDs.length];
		String packageName = context.getPackageName();
		for( int i = 0; i < paletteIDs.length; i++ ) {
			iconIds[i] = res.getIdentifier("p" + paletteIDs[i], "drawable", packageName );
		}
	}
	/**
	 * Method geBitmap retrieves the requested bitmap.
	 * @param index of the bitmap to be retrieved.
	 * @return Bitmap representing the bitmap.
	 */
	private Bitmap getBitmap(int index) {
		return BitmapFactory.decodeResource(
			getContext().getResources(), iconIds[index]);
	}
	/**
	 * {@inheritDoc}
	 */
	protected View onCreateDialogView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());

		View view = layoutInflater.inflate(
			R.layout.gallerypreference_layout, null);

		int defaultIndex = getPersistedInt(0);
		//int defaultIndex = 0;

		gallery = (Gallery)view.findViewById(R.id.gallery);

		gallery.setAdapter(new ImageAdapter(getContext(), iconIds));
		gallery.setSelection(defaultIndex);
		gallery.setOnItemClickListener(new OnItemClickListener() {
			/**
			 * {@inheritDoc}
			 */
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				imageView.setImageBitmap(getBitmap(position));
				selectedPaletteID = paletteIDs[position];
			}
		});

		imageView = (ImageView)view.findViewById(R.id.galleryImage);

		imageView.setImageBitmap(getBitmap(defaultIndex));

		return view;
	}
	/**
	 * {@inheritDoc}
	 */
	public void onClick(DialogInterface dialog, int which) {
		// if the positive button is clicked, we persist the value.
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if (shouldPersist()) {

				int pos = gallery.getSelectedItemPosition();
				persistInt(pos);
			}
		}

		super.onClick(dialog, which);
	}
}
