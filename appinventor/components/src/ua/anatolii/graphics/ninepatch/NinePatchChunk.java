package ua.anatolii.graphics.ninepatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Anatolii on 8/27/13.
 */
public class NinePatchChunk implements Externalizable {

	/**
	 * The 9 patch segment is not a solid color.
	 */
	public static final int NO_COLOR = 0x00000001;

	/**
	 * The 9 patch segment is completely transparent.
	 */
	public static final int TRANSPARENT_COLOR = 0x00000000;

	/**
	 * Default density for image loading from some InputStream
	 */
	public static final int DEFAULT_DENSITY = 160;

	/**
	 * By default it's true
	 */
	public boolean wasSerialized = true;

	/**
	 * Horizontal stretchable areas list.
	 */
	public ArrayList<Div> xDivs;

	/**
	 * Vertical stretchable areas list.
	 */
	public ArrayList<Div> yDivs;

	/**
	 * Content padding
	 */
	public Rect padding = new Rect();

	/**
	 * Colors array for chunks. If not sure what it is - fill it with NO_COLOR value. Or just use createColorsArray() method with current chunk instance.
	 */
	public int colors[];

	/**
	 * Creates new NinePatchChunk from byte array.
	 * Note! In order to avoid some Runtime issues, please, do this check before using this method: NinePatch.isNinePatchChunk(byte[] chunk).
	 *
	 * @param data array of chunk data
	 * @return parsed NinePatch chunk.
	 * @throws DivLengthException          if there's no horizontal or vertical stretchable area at all.
	 * @throws ChunkNotSerializedException if first bit is 0. I simply didn't face this case. If you will - feel free to contact me.
	 * @throws BufferUnderflowException    if the position of reading buffer is equal or greater than limit (data array length).
	 */
	public static NinePatchChunk parse(byte[] data) throws DivLengthException, ChunkNotSerializedException, BufferUnderflowException {
		ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());

		NinePatchChunk chunk = new NinePatchChunk();
		chunk.wasSerialized = byteBuffer.get() != 0;
		if (!chunk.wasSerialized)
			throw new ChunkNotSerializedException();//don't know how to handle

		byte divXCount = byteBuffer.get();
		checkDivCount(divXCount);
		byte divYCount = byteBuffer.get();
		checkDivCount(divYCount);

		chunk.colors = new int[byteBuffer.get()];

		// skip 8 bytes
		byteBuffer.getInt();//position = 4
		byteBuffer.getInt();//position = 8

		chunk.padding.left = byteBuffer.getInt();
		chunk.padding.right = byteBuffer.getInt();
		chunk.padding.top = byteBuffer.getInt();
		chunk.padding.bottom = byteBuffer.getInt();

		// skip 4 bytes
		byteBuffer.getInt();//position = 28

		int xDivs = divXCount >> 1;
		chunk.xDivs = new ArrayList<Div>(xDivs);
		readDivs(xDivs, byteBuffer, chunk.xDivs);

		int yDivs = divYCount >> 1;
		chunk.yDivs = new ArrayList<Div>(yDivs);
		readDivs(yDivs, byteBuffer, chunk.yDivs);

		for (int i = 0; i < chunk.colors.length; i++)
			chunk.colors[i] = byteBuffer.getInt();

		return chunk;
	}

	/**
	 * Creates NinePatchDrawable right from raw Bitmap object. So resulting drawable will have width and height 2 pixels less if it is raw, not compiled 9-patch resource.
	 *
	 * @param context
	 * @param bitmap  The bitmap describing the patches. Can be loaded from application resources
	 * @param srcName The name of the source for the bitmap. Might be null.
	 * @return new NinePatchDrawable object or null if bitmap parameter is null.
	 */
	public static NinePatchDrawable create9PatchDrawable(Context context, Bitmap bitmap, String srcName) {
		return BitmapType.getNinePatchDrawable(context.getResources(), bitmap, srcName);
	}

	/**
	 * Creates NinePatchDrawable from inputStream.
	 *
	 * @param context
	 * @param inputStream The input stream that holds the raw data to be decoded into a bitmap. Uses <code>DEFAULT_DENSITY</code> value for image decoding.
	 * @param srcName     The name of the source for the bitmap. Might be null.
	 * @return NinePatchDrawable instance.
	 */
	public static NinePatchDrawable create9PatchDrawable(Context context, InputStream inputStream, String srcName) {
		return create9PatchDrawable(context, inputStream, DEFAULT_DENSITY, srcName);
	}

	/**
	 * Creates NinePatchDrawable from inputStream.
	 *
	 * @param context
	 * @param inputStream  The input stream that holds the raw data to be decoded into a bitmap.
	 * @param imageDensity density of the image if known in advance.
	 * @param srcName      new NinePatchDrawable object or null if bitmap parameter is null.
	 * @return
	 */
	public static NinePatchDrawable create9PatchDrawable(Context context, InputStream inputStream, int imageDensity, String srcName) {
		ImageLoadingResult loadingResult = createChunkFromRawBitmap(context, inputStream, imageDensity);
		return loadingResult.getNinePatchDrawable(context.getResources(), srcName);
	}


	/**
	 * * Creates NinePatchChunk instance from raw bitmap image. Method calls <code>isRawNinePatchBitmap</code>
	 * method to make sure the bitmap is valid.
	 *
	 * @param bitmap source image
	 * @return new instance of chunk or empty chunk if bitmap is null or some Exceptions happen.
	 */
	public static NinePatchChunk createChunkFromRawBitmap(Bitmap bitmap) {
		try {
			return createChunkFromRawBitmap(bitmap, true);
		} catch (RuntimeException e) {
			return createEmptyChunk();
		}
	}

	/**
	 * Creates chunk from bitmap loaded from input stream. Uses <code>DEFAULT_DENSITY</code> value for image decoding.
	 *
	 * @param context
	 * @param inputStream The input stream that holds the raw data to be decoded into a bitmap.
	 * @return loading result which contains chunk object and loaded bitmap. Note! Resulting bitmap can be not the same as the source bitmap.
	 */
	public static ImageLoadingResult createChunkFromRawBitmap(Context context, InputStream inputStream) {
		return createChunkFromRawBitmap(context, inputStream, DEFAULT_DENSITY);
	}

	/**
	 * Creates chunk from bitmap loaded from input stream.
	 *
	 * @param context
	 * @param inputStream  The input stream that holds the raw data to be decoded into a bitmap.
	 * @param imageDensity density of the image if known in advance.
	 * @return loading result which contains chunk object and loaded bitmap. Note! Resulting bitmap can be not the same as the source bitmap.
	 */
	public static ImageLoadingResult createChunkFromRawBitmap(Context context, InputStream inputStream, int imageDensity) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inDensity = imageDensity;
		opts.inTargetDensity = imageDensity;
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream, new Rect(), opts);
		return createChunkFromRawBitmap(context, bitmap);
	}

	/**
	 * Creates chunk from raw bitmap.
	 *
	 * @param context
	 * @param bitmap  source image.
	 * @return loading result which contains chunk object and loaded bitmap. Note! Resulting bitmap can be not the same as the source bitmap.
	 */
	public static ImageLoadingResult createChunkFromRawBitmap(Context context, Bitmap bitmap) {
		BitmapType type = BitmapType.determineBitmapType(bitmap);
		NinePatchChunk chunk = type.createChunk(bitmap);
		bitmap = type.modifyBitmap(context.getResources(), bitmap, chunk);
		return new ImageLoadingResult(bitmap, chunk);
	}

	/**
	 * Simply creates new empty NinePatchChunk object. You can use it to modify data as you want to.
	 *
	 * @return new NinePatchChunk instance.
	 */
	public static NinePatchChunk createEmptyChunk() {
		NinePatchChunk out = new NinePatchChunk();
		out.colors = new int[0];
		out.padding = new Rect();
		out.yDivs = new ArrayList<Div>();
		out.xDivs = new ArrayList<Div>();
		return out;
	}

	/**
	 * Serializes current chunk instance to byte array. This array will pass thia check: NinePatch.isNinePatchChunk(byte[] chunk)
	 *
	 * @return The 9-patch data chunk describing how the underlying bitmap is split apart and drawn.
	 */
	public byte[] toBytes() {
		int capacity = 4 + (7 * 4) + xDivs.size() * 2 * 4 + yDivs.size() * 2 * 4 + colors.length * 4;
		ByteBuffer byteBuffer = ByteBuffer.allocate(capacity).order(ByteOrder.nativeOrder());
		byteBuffer.put(Integer.valueOf(1).byteValue());
		byteBuffer.put(Integer.valueOf(xDivs.size() * 2).byteValue());
		byteBuffer.put(Integer.valueOf(yDivs.size() * 2).byteValue());
		byteBuffer.put(Integer.valueOf(colors.length).byteValue());
		//Skip
		byteBuffer.putInt(0);
		byteBuffer.putInt(0);

		if (padding == null)
			padding = new Rect();
		byteBuffer.putInt(padding.left);
		byteBuffer.putInt(padding.right);
		byteBuffer.putInt(padding.top);
		byteBuffer.putInt(padding.bottom);

		//Skip
		byteBuffer.putInt(0);

		for (Div div : xDivs) {
			byteBuffer.putInt(div.start);
			byteBuffer.putInt(div.stop);
		}
		for (Div div : yDivs) {
			byteBuffer.putInt(div.start);
			byteBuffer.putInt(div.stop);
		}
		for (int color : colors)
			byteBuffer.putInt(color);


		return byteBuffer.array();

	}

	/**
	 * Util method. Creates new colors array filled with NO_COLOR value according to current divs state and sets it to the chunk.
	 *
	 * @param chunk        chunk instance which contains divs information.
	 * @param bitmapWidth  width of bitmap. Note! This value must be width without 9-patch borders. (2 pixels less then original 9.png image width)
	 * @param bitmapHeight height of bitmap. Note! This value must be height without 9-patch borders. (2 pixels less then original 9.png image height)
	 */
	public static void createColorsArrayAndSet(NinePatchChunk chunk, int bitmapWidth, int bitmapHeight) {
		int[] colorsArray = createColorsArray(chunk, bitmapWidth, bitmapHeight);
		if (chunk != null)
			chunk.colors = colorsArray;
	}

	/**
	 * Util method. Creates new colors array according to current divs state.
	 *
	 * @param chunk        chunk instance which contains divs information.
	 * @param bitmapWidth  width of bitmap. Note! This value must be width without 9-patch borders. (2 pixels less then original 9.png image width)
	 * @param bitmapHeight height of bitmap. Note! This value must be height without 9-patch borders. (2 pixels less then original 9.png image height)
	 * @return new properly sized array filled with NO_COLOR value.
	 */
	public static int[] createColorsArray(NinePatchChunk chunk, int bitmapWidth, int bitmapHeight) {
		if (chunk == null) return new int[0];
		ArrayList<Div> xRegions = getRegions(chunk.xDivs, bitmapWidth);
		ArrayList<Div> yRegions = getRegions(chunk.yDivs, bitmapHeight);
		int[] out = new int[xRegions.size() * yRegions.size()];
		Arrays.fill(out, NO_COLOR);
		return out;
	}

	/**
	 * Checks if bitmap is raw, not compiled 9-patch resource.
	 *
	 * @param bitmap source image
	 * @return true if so and false if not or bitmap is null.
	 */
	public static boolean isRawNinePatchBitmap(Bitmap bitmap) {
		if (bitmap == null) return false;
		if (bitmap.getWidth() < 3 || bitmap.getHeight() < 3)
			return false;
		if (!isCornerPixelsAreTrasperent(bitmap))
			return false;
		if (!hasNinePatchBorder(bitmap))
			return false;
		return true;
	}

	private static boolean hasNinePatchBorder(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int lastXPixel = width - 1;
		int lastYPixel = height - 1;
		for (int i = 1; i < lastXPixel; i++) {
			if (!isBorderPixel(bitmap.getPixel(i, 0)) || !isBorderPixel(bitmap.getPixel(i, lastYPixel)))
				return false;
		}
		for (int i = 1; i < lastYPixel; i++) {
			if (!isBorderPixel(bitmap.getPixel(0, i)) || !isBorderPixel(bitmap.getPixel(lastXPixel, i)))
				return false;
		}
		if (getXDivs(bitmap, 0).size() == 0)
			return false;
		if (getXDivs(bitmap, lastYPixel).size() > 1)
			return false;
		if (getYDivs(bitmap, 0).size() == 0)
			return false;
		if (getYDivs(bitmap, lastXPixel).size() > 1)
			return false;
		return true;
	}

	private static boolean isBorderPixel(int tmpPixel1) {
		return isTransparent(tmpPixel1) || isBlack(tmpPixel1);
	}

	private static boolean isCornerPixelsAreTrasperent(Bitmap bitmap) {
		int lastYPixel = bitmap.getHeight() - 1;
		int lastXPixel = bitmap.getWidth() - 1;
		return isTransparent(bitmap.getPixel(0, 0))
				&& isTransparent(bitmap.getPixel(0, lastYPixel))
				&& isTransparent(bitmap.getPixel(lastXPixel, 0))
				&& isTransparent(bitmap.getPixel(lastXPixel, lastYPixel));
	}

	private static boolean isTransparent(int color) {
		return Color.alpha(color) == Color.TRANSPARENT;
	}

	private static boolean isBlack(int pixel) {
		return pixel == Color.BLACK;
	}

	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		int length = input.readInt();
		byte[] bytes = new byte[length];
		input.read(bytes);
		try {
			NinePatchChunk patch = parse(bytes);
			this.wasSerialized = patch.wasSerialized;
			this.xDivs = patch.xDivs;
			this.yDivs = patch.yDivs;
			this.padding = patch.padding;
			this.colors = patch.colors;
		} catch (DivLengthException e) {
			//ignore
		} catch (ChunkNotSerializedException e) {
			//ignore
		}
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		byte[] bytes = toBytes();
		output.writeInt(bytes.length);
		output.write(bytes);
	}

	protected static NinePatchChunk createChunkFromRawBitmap(Bitmap bitmap, boolean checkBitmap) throws WrongPaddingException, DivLengthException {
		if (checkBitmap && !isRawNinePatchBitmap(bitmap)) {
			return createEmptyChunk();
		}
		NinePatchChunk out = new NinePatchChunk();
		setupStretchableRegions(bitmap, out);
		setupPadding(bitmap, out);

		setupColors(bitmap, out);
		return out;
	}

	private static void readDivs(int divs, ByteBuffer byteBuffer, ArrayList<Div> divArrayList) {
		for (int i = 0; i < divs; i++) {
			Div div = new Div();
			div.start = byteBuffer.getInt();
			div.stop = byteBuffer.getInt();
			divArrayList.add(div);
		}
	}

	private static void checkDivCount(byte divCount) throws DivLengthException {
		if (divCount == 0 || ((divCount & 1) != 0)) {
			throw new DivLengthException("Div count should be aliquot 2 and more then 0, but was: " + divCount);
		}
	}

	private static void setupColors(Bitmap bitmap, NinePatchChunk out) {
		int bitmapWidth = bitmap.getWidth() - 2;
		int bitmapHeight = bitmap.getHeight() - 2;
		ArrayList<Div> xRegions = getRegions(out.xDivs, bitmapWidth);
		ArrayList<Div> yRegions = getRegions(out.yDivs, bitmapHeight);
		out.colors = new int[xRegions.size() * yRegions.size()];

		int colorIndex = 0;
		for (Div yDiv : yRegions) {
			for (Div xDiv : xRegions) {
				int startX = xDiv.start + 1;
				int startY = yDiv.start + 1;
				if (hasSameColor(bitmap, startX, xDiv.stop + 1, startY, yDiv.stop + 1)) {
					int pixel = bitmap.getPixel(startX, startY);
					if (isTransparent(pixel))
						pixel = TRANSPARENT_COLOR;
					out.colors[colorIndex] = pixel;
				} else {
					out.colors[colorIndex] = NO_COLOR;
				}
				colorIndex++;
			}
		}
	}

	private static boolean hasSameColor(Bitmap bitmap, int startX, int stopX, int startY, int stopY) {
		int color = bitmap.getPixel(startX, startY);
		for (int x = startX; x <= stopX; x++) {
			for (int y = startY; y <= stopY; y++) {
				if (color != bitmap.getPixel(x, y))
					return false;
			}
		}
		return true;
	}

	private static void setupPadding(Bitmap bitmap, NinePatchChunk out) throws WrongPaddingException {
		int maxXPixels = bitmap.getWidth() - 2;
		int maxYPixels = bitmap.getHeight() - 2;
		ArrayList<Div> xPaddings = getXDivs(bitmap, bitmap.getHeight() - 1);
		if (xPaddings.size() > 1)
			throw new WrongPaddingException("Raw padding is wrong. Should be only one horizontal padding region");
		ArrayList<Div> yPaddings = getYDivs(bitmap, bitmap.getWidth() - 1);
		if (yPaddings.size() > 1)
			throw new WrongPaddingException("Column padding is wrong. Should be only one vertical padding region");
		if (xPaddings.size() == 0) xPaddings.add(out.xDivs.get(0));
		if (yPaddings.size() == 0) yPaddings.add(out.yDivs.get(0));
		out.padding = new Rect();
		out.padding.left = xPaddings.get(0).start;
		out.padding.right = maxXPixels - xPaddings.get(0).stop;
		out.padding.top = yPaddings.get(0).start;
		out.padding.bottom = maxYPixels - yPaddings.get(0).stop;
	}

	private static void setupStretchableRegions(Bitmap bitmap, NinePatchChunk out) throws DivLengthException {
		out.xDivs = getXDivs(bitmap, 0);
		if (out.xDivs.size() == 0)
			throw new DivLengthException("must be at least one horizontal stretchable region");
		out.yDivs = getYDivs(bitmap, 0);
		if (out.yDivs.size() == 0)
			throw new DivLengthException("must be at least one vertical stretchable region");
	}

	private static ArrayList<Div> getRegions(ArrayList<Div> divs, int max) {
		ArrayList<Div> out = new ArrayList<Div>();
		if (divs == null || divs.size() == 0) return out;
		for (int i = 0; i < divs.size(); i++) {
			Div div = divs.get(i);
			if (i == 0 && div.start != 0) {
				out.add(new Div(0, div.start - 1));
			}
			if (i > 0) {
				out.add(new Div(divs.get(i - 1).stop, div.start - 1));
			}
			out.add(new Div(div.start, div.stop - 1));
			if (i == divs.size() - 1 && div.stop < max) {
				out.add(new Div(div.stop, max - 1));
			}
		}
		return out;
	}

	private static ArrayList<Div> getYDivs(Bitmap bitmap, int column) {
		ArrayList<Div> yDivs = new ArrayList<Div>();
		Div tmpDiv = null;
		for (int i = 1; i < bitmap.getHeight(); i++) {
			tmpDiv = processChunk(bitmap.getPixel(column, i), tmpDiv, i - 1, yDivs);
		}
		return yDivs;
	}

	private static ArrayList<Div> getXDivs(Bitmap bitmap, int raw) {
		ArrayList<Div> xDivs = new ArrayList<Div>();
		Div tmpDiv = null;
		for (int i = 1; i < bitmap.getWidth(); i++) {
			tmpDiv = processChunk(bitmap.getPixel(i, raw), tmpDiv, i - 1, xDivs);
		}
		return xDivs;
	}

	private static Div processChunk(int pixel, Div tmpDiv, int position, ArrayList<Div> divs) {
		if (isBlack(pixel)) {
			if (tmpDiv == null) {
				tmpDiv = new Div();
				tmpDiv.start = position;
			}
		}
		if (isTransparent(pixel)) {
			if (tmpDiv != null) {
				tmpDiv.stop = position;
				divs.add(tmpDiv);
				tmpDiv = null;
			}
		}
		return tmpDiv;
	}
}
