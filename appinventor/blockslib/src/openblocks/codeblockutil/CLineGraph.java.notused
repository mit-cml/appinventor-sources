package codeblockutil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

public class CLineGraph extends JLabel{
	private static final long serialVersionUID = 328149080211L;
	private static final Color DEFAULT_BACKGROUND = CGraphite.blue;
	private JFreeChart chart;
	private LineData chartData;
	private Color background;
    private BufferedImage img = null;
    private boolean lock = false;
    private ChartPanel output = null;
	
	public CLineGraph(String title, int seriesNum, Color background){
		super("", SwingConstants.CENTER);
		
		this.chartData = new LineData(title, true, 0.0, 30.0, 0.0, 100.0, seriesNum);
		this.chart = chartData.makeChart();
		this.background = background == null ? DEFAULT_BACKGROUND : background;
		this.chart.setBackgroundPaint(this.background);
		this.chart.setBorderPaint(null);
		
		ImageIcon icon = new ImageIcon(chart.createBufferedImage(150, 100));
		this.setLayout(null);
		this.setIcon(icon);
		this.setBounds(0,0,170,130);
	}
	
	public Insets getInsets(){
		return new Insets(15,10,15,10);
	}
	
	public void clearChart(){
		if(!lock){
			this.chart = chartData.makeChart();
			this.chart.setBackgroundPaint(this.background);
			this.chart.setBorderPaint(null);
			if(output != null){
				JFreeChart newChart = new JFreeChart(chart.getPlot());
		        newChart.getLegend().setPosition(RectangleEdge.TOP);
		        newChart.getLegend().setPadding(5, 5, 5, 5);
		        newChart.setBackgroundPaint(background);
				output.setChart(newChart);
				output.invalidate();
				output.repaint();
			}
		}
	}
	
	public void updateDomain(String title, int seriesNum, Color background){
		if(!lock){
			this.chartData.setSeriesNum(seriesNum);
			this.chart = chartData.makeChart();
			this.background = background == null ? DEFAULT_BACKGROUND : background;
			this.chart.setBackgroundPaint(this.background);
			this.chart.setBorderPaint(null);
			if(output != null){
				JFreeChart newChart = new JFreeChart(chart.getPlot());
		        newChart.getLegend().setPosition(RectangleEdge.TOP);
		        newChart.getLegend().setPadding(5, 5, 5, 5);
		        newChart.setBackgroundPaint(background);
				output.setChart(newChart);
				output.invalidate();
				output.repaint();
			}
		}
	}
	
	/**
	 * Clears all values for all series, leaving the same number of series 
	 */
	public void clearValues() {
		if (!lock) {
			for (int i=0; i<chart.getXYPlot().getSeriesCount(); i++) {
		        XYSeries s = ((XYSeriesCollection) chart.getXYPlot().getDataset()).getSeries(i);
		        while (s.getItemCount()>0) {
		        	s.remove(0);
		        }
			}
		}
	}

	/**
	 * Clear the graph starting from the startTime.
	 * @param startTime an x-value on the graph
	 */
	public void clearValues(int index, double startTime) {
	    if (!lock) {
	        XYSeries s = ((XYSeriesCollection) chart.getXYPlot().getDataset()).getSeries(index);
	        int i = s.indexOf(startTime);
	        if (i >= 0) {
	            int total = s.getItemCount();
	            for (; i < total; total--) {
	                s.remove(i);
	            }
	        }
	    }
	}
	
	/**
	 * Updates the values for the specified seriesName with the given values
	 * @param seriesName the name a series in the graph
	 * @param index the index of the desired series to update
	 * @param time the time at which to update
	 * @param value the value to update
	 */
	public void updateValues(String seriesName, int index, double time, double value){
		if(!lock){
			XYSeries s = ((XYSeriesCollection) chart.getXYPlot().getDataset()).getSeries(index);
			s.setKey(seriesName);
			s.addOrUpdate(time, value);
		}
	}
	
	/**
	 * Updates the series name at the specified index
	 * @param seriesName the new seriesName to set at the specified index
	 * @param index the desired index to set the new seriesName to
	 */
	public void updateSeriesNameAt(String seriesName, int index) {
		if(!lock){
			XYSeries s = ((XYSeriesCollection) chart.getXYPlot().getDataset()).getSeries(index);
			s.setKey(seriesName);
		}
	}
	
	/**
	 * Updates the graph image displayed of this.
	 * Image includes graph data, axis, and legend.
	 */
	public void updateImage(){
		if(!lock){
			GraphicsManager.recycleGCCompatibleImage(img);
	        img = GraphicsManager.getGCCompatibleImage(150, 100);
	        chart.draw(img.createGraphics(), new Rectangle2D.Double(0, 0, 150, 100), null);
	        ImageIcon icon = new ImageIcon(img);
	        //ImageIcon icon = new ImageIcon(chart.createBufferedImage(150, 100, 150.0, 100.0, null));
			this.setIcon(icon);
		}
	}
	
	public ChartPanel getOutputPanel(){
        //we return a copy of the chart because we only want to show the
		//legend in the larger view of the graph
        //not the small runtime graph block view
        JFreeChart newChart = new JFreeChart(chart.getPlot());
        newChart.getLegend().setPosition(RectangleEdge.TOP);
        newChart.getLegend().setPadding(5, 5, 5, 5);
        newChart.setBackgroundPaint(background);
		output = new ChartPanel(newChart);
        return output;
	}
	
	public BufferedImage getBufferedImage(int width, int height){
		return chart.createBufferedImage(width, height);
	}
	
	public String getCSV(){
		lock = true;
		int max = 0;
		List<List<String>> columns = new ArrayList<List<String>>();
		for (int i = 0; i< chartData.getSeriesNum(); i++) {
			if(i == 0){
				List<String> time = new ArrayList<String>();//{name, v1, v2, v3}
				XYSeries s = ((XYSeriesCollection) chart.getXYPlot().getDataset()).getSeries(i);
				max = Math.max(max, s.getItemCount());
				time.add("TIME,");
				for(Object o : s.getItems()){
					XYDataItem item = (XYDataItem)o;
					time.add(item.getX()+",");
				}
				columns.add(time);
			}
			List<String> values = new ArrayList<String>();//{name, v1, v2, v3}
			XYSeries s = ((XYSeriesCollection) chart.getXYPlot().getDataset()).getSeries(i);
			max = Math.max(max, s.getItemCount());
			values.add(s.getKey().toString()+",");
			for(Object o : s.getItems()){
				XYDataItem item = (XYDataItem)o;
				values.add(item.getY()+",");
			}
			columns.add(values);
		}
		StringBuilder output = new StringBuilder();
		for(int i = 0; i<max ; i++){
			for(int j=0; j<columns.size(); j++){
				if(i<columns.get(j).size()){
					output.append(columns.get(j).get(i));
				}else{
					output.append(",");
				}
			}
			output.append("\n");
		}
		lock = false;
		return output.toString();
	}
	
	private class LineData extends ChartData {
		private boolean autoRange;
		private double xMin;
		private double xMax;
		private double yMin;
		private double yMax;
		private int seriesNum;
		
		public LineData(String title, boolean autoRange, double xMin, double xMax, double yMin, double yMax, int seriesNum) {
			super(title);
			this.autoRange = autoRange;
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;
			this.seriesNum = seriesNum;
		}
		public JFreeChart makeChart() {
			JFreeChart chart;
			XYSeriesCollection lineDataset = new XYSeriesCollection();
			for (int i = 0; i < getSeriesNum(); i++)
				lineDataset.addSeries(new XYSeries(new Integer(i)));
			chart = ChartFactory.createXYLineChart("", "", "", lineDataset, 
					PlotOrientation.VERTICAL, false, false, false);
			XYItemRenderer r = chart.getXYPlot().getRenderer();
			
			for (int i = 0; i < getSeriesNum(); i++) {
				//int colorValue = Color.HSBtoRGB(i*17/240f, 1f,0.5f);
				int colorValue = Color.HSBtoRGB(((float)i)/getSeriesNum(), 1f,0.5f);
	            Color color = new Color(colorValue);
				r.setSeriesPaint(i, color);
				r.setSeriesStroke(i, new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
			}
			
	        NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
	        rangeAxis.setNumberFormatOverride(new DecimalFormat("######.###"));
	        NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
	        
	        if (autoRange) {
	        	rangeAxis.setAutoRange(true);
	        	domainAxis.setAutoRange(true);
	        }
	        else {
	        	domainAxis.setRange(xMin, xMax);
	        	rangeAxis.setRange(yMin, yMax);
	        }
			return chart;
		}
		public int getSeriesNum() {
			return seriesNum;
		}
		public void setSeriesNum(int seriesNum) {
			this.seriesNum = seriesNum;
		}
	}
}