package com.google.appinventor.components.runtime;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ChartView<C extends Chart, D extends ChartData> {
    protected C chart;
    protected D data;

    protected Handler uiHandler = new Handler();

    // Used to store a single Runnable to refresh the Chart.
    // The AtomicReference acts as an accumulator in throttling the
    // number of refreshes to limit the refresh rate to a single refreesh
    // per certain time frame.
    private AtomicReference<Runnable> refreshRunnable = new AtomicReference<Runnable>();

    /**
     * Returns the underlying view holding all the necessary Chart Views.
     * The reason this does not return the Chart view straight away is
     * due to some Charts having more than one view (e.g. Pie Chart
     * with rings)
     * @return  Chart view
     */
    public abstract View getView();

    /**
     * Sets the background color of the Chart.
     * @param argb  background color
     */
    public void setBackgroundColor(int argb) {
        chart.setBackgroundColor(argb);
    }

    /**
     * Sets the description text of the Chart.
     * @param text  description text
     */
    public void setDescription(String text) {
        chart.getDescription().setText(text);
    }

    public void setLegendEnabled(boolean enabled) {
        chart.getLegend().setEnabled(enabled);
    }

    public void setGridEnabled(boolean enabled) {
        chart.getXAxis().setDrawGridLines(false);
    }

    /**
     * Creates a new Chart Model object instance.
     *
     * @return  Chart Model instance
     */
    public abstract ChartDataModel createChartModel();

    /**
     * Sets the necessary default settings for the Chart view.
     */
    protected void initializeDefaultSettings() {
        // Center the Legend
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chart.getLegend().setWordWrapEnabled(true); // Wrap Legend entries in case of many entries
    }

    /**
     * Refreshes the Chart View to react to styling changes.
     */
    public void Refresh() {
        chart.invalidate();

        /* BELOW COMMENTS CONTAIN PREVIOUS CHART REFRESHING SOLUTION;
         * The currently chosen solution re-sets the values of the Data Sets
         * on the UI thread on each Refresh; Previously, the Refresh method
         * used NotifyDataSetChanged, which caused issues when refreshing went
         * by a bit too quick (and multiple Data Series were in place); The
         * older Refresh solution used a complementing getRefreshRunnable method.
         * Currently, this method is utilized to only be used whenever the Chart
         * View itself is refreshed on changing styling properties (e.g. color) */

        // Currently, if data is changed far too fast and too many Refresh calls are invoked,
        // exceptions related to the library will be thrown (ArrayIndexOutOfBoundsExceptions)
        // Since these exceptions are beyond our control, some measures are needed to control
        // the crashes (although a solution to wholly avoid them has not been found).
        // It is also worth to note that the MPAndroidChart (in v3.1.0) is not a thread-safe
        // library, but otherwise it seems to work quite well in async.
        // The mentioned issues occur even with just a single Data Set attached.
        // With regards to the issue, approaches that do not fully work (still prone to exceptions):
        // * Using an AsyncTask queue and executing the next one right after the other
        // * Switching ExecutorService with a HandlerThread + Handler (same behavior)
        // * Using a CountdownLatch to wait for invalidate to be over (+ HardwareAcceleration disabled)
        // * Switching off hardware acceleration for the Chart (no effect)
        // * Synchronizing the Data object
        // * Adding delays (far less exceptions however) <------ CURRENT SOLUTION
        // * Moving all the lines to run purely on UI with no delays (sometimes makes it worse)
        // * Using runOnUIThread instead of Handler
        // * Using volatile variables for datasets/data/chart
        // * Deferring Refresh calls to happen at the end of ExecutorService queue
        // * Using DelayQueue to throttle refreshes (alternate solution)
        // * Not using a Handler (simply using postInvalidate)
        // * Posting runnables from Chart itself
        // * Additional thread sleeping in-between Chart refreshes
        // * Scheduling ChartData tasks with delay
        // * Using a single ScheduledExecutorService (with delays/DelayQueue/current setup) for all Data Series
        //   for refreshing
        // * Using RateLimiter with 10 permits (1 per 100ms) to throttle refresh rate
        // * Using scheduled interval executor + queue to accumulate executable tasks per 100ms, execute them
        //   in sequence and then refresh the Chart only once
        // * Using FutureTasks, Hardware Acceleration off (to make invalidate non-async) and run on UI thread
        //   and FutureTask.get() to wait for refreshing to finish
        // * Using FutureTasks and cancelling current Refresh tasks, and starting another one (+ CountDownLatch
        //   approach combined with FutureTask cancelling)
        // * Waiting for Refresh Tasks to finish via posted FutureTasks on the UI Handler
        // The chosen solution is to then have delays and refresh throttling.


        // An AtomicReference is used to hold a single Refresh runnable, which is then
        // executed in a UIHandler. Since the AtomicReference holds a single Runnable
        // instance, it also acts as an accumulator in the case of too many refresh
        // calls being invoked within a time frame of 100ms.
//        refreshRunnable.set(getRefreshRunnable());

        // Post a Refresh runnable on the UI Thread (via the UI Handler),
        // since refreshing should only be invoked in the UI thread (due
        // to accessing views). A delay of 100ms is used to throttle the
        // refresh rate to prevent crashes.
//        uiHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // Get the runnable from the AtomicReference
//                Runnable runnable = refreshRunnable.getAndSet(null);
//
//                // Runnable non-null; Execute it
//                if (runnable != null) {
//                    runnable.run();
//                }
//            }
//        }, 100);

        // Alternate solution (very similar results):
        //        if (!delayQueue.isEmpty()) {
        //            return;
        //        }
        //
        //        final long startTime = System.currentTimeMillis() + 100;
        //
        //        Delayed delay = new Delayed() {
        //            @Override
        //            public long getDelay(@NonNull TimeUnit unit) {
        //                return unit.convert(startTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        //            }
        //
        //            @Override
        //            public int compareTo(@NonNull Delayed o) {
        //                return 0;
        //            }
        //        };
        //
        //        delayQueue.put(delay);
        //
        //        try {
        //            delayQueue.take();
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        //
        //        chart.getData().notifyDataChanged();
        //        chart.notifyDataSetChanged();
        //        chart.postInvalidate();
    }

    /**
     * Updates the specified Chart Data Model and refreshes the
     * Chart.
     * @param model  Chart Data Model to update & refresh
     */
    public void Refresh(final ChartDataModel model) {
        // Create a new RefreshTask with the model's current List of Entries
        RefreshTask refreshTask = new RefreshTask(model.getEntries());

        // Execute the RefreshTask with the ChartDataModel argument
        refreshTask.execute(model);
    }

    /**
     * AsyncTask used to refresh the Chart View with new data on the UI thread.
     * Used as a measure to prevent crashes and exceptions by taking in a constant
     * copy of the data, and re-setting it to the currently refreshed Chart Data
     * Model, while also updating the Chart itself and invalidating the View.
     */
    private class RefreshTask extends AsyncTask<ChartDataModel, Void, ChartDataModel> {

        // Local copy of latest Chart Entries
        private List<Entry> mEntries;

        public RefreshTask(List<Entry> entries) {
            // Create a copy of the passed in Entries List.
            mEntries = new ArrayList<Entry>(entries);
        }

        @Override
        protected ChartDataModel doInBackground(ChartDataModel... chartDataModels) {
            // All the work should be done on the UI thread; Simply pass the first
            // passed in Chart Data Model (expect non-null, non-empty var args)
            return chartDataModels[0];
        }

        @Override
        protected void onPostExecute(ChartDataModel result) {
            // Refresh the Chart and the Data Model with the
            // local Entries List copy. This is done on the UI
            // thread to avoid exceptions (onPostExecute runs
            // on the UI)
            Refresh(result, mEntries);
        }
    }

    /**
     * Sets the specified List of Entries to the specified Chart Data
     * Model and refreshes the local Chart View.
     *
     * To be used after updating a ChartDataModel's entries to display
     * the changes on the Chart itself.
     *
     * Values are overwritten with the specified List of entries.
     *
     * @param model  Chart Data Model to update
     * @param entries  List of entries to set to the Chart Data Model
     */
    protected void Refresh(ChartDataModel model, List<Entry> entries) {
        // Set the specified Entries to the Data Set. This is used to
        // prevent exceptions on quick data changing operations (so that
        // the invalidation/refreshing can keep up and inconsistent states
        // would not be caused by asynchronous operations)
        model.getDataset().setValues(entries);

        // Notify the Data component of data changes (needs to be called
        // when Datasets get changed directly)
        chart.getData().notifyDataChanged();

        // Notify the Chart of Data changes (needs to be called
        // when Data objects get changed directly)
        chart.notifyDataSetChanged();

        // Invalidate the Chart view for the changes to take
        // effect.
        chart.invalidate();
    }

    //    protected Runnable getRefreshRunnable() {
//        return new Runnable() {
//            @Override
//            public void run() {
//                // Notify the Data component of data changes (needs to be called
//                // when Datasets get changed directly)
//                chart.getData().notifyDataChanged();
//
//                // Notify the Chart of Data changes (needs to be called
//                // when Data objects get changed directly)
//                chart.notifyDataSetChanged();
//
//                // Invalidate the Chart view for the changes to take
//                // effect. NOTE: Most exceptions with regards to data
//                // changing too fast occur as a result of calling the
//                // invalidate method.
//                chart.invalidate();
//            }
//        };
//    }
}
