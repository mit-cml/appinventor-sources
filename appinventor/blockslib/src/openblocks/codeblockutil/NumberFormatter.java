package openblocks.codeblockutil;

/**
 * Class for displaying a number based on a specified
 * level of precision and other display parameters.
 */
public class NumberFormatter {
	/** Specifies the "amount of precision" to use. */
	public interface PrecisionSpecifier {
		/**
		 * Returns the number of significant digits
		 * to display given the precision of a
		 * particular number.  Given a non-zero
		 * number x,
		 * significantDigits(Math.log10(Math.abs(x)))
		 * digits should be displayed.
		 */
		public int significantDigits(int precision);
		/**
		 * Returns the precision below which
		 * to display a number with e notation.
		 */
		public int showAsDecimalThreshold();
	}
	
	private static class StandardPrecisionSpecifier implements PrecisionSpecifier {
		private final int lowPrecisionThreshold;
		private final int highPrecisionThreshold;
		private final int desiredDigits;
		
		/**
		 * Constructs a precision specifier that
		 * shows the specified number of digits if
		 * and only if Math.abs(precision) <=
		 * desiredDigits || precision >
		 * highPrecisionThreshold || precision <
		 * lowPrecisionThreshold and shows the number
		 * without e notation if and only if the
		 * precision lies between
		 * lowPrecisionThreshold and
		 * highPrecisionThreshold.
		 */
		public StandardPrecisionSpecifier(int lowPrecisionThreshold, int highPrecisionThreshold, int desiredDigits) throws IllegalArgumentException {
			if (lowPrecisionThreshold > 0)
				throw new IllegalArgumentException("Low precision threshold cannot be positive");
			if (highPrecisionThreshold < 0)
				throw new IllegalArgumentException("High precision threshold cannot be negaitive");
			this.lowPrecisionThreshold = lowPrecisionThreshold;
			this.highPrecisionThreshold = highPrecisionThreshold;
			this.desiredDigits = desiredDigits;
		}

		public int significantDigits(int precision) {
			if (Math.abs(precision) <= desiredDigits || precision > highPrecisionThreshold || precision < lowPrecisionThreshold)
				return desiredDigits;
			else
				return precision;
		}

		public int showAsDecimalThreshold() {
			return lowPrecisionThreshold;
		}
	}
	
	public static final StandardPrecisionSpecifier LOW_PRECISION = new StandardPrecisionSpecifier(-2, 4, 3);
	public static final StandardPrecisionSpecifier MEDIUM_PRECISION = new StandardPrecisionSpecifier(-5, 7, 4);
	public static final StandardPrecisionSpecifier HIGH_PRECISION = new StandardPrecisionSpecifier(-5, 7, 6);
	public static final StandardPrecisionSpecifier VERY_LOW_PRECISION = new StandardPrecisionSpecifier(-6, 8, 8);
	
	private PrecisionSpecifier ps;
	private String eCharacter = "e";
	private boolean showZeroBeforeDecimal = true;
	private boolean showPlusInExponent = true;
	private boolean showExtraZeros = false;
	
	public NumberFormatter() {
		ps = MEDIUM_PRECISION;
	}
	
	public NumberFormatter(PrecisionSpecifier ps) {
		this.ps = ps;
	}
	
	/**
	 * @param showPlusInExponent Whether to show the '+' character when displaying a number with e notation (e.g. 1.5e+12)
	 */
	public NumberFormatter(PrecisionSpecifier ps, char eCharacter, boolean showZeroBeforeDecimal, boolean showPlusInExponent, boolean showExtraZeros) {
		this(ps, "" + eCharacter, showZeroBeforeDecimal, showPlusInExponent, showExtraZeros);
	}
	
	/**
	 * @param showPlusInExponent Whether to show the '+' character when displaying a number with e notation (e.g. 1.5e+12)
	 */
	public NumberFormatter(PrecisionSpecifier ps, String eCharacter, boolean showZeroBeforeDecimal, boolean showPlusInExponent, boolean showExtraZeros) {
		this.ps = ps;
		this.eCharacter = eCharacter;
		this.showZeroBeforeDecimal = showZeroBeforeDecimal;
		this.showPlusInExponent = showPlusInExponent;
		this.showExtraZeros = showExtraZeros;
	}
	
	public String format(double x) {
		if (x == 0.0)
			return "0";
		
		StringBuffer buf = new StringBuffer();
		if (x < 0.0) {
			buf.append('-');
			x = -x;
		}
		
		int p = (int)Math.floor(Math.log10(x));
		int d = Math.max(1, Math.min(14, ps.significantDigits(p)));
		long a = (long)(x * Math.pow(10.0, -p + d - 1));
		
		if (p < 0) {
			if (p >= ps.showAsDecimalThreshold()) {
				if (!showExtraZeros) {
					while (a % 10l == 0l) {
						a /= 10l;
						d--;
					}
				}
				String sigFigs = Long.toString(a);
				
				if (showZeroBeforeDecimal)
					buf.append('0');
				buf.append('.');
				for(int i = -1; i > p; i--)
					buf.append('0');
				buf.append(sigFigs);
				
				return buf.toString();
			 }
		}
		else if (d >= p + 1) {
			if (!showExtraZeros) {
				while (p + 1 < d && a % 10l == 0l) {
					a /= 10l;
					d--;
				}
			}
			String sigFigs = Long.toString(a);
			
			buf.append(sigFigs.substring(0, p + 1));
			if (p + 1 != d) {
				buf.append('.');
				buf.append(sigFigs.substring(p + 1, sigFigs.length()));
			}
			
			return buf.toString();
		}
		
		//e notation
		if (!showExtraZeros) {
			while (a % 10l == 0l) {
				a /= 10l;
				d--;
			}
		}
		String sigFigs = Long.toString(a);
		
		buf.append(sigFigs.charAt(0));
		buf.append('.');
		buf.append(sigFigs.substring(1, sigFigs.length()));
		buf.append(eCharacter);
		if (p > 0 && showPlusInExponent)
			buf.append("+");
		buf.append(Integer.toString(p));
		return buf.toString();
	}
}
