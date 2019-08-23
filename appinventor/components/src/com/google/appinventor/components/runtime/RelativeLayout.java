package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.util.ViewUtil;

import android.content.Context;
import android.view.ViewGroup;
import android.os.Handler;
import android.view.View;
import android.util.Log;

/**
 * Relative Layout for placing components at a specified position inside the 
 * layout. Closely follows the implementation of LinearLayout with certain 
 * changes.
 */
@SimpleObject
public class RelativeLayout implements Layout {
	
	private final android.widget.RelativeLayout layoutManager;
	private final Handler handler;
	
	/**
	 * Creates a new relative layout.
	 * 
	 * @param context view context
	 */
	RelativeLayout(Context context) {
		this(context, null, null);
	}
	
	/**
	 * Creates a new relative layout with a preferred empty width/height.
	 * 
	 * @param context view context
	 * @param preferredEmptyWidth the preferred width of an empty layout
	 * @param preferredEmptyHeight the preferred height of an empty layout
	 */
	RelativeLayout(Context context, final Integer preferredEmptyWidth, 
			final Integer preferredEmptyHeight) {
		if (preferredEmptyWidth == null && preferredEmptyHeight != null ||
		    preferredEmptyWidth != null && preferredEmptyHeight == null) {
		    throw new IllegalArgumentException("RelativeLayout - preferredEmptyWidth and " +
		    		"preferredEmptyHeight must be either both null or both not null");
		}
		
		handler = new Handler();
		
		// Create an Android RelativeLayout, but override onMeasure so that we can use our preferred
	    // empty width/height.
	    layoutManager = new android.widget.RelativeLayout(context) {
	        @Override
	        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        	// If there was no preferred empty width/height specified (see constructors above), just
	        	// call super. (This is the case for the Form component.)
	        	if (preferredEmptyWidth == null || preferredEmptyHeight == null) {
	        		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	        		return;
	        	}

	        	// If the layout has any children, just call super.
	        	if (getChildCount() != 0) {
	        		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	        		return;
	        	}

	        	setMeasuredDimension(getSize(widthMeasureSpec, preferredEmptyWidth),
	        						getSize(heightMeasureSpec, preferredEmptyHeight));
	        }

	        private int getSize(int measureSpec, int preferredSize) {
	        	int result;
	        	int specMode = MeasureSpec.getMode(measureSpec);
	        	int specSize = MeasureSpec.getSize(measureSpec);

	        	if (specMode == MeasureSpec.EXACTLY) {
	        		// We were told how big to be
	        		result = specSize;
	        	} else {
	        		// Use the preferred size.
	        		result = preferredSize;
	        		if (specMode == MeasureSpec.AT_MOST) {
	        			// Respect AT_MOST value if that was what is called for by measureSpec
	        			result = Math.min(result, specSize);
	        		}
	        	}

	        	return result;
	        }
	    };
	}
	
	/**
	 * Returns the width.
	 * 
	 * @return width
	 */
	public int getWidth() {
		return layoutManager.getWidth();
	}
	
	/**
	 * Returns the height.
	 * 
	 * @return height
	 */
	public int getHeight() {
		return layoutManager.getHeight();
	}
	
	@Override
	public ViewGroup getLayoutManager() {
		return layoutManager;
	}

	@Override
	public void add(AndroidViewComponent component) {
		// We cannot add component to layoutManager just yet because component 
		// does not have its own x and y coordinates yet.
		component.getView().setLayoutParams(
				new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
															   ViewGroup.LayoutParams.WRAP_CONTENT));
		addComponentLater(component);
	}
	
	/**
	 * Causes addComponent to be called later.
	 * 
	 * @param component component to be added later
	 */
	private void addComponentLater(final AndroidViewComponent component) {
		handler.post(new Runnable() {
		    public void run() {
		        addComponent(component);
		    }
		});
	}
	
	private void addComponent(final AndroidViewComponent component) {
		int x = component.XCoord();
		int y = component.YCoord();
		if (x == ComponentConstants.DEFAULT_X_Y || 
			y == ComponentConstants.DEFAULT_X_Y) {
			addComponentLater(component);
		} else {
			if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
				ViewGroup.LayoutParams params = component.getView().getLayoutParams();
				android.widget.RelativeLayout.LayoutParams newParams = 
						new android.widget.RelativeLayout.LayoutParams(params.width, params.height);
				newParams.topMargin = ViewUtil.calculatePixels(component.getView(), y);
				newParams.leftMargin = ViewUtil.calculatePixels(component.getView(), x);
				layoutManager.addView(component.getView(), newParams);
			} else {
				Log.e("RelativeLayout", "Child has illegal x or y position.");
			}
		}
	}
	
}