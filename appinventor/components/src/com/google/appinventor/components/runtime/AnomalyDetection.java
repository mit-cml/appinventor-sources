package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.YailList;
import gnu.lists.LList;

import java.util.ArrayList;
import java.util.List;

/**
 * A data science component to apply different anomaly detection models
 * The component needs a data source to apply the model on
 */
@DesignerComponent(version = YaVersion.ANOMALY_COMPONENT_VERSION,
        description = "A component that contains anomaly detection models",
        category = ComponentCategory.DATASCIENCE,
        iconName = "images/web.png",
        nonVisible = true)
@SimpleObject
public class AnomalyDetection extends DataCollection {

    /**
     * Gets the line of best fit correlation coefficient
     *
     * @param dataList - x value of entry
     * @return List
     */
    @SimpleFunction(description = "Gets the line of best fit correlation coefficient.")
    public List DetectAnomalies(final YailList dataList) {
        ArrayList anomalies = new ArrayList<>();
        LList dataListValues = (LList) dataList.getCdr();
        List<Double> data = castToDouble(dataListValues);
        double threshold = 2.0;

        // Calculate mean and standard deviation
        double sum = data.stream().reduce((x, y) -> x + y).get();
        double mean = sum / data.size();
        double variance = data.stream().map(x -> Math.pow(x - mean, 2)).reduce((x, y) -> x + y).get() / data.size();
        double sd = Math.sqrt(variance);

        // Detect anomalies using Z-score
        for (int i = 0; i < data.size(); i++) {
            double zScore = Math.abs((data.get(i) - mean) / sd);
            if (zScore > threshold) {
                //System.out.println("Anomaly detected at index " + i + ": " + data.get(i));
                anomalies.add(i,data.get(i));
            }
        }
        return anomalies;
    }
    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return null;
    }
}
