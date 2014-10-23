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
	private GestureDetector mTapDetector;
	private GestureDetector mDoubleTapDetector;

	public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
		mListener = listener;
		mTapDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return true;
			}

		});
		mDoubleTapDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				return true;
			}

		});
	}

	@Override
	public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
		View v = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
		if (v != null && mListener != null) {
			if (mDoubleTapDetector.onTouchEvent(motionEvent)) {
				mListener.onDoubleItemClick(v, recyclerView.getChildPosition(v));
			} else if (mTapDetector.onTouchEvent(motionEvent)) {
				mListener.onItemClick(v, recyclerView.getChildPosition(v));
			}
		}
		return false;
	}

	@Override
	public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

	}

	public interface OnItemClickListener {
		public void onItemClick(View view, int position);

		public void onDoubleItemClick(View view, int position);
	}

}
