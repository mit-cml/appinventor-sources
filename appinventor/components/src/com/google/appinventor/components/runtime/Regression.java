package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;
import gnu.lists.LList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A machine learning component to apply different ML models
 * The component is attached directly to a Chart component by dragging it onto the Chart.
 */
@DesignerComponent(version = YaVersion.REGRESSION_COMPONENT_VERSION,
        description = "A component that contains machine learning models",
        category = ComponentCategory.DATASCIENCE,
        iconName = "images/web.png",
        nonVisible = true)
@SimpleObject
@SuppressWarnings("checkstyle:JavadocParagraph")
public final class Regression extends DataCollection {

    /**
     *
     * Creates a new Chart Data component.
     */
    public Regression(ComponentContainer container) {
        super();
        // Construct default dataFileColumns list with 2 entries
        dataFileColumns = Arrays.asList("", "");
        sheetsColumns = Arrays.asList("", "");
        webColumns = Arrays.asList("", ""); // Construct default webColumns list with 2 entries
    }

    /**
     * Creates a new Coordinate Data component.
     */
    public static List<Double> castToDouble(List list) {
        List<Double> listDoubles = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Number) {
                listDoubles.add(((Number) o).doubleValue());
            } else {
                try {
                    listDoubles.add(Double.parseDouble(o.toString()));
                } catch (NumberFormatException e) {
                    // Do nothing (value already false)
                }
            }
        }
        return listDoubles;
    }

    /**
     * Calculates the line of best fit
     *
     * @param xEntries - x value of entry
     * @param yEntries - y value of entry
     * @return list. 1st element of the list is the slope, 2nd element is the intercept, 3 element is the line of best fit prediction values
     */
    public static List ComputeLineOfBestFit(final YailList xEntries, final YailList yEntries) {
        LList xValues = (LList) xEntries.getCdr();
        List<Double> x = castToDouble(xValues);

        LList yValues = (LList) yEntries.getCdr();
        List<Double> y = castToDouble(yValues);

        if (xValues.size() != yValues.size())
            throw new IllegalStateException("Must have equal X and Y data points");

        int n = xValues.size();

        double sumx = 0.0, sumy = 0.0, sumXY = 0.0, squareSum_X = 0.0, squareSum_Y = 0.0;
        for (int i = 0; i < n; i++) {
            sumx += x.get(i);
            sumXY = sumXY + x.get(i) * y.get(i);
            sumy += y.get(i);
            squareSum_X = squareSum_X + x.get(i) * x.get(i);
            squareSum_Y = squareSum_Y + y.get(i) * y.get(i);
        }
        double xmean = sumx / n;
        double ymean = sumy / n;

        double xxmean = 0.0, xymean = 0.0;
        List predictions = new ArrayList();
        for (int i = 0; i < n; i++) {
            xxmean += (x.get(i) - xmean) * (x.get(i) - xmean);
            xymean += (x.get(i) - xmean) * (y.get(i) - ymean);
        }
        double slope = xymean / xxmean;
        double intercept = ymean - slope * xmean;
        // use formula for calculating correlation coefficient.
        double corr = (n * sumXY - sumx * sumy) / (Math.sqrt((n * squareSum_X - sumx * sumx) * (n * squareSum_Y - sumy * sumy)));

        for (int i = 0; i < n; i++) {
            double prediction = slope * x.get(i) + intercept;
            predictions.add(prediction);
        }
        // ToDo: return a user a dictionary and let the user extract the parameters they need themselves instead
        ArrayList result = new ArrayList<>();
        result.add(slope);
        result.add(intercept);
        result.add(corr);
        result.add(predictions);
        return result;
    }

    /**
     * Gets the line of best fit slope
     *
     * @param xList - x value of entry
     * @param yList - y value of entry
     * @return Double slope
     */
    @SimpleFunction(description = "Gets the line of best fit Slope.")
    public double GetLineOfBestFitSlope(final YailList xList, final YailList yList) {
        return (Double) ComputeLineOfBestFit(xList, yList).get(0);
    }

    /**
     * Gets the line of best fit Intercept
     *
     * @param xList - x value of entry
     * @param yList - y value of entry
     * @return Double intercept
     */
    @SimpleFunction(description = "Gets the line of best fit intercept.")
    public double GetLineOfBestFitYIntercept(final YailList xList, final YailList yList) {
        return (Double) ComputeLineOfBestFit(xList, yList).get(1);
    }

    /**
     * Gets the line of best fit correlation coefficient
     *
     * @param xList - x value of entry
     * @param yList - y value of entry
     * @return Double corr
     */
    @SimpleFunction(description = "Gets the line of best fit correlation coefficient.")
    public double GetLineOfBestFitCorCoef(final YailList xList, final YailList yList) {
        return (Double) ComputeLineOfBestFit(xList, yList).get(2);
    }

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return null;
    }
}


