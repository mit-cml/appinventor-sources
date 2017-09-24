package ua.anatolii.graphics.ninepatch;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

/**
 * Created by Anatolii on 11/2/13.
 */
public class ImageLoadingResult {

    /**
     * Loaded bitmap on which base chunk was created. Can be null.
     */
    public final Bitmap bitmap;

    /**
     * Chunk instance. Can be null.
     */
    public final NinePatchChunk chunk;

    protected ImageLoadingResult(Bitmap bitmap, NinePatchChunk chunk) {
        this.bitmap = bitmap;
        this.chunk = chunk;
    }

    /**
     * Method creates NinePatchDrawable according to the current object state.
     * @param resources uses to get some information about system, get access to resources cache.
     * @param strName if not null it will be cached with this name inside resource manager.
     * @return 9 patch drawable instance or null if bitmap was null.
     */
    public NinePatchDrawable getNinePatchDrawable(Resources resources, String strName){
        if(bitmap == null)
            return null;
        if(chunk == null)
            return new NinePatchDrawable(resources, bitmap, null, new Rect(), strName);
        return new NinePatchDrawable(resources, bitmap, chunk.toBytes(), chunk.padding, strName);
    }
}
