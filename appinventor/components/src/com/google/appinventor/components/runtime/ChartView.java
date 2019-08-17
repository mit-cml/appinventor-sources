package com.google.appinventor.components.runtime;

import android.os.Handler;
import android.view.View;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.ChartData;

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

    /**
     * Refreshes the Chart to react to Data Set changes.
     *
     * The method is made asynchronous since multiple Data Sets
     * may attempt to refresh the Chart at the same time.
     */
    public void Refresh() {
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
        // The chosen solution is to then have delays and refresh throttling.


        // An AtomicReference is used to hold a single Refresh runnable, which is then
        // executed in a UIHandler. Since the AtomicReference holds a single Runnable
        // instance, it also acts as an accumulator in the case of too many refresh
        // calls being invoked within a time frame of 100ms.
        refreshRunnable.set(getRefreshRunnable());

        // Post a Refresh runnable on the UI Thread (via the UI Handler),
        // since refreshing should only be invoked in the UI thread (due
        // to accessing views). A delay of 100ms is used to throttle the
        // refresh rate to prevent crashes.
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Get the runnable from the AtomicReference
                Runnable runnable = refreshRunnable.getAndSet(null);

                // Runnable non-null; Execute it
                if (runnable != null) {
                    runnable.run();
                }
            }
        }, 100);

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

    protected Runnable getRefreshRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                // Notify the Data component of data changes (needs to be called
                // when Datasets get changed directly)
                chart.getData().notifyDataChanged();

                // Notify the Chart of Data changes (needs to be called
                // when Data objects get changed directly)
                chart.notifyDataSetChanged();

                // Invalidate the Chart view for the changes to take
                // effect. NOTE: Most exceptions with regards to data
                // changing too fast occur as a result of calling the
                // invalidate method.
                chart.invalidate();
            }
        };
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void Refresh2(final ChartDataModel model) {
        model.updateEntries();

        // Notify the Data component of data changes (needs to be called
        // when Datasets get changed directly)
        chart.getData().notifyDataChanged();

        // Notify the Chart of Data changes (needs to be called
        // when Data objects get changed directly)
        chart.notifyDataSetChanged();

        // Invalidate the Chart view for the changes to take
        // effect. NOTE: Most exceptions with regards to data
        // changing too fast occur as a result of calling the
        // invalidate method.
        chart.invalidate();
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
}
