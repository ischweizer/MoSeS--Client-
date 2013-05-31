package de.da_sense.moses.client.abstraction.apks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * TODO Class Comment
 *
 */
public class ImageAdapter extends BaseAdapter {
	private Context mContext;

	private Integer[] mImageIds;
	private String[] mAlternateText;

	/**
	 * 
	 * @param c The context
	 * @param i Integer[] of the Image Ids
	 * @param a String[] of the Alternate Text
	 */
	public ImageAdapter(Context c, Integer[] i, String[] a) {
		mContext = c;
		mImageIds = i;
		mAlternateText = a;
	}

	@Override
	public int getCount() {
		return mImageIds.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = new ImageView(mContext);
		imageView.setImageResource(mImageIds[position]);
		imageView.setContentDescription(mAlternateText[position]);
		return imageView;
	}
}