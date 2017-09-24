package ua.anatolii.graphics.ninepatch;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

import java.util.ArrayList;

/**
 * Created by Anatolii on 11/2/13.
 */
public enum BitmapType {
	NinePatch {
		@Override
		public NinePatchChunk createChunk(Bitmap bitmap) {
			return NinePatchChunk.parse(bitmap.getNinePatchChunk());
		}
	}, RawNinePatch {
		@Override
		protected NinePatchChunk createChunk(Bitmap bitmap) {
			try {
				return NinePatchChunk.createChunkFromRawBitmap(bitmap, false);
			} catch (WrongPaddingException e) {
				return NinePatchChunk.createEmptyChunk();
			} catch (DivLengthException e) {
				return NinePatchChunk.createEmptyChunk();
			}
		}

		@Override
		protected Bitmap modifyBitmap(Resources resources, Bitmap bitmap, NinePatchChunk chunk) {
			Bitmap content = Bitmap.createBitmap(bitmap, 1, 1, bitmap.getWidth() - 2, bitmap.getHeight() - 2);
			int targetDensity = resources.getDisplayMetrics().densityDpi;
			float densityChange = (float) targetDensity / bitmap.getDensity();
			if (densityChange != 1f) {
				int dstWidth = Math.round(content.getWidth() * densityChange);
				int dstHeight = Math.round(content.getHeight() * densityChange);
				content = Bitmap.createScaledBitmap(content, dstWidth, dstHeight, true);
				content.setDensity(targetDensity);
				chunk.padding = new Rect(Math.round(chunk.padding.left * densityChange),
						Math.round(chunk.padding.top * densityChange),
						Math.round(chunk.padding.right * densityChange),
						Math.round(chunk.padding.bottom * densityChange));

				recalculateDivs(densityChange, chunk.xDivs);
				recalculateDivs(densityChange, chunk.yDivs);
			}
			bitmap = content;
			return content;
		}

		private void recalculateDivs(float densityChange, ArrayList<Div> divs) {
			for (Div div : divs) {
				div.start = Math.round(div.start * densityChange);
				div.stop = Math.round(div.stop * densityChange);
			}
		}
	}, PlainImage {
		@Override
		protected NinePatchChunk createChunk(Bitmap bitmap) {
			return NinePatchChunk.createEmptyChunk();
		}
	}, NULL {
		@Override
		protected NinePatchDrawable createNinePatchDrawable(Resources resources, Bitmap bitmap, String srcName) {
			return null;
		}
	};

	/**
	 * Depending on bitmap will return chunk which satisfies it.
	 *
	 * @param bitmap source image
	 * @return chunk instance. Or EmptyChunk. Can't be null.
	 */
	protected NinePatchChunk createChunk(Bitmap bitmap) {
		return NinePatchChunk.createEmptyChunk();
	}

	/**
	 * Modifies source bitmap so it fits NinePatchDrawable requirements. Can change provided chunk.
	 *
	 * @param resources uses to get some information about system, get access to resources cache.
	 * @param bitmap    source bitmap
	 * @param chunk     chunk instance which was created using this bitmap.
	 * @return modified bitmap or the same bitmap.
	 */
	protected Bitmap modifyBitmap(Resources resources, Bitmap bitmap, NinePatchChunk chunk) {
		return bitmap;
	}

	/**
	 * Detects which type of bitmap source instance belongs.
	 *
	 * @param bitmap source image.
	 * @return detected type of source image.
	 */
	public static BitmapType determineBitmapType(Bitmap bitmap) {
		if (bitmap == null) return NULL;
		byte[] ninePatchChunk = bitmap.getNinePatchChunk();
		if (ninePatchChunk != null && android.graphics.NinePatch.isNinePatchChunk(ninePatchChunk))
			return NinePatch;
		if (NinePatchChunk.isRawNinePatchBitmap(bitmap))
			return RawNinePatch;
		return PlainImage;
	}

	/**
	 * Creates NinePatchDrawable instance from given bitmap.
	 * @param resources
	 * @param bitmap source bitmap.
	 * @param srcName The name of the source for the bitmap. Might be null.
	 * @return not null NinePatchDrawable instance.
	 */
	public static NinePatchDrawable getNinePatchDrawable(Resources resources, Bitmap bitmap, String srcName) {
		return determineBitmapType(bitmap).createNinePatchDrawable(resources, bitmap, srcName);
	}

	protected NinePatchDrawable createNinePatchDrawable(Resources resources, Bitmap bitmap, String srcName) {
		NinePatchChunk chunk = createChunk(bitmap);
		return new NinePatchDrawable(resources, modifyBitmap(resources, bitmap, chunk), chunk.toBytes(), chunk.padding, srcName);
	}
}
