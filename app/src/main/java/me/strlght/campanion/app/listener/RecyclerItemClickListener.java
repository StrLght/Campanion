package me.strlght.campanion.app.listener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by grigoriy on 23.10.2014.
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

	private OnItemClickListener mListener;
	private GestureDetector mGestureDetector;

	public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
		mListener = listener;
		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return true;
			}

		});
	}

	@Override
	public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
		View v = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
		if (v != null && mListener != null && mGestureDetector.onTouchEvent(motionEvent)) {
			mListener.onItemClick(v, recyclerView.getChildPosition(v));
		}
		return false;
	}

	@Override
	public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

	}

	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

}
