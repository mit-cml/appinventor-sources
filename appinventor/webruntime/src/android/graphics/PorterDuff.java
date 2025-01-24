package android.graphics;

public class PorterDuff {
  public enum Mode {
    // these value must match their native equivalents. See SkXfermode.h
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_CLEAR.png" />
     *     <figcaption>Destination pixels covered by the source are cleared to 0.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = 0\)</p>
     * <p>\(C_{out} = 0\)</p>
     */
    CLEAR       (0),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_SRC.png" />
     *     <figcaption>The source pixels replace the destination pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src}\)</p>
     * <p>\(C_{out} = C_{src}\)</p>
     */
    SRC         (1),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_DST.png" />
     *     <figcaption>The source pixels are discarded, leaving the destination intact.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{dst}\)</p>
     * <p>\(C_{out} = C_{dst}\)</p>
     */
    DST         (2),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_SRC_OVER.png" />
     *     <figcaption>The source pixels are drawn over the destination pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src} + (1 - \alpha_{src}) * \alpha_{dst}\)</p>
     * <p>\(C_{out} = C_{src} + (1 - \alpha_{src}) * C_{dst}\)</p>
     */
    SRC_OVER    (3),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_DST_OVER.png" />
     *     <figcaption>The source pixels are drawn behind the destination pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{dst} + (1 - \alpha_{dst}) * \alpha_{src}\)</p>
     * <p>\(C_{out} = C_{dst} + (1 - \alpha_{dst}) * C_{src}\)</p>
     */
    DST_OVER    (4),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_SRC_IN.png" />
     *     <figcaption>Keeps the source pixels that cover the destination pixels,
     *     discards the remaining source and destination pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src} * \alpha_{dst}\)</p>
     * <p>\(C_{out} = C_{src} * \alpha_{dst}\)</p>
     */
    SRC_IN      (5),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_DST_IN.png" />
     *     <figcaption>Keeps the destination pixels that cover source pixels,
     *     discards the remaining source and destination pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src} * \alpha_{dst}\)</p>
     * <p>\(C_{out} = C_{dst} * \alpha_{src}\)</p>
     */
    DST_IN      (6),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_SRC_OUT.png" />
     *     <figcaption>Keeps the source pixels that do not cover destination pixels.
     *     Discards source pixels that cover destination pixels. Discards all
     *     destination pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = (1 - \alpha_{dst}) * \alpha_{src}\)</p>
     * <p>\(C_{out} = (1 - \alpha_{dst}) * C_{src}\)</p>
     */
    SRC_OUT     (7),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_DST_OUT.png" />
     *     <figcaption>Keeps the destination pixels that are not covered by source pixels.
     *     Discards destination pixels that are covered by source pixels. Discards all
     *     source pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = (1 - \alpha_{src}) * \alpha_{dst}\)</p>
     * <p>\(C_{out} = (1 - \alpha_{src}) * C_{dst}\)</p>
     */
    DST_OUT     (8),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_SRC_ATOP.png" />
     *     <figcaption>Discards the source pixels that do not cover destination pixels.
     *     Draws remaining source pixels over destination pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{dst}\)</p>
     * <p>\(C_{out} = \alpha_{dst} * C_{src} + (1 - \alpha_{src}) * C_{dst}\)</p>
     */
    SRC_ATOP    (9),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_DST_ATOP.png" />
     *     <figcaption>Discards the destination pixels that are not covered by source pixels.
     *     Draws remaining destination pixels over source pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src}\)</p>
     * <p>\(C_{out} = \alpha_{src} * C_{dst} + (1 - \alpha_{dst}) * C_{src}\)</p>
     */
    DST_ATOP    (10),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_XOR.png" />
     *     <figcaption>Discards the source and destination pixels where source pixels
     *     cover destination pixels. Draws remaining source pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = (1 - \alpha_{dst}) * \alpha_{src} + (1 - \alpha_{src}) * \alpha_{dst}\)</p>
     * <p>\(C_{out} = (1 - \alpha_{dst}) * C_{src} + (1 - \alpha_{src}) * C_{dst}\)</p>
     */
    XOR         (11),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_DARKEN.png" />
     *     <figcaption>Retains the smallest component of the source and
     *     destination pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src} + \alpha_{dst} - \alpha_{src} * \alpha_{dst}\)</p>
     * <p>\(C_{out} = (1 - \alpha_{dst}) * C_{src} + (1 - \alpha_{src}) * C_{dst} + min(C_{src}, C_{dst})\)</p>
     */
    DARKEN      (16),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_LIGHTEN.png" />
     *     <figcaption>Retains the largest component of the source and
     *     destination pixel.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src} + \alpha_{dst} - \alpha_{src} * \alpha_{dst}\)</p>
     * <p>\(C_{out} = (1 - \alpha_{dst}) * C_{src} + (1 - \alpha_{src}) * C_{dst} + max(C_{src}, C_{dst})\)</p>
     */
    LIGHTEN     (17),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_MULTIPLY.png" />
     *     <figcaption>Multiplies the source and destination pixels.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src} * \alpha_{dst}\)</p>
     * <p>\(C_{out} = C_{src} * C_{dst}\)</p>
     */
    MULTIPLY    (13),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_SCREEN.png" />
     *     <figcaption>Adds the source and destination pixels, then subtracts the
     *     source pixels multiplied by the destination.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src} + \alpha_{dst} - \alpha_{src} * \alpha_{dst}\)</p>
     * <p>\(C_{out} = C_{src} + C_{dst} - C_{src} * C_{dst}\)</p>
     */
    SCREEN      (14),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_ADD.png" />
     *     <figcaption>Adds the source pixels to the destination pixels and saturates
     *     the result.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = max(0, min(\alpha_{src} + \alpha_{dst}, 1))\)</p>
     * <p>\(C_{out} = max(0, min(C_{src} + C_{dst}, 1))\)</p>
     */
    ADD         (12),
    /**
     * <p>
     *     <img src="{@docRoot}reference/android/images/graphics/composite_OVERLAY.png" />
     *     <figcaption>Multiplies or screens the source and destination depending on the
     *     destination color.</figcaption>
     * </p>
     * <p>\(\alpha_{out} = \alpha_{src} + \alpha_{dst} - \alpha_{src} * \alpha_{dst}\)</p>
     * <p>\(\begin{equation}
     * C_{out} = \begin{cases} 2 * C_{src} * C_{dst} & 2 * C_{dst} \lt \alpha_{dst} \\
     * \alpha_{src} * \alpha_{dst} - 2 (\alpha_{dst} - C_{src}) (\alpha_{src} - C_{dst}) & otherwise \end{cases}
     * \end{equation}\)</p>
     */
    OVERLAY     (15);

    Mode(int nativeInt) {
      this.nativeInt = nativeInt;
    }

    /**
     * @hide
     */
    public final int nativeInt;
  }
}
