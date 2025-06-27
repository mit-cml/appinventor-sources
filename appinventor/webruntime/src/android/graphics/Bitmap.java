package android.graphics;

public class Bitmap {
  public enum Config {
    // these native values must match up with the enum in SkBitmap.h

    /**
     * Each pixel is stored as a single translucency (alpha) channel.
     * This is very useful to efficiently store masks for instance.
     * No color information is stored.
     * With this configuration, each pixel requires 1 byte of memory.
     */
    ALPHA_8(1),

    /**
     * Each pixel is stored on 2 bytes and only the RGB channels are
     * encoded: red is stored with 5 bits of precision (32 possible
     * values), green is stored with 6 bits of precision (64 possible
     * values) and blue is stored with 5 bits of precision.
     *
     * This configuration can produce slight visual artifacts depending
     * on the configuration of the source. For instance, without
     * dithering, the result might show a greenish tint. To get better
     * results dithering should be applied.
     *
     * This configuration may be useful when using opaque bitmaps
     * that do not require high color fidelity.
     *
     * <p>Use this formula to pack into 16 bits:</p>
     * <pre class="prettyprint">
     * short color = (R & 0x1f) << 11 | (G & 0x3f) << 5 | (B & 0x1f);
     * </pre>
     */
    RGB_565(3),

    /**
     * Each pixel is stored on 2 bytes. The three RGB color channels
     * and the alpha channel (translucency) are stored with a 4 bits
     * precision (16 possible values.)
     *
     * This configuration is mostly useful if the application needs
     * to store translucency information but also needs to save
     * memory.
     *
     * It is recommended to use {@link #ARGB_8888} instead of this
     * configuration.
     *
     * Note: as of {@link android.os.Build.VERSION_CODES#KITKAT},
     * any bitmap created with this configuration will be created
     * using {@link #ARGB_8888} instead.
     *
     * @deprecated Because of the poor quality of this configuration,
     *             it is advised to use {@link #ARGB_8888} instead.
     */
    @Deprecated
    ARGB_4444(4),

    /**
     * Each pixel is stored on 4 bytes. Each channel (RGB and alpha
     * for translucency) is stored with 8 bits of precision (256
     * possible values.)
     *
     * This configuration is very flexible and offers the best
     * quality. It should be used whenever possible.
     *
     * <p>Use this formula to pack into 32 bits:</p>
     * <pre class="prettyprint">
     * int color = (A & 0xff) << 24 | (B & 0xff) << 16 | (G & 0xff) << 8 | (R & 0xff);
     * </pre>
     */
    ARGB_8888(5),

    /**
     * Each pixel is stored on 8 bytes. Each channel (RGB and alpha
     * for translucency) is stored as a
     * {@link android.util.Half half-precision floating point value}.
     *
     * This configuration is particularly suited for wide-gamut and
     * HDR content.
     *
     * <p>Use this formula to pack into 64 bits:</p>
     * <pre class="prettyprint">
     * long color = (A & 0xffff) << 48 | (B & 0xffff) << 32 | (G & 0xffff) << 16 | (R & 0xffff);
     * </pre>
     */
    RGBA_F16(6),

    /**
     * Special configuration, when bitmap is stored only in graphic memory.
     * Bitmaps in this configuration are always immutable.
     *
     * It is optimal for cases, when the only operation with the bitmap is to draw it on a
     * screen.
     */
    HARDWARE(7),

    /**
     * Each pixel is stored on 4 bytes. Each RGB channel is stored with 10 bits of precision
     * (1024 possible values). There is an additional alpha channel that is stored with 2 bits
     * of precision (4 possible values).
     *
     * This configuration is suited for wide-gamut and HDR content which does not require alpha
     * blending, such that the memory cost is the same as ARGB_8888 while enabling higher color
     * precision.
     *
     * <p>Use this formula to pack into 32 bits:</p>
     * <pre class="prettyprint">
     * int color = (A & 0x3) << 30 | (B & 0x3ff) << 20 | (G & 0x3ff) << 10 | (R & 0x3ff);
     * </pre>
     */
    RGBA_1010102(8);

    final int nativeInt;

    private static Config sConfigs[] = {
        null, ALPHA_8, null, RGB_565, ARGB_4444, ARGB_8888, RGBA_F16, HARDWARE, RGBA_1010102
    };

    Config(int ni) {
      this.nativeInt = ni;
    }

    static Config nativeToConfig(int ni) {
      return sConfigs[ni];
    }
  }

  public enum CompressFormat {
    /**
     * Compress to the JPEG format. {@code quality} of {@code 0} means
     * compress for the smallest size. {@code 100} means compress for max
     * visual quality.
     */
    JPEG          (0),
    /**
     * Compress to the PNG format. PNG is lossless, so {@code quality} is
     * ignored.
     */
    PNG           (1),
    /**
     * Compress to the WEBP format. {@code quality} of {@code 0} means
     * compress for the smallest size. {@code 100} means compress for max
     * visual quality. As of {@link android.os.Build.VERSION_CODES#Q}, a
     * value of {@code 100} results in a file in the lossless WEBP format.
     * Otherwise the file will be in the lossy WEBP format.
     *
     * @deprecated in favor of the more explicit
     *             {@link CompressFormat#WEBP_LOSSY} and
     *             {@link CompressFormat#WEBP_LOSSLESS}.
     */
    @Deprecated
    WEBP          (2),
    /**
     * Compress to the WEBP lossy format. {@code quality} of {@code 0} means
     * compress for the smallest size. {@code 100} means compress for max
     * visual quality.
     */
    WEBP_LOSSY    (3),
    /**
     * Compress to the WEBP lossless format. {@code quality} refers to how
     * much effort to put into compression. A value of {@code 0} means to
     * compress quickly, resulting in a relatively large file size.
     * {@code 100} means to spend more time compressing, resulting in a
     * smaller file.
     */
    WEBP_LOSSLESS (4);

    CompressFormat(int nativeInt) {
      this.nativeInt = nativeInt;
    }
    final int nativeInt;
  }

  public static Bitmap createBitmap(int width, int height, Config config) {
    return null;
  }

  public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
    return null;
  }

  public Bitmap compress(CompressFormat format, int quality, java.io.OutputStream stream) {
    return null;
  }

  public int getWidth() {
    return 0;
  }

  public int getHeight() {
    return 0;
  }
}
