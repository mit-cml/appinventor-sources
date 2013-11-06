package codeblockutil;

import java.awt.Color;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

public class CBarGraph extends JLabel{
	private static final long serialVersionUID = 328149080230L;
	private static final Color DEFAULT_BACKGROUND = CGraphite.blue;
	private JFreeChart chart;
	private BarData chartData;
	private Color background;
    private boolean lock = false;
    private ChartPanel output = null;
	private double upperBound = -1e9;
	private double lowerBound = 0;
	
	public CBarGraph(String title, int seriesNum, Color background){
		super("", SwingConstants.CENTER);
		
		this.chartData = new BarData(title);
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
	
	public void updateValues(String seriesName, double value){
		if(!lock){
			DefaultCategoryDataset dataset = (DefaultCategoryDataset) chart.getCategoryPlot().getDataset();
			dataset.addValue(value, seriesName, Integer.valueOf(0));
			
			if (value < lowerBound) lowerBound = value;
			if (value > upperBound) upperBound = value;
			
			CategoryPlot plot = chart.getCategoryPlot();
			plot.getRangeAxis().setLowerBound(lowerBound);
			plot.getRangeAxis().setUpperBound(upperBound);
		}
	}
	
	/**
	 * Updates the series name at the specified index
	 * @param seriesName the new seriesName to set at the specified index
	 * @param index the desired index to set the new seriesName to
	 */
	public void updateSeriesNamesAt(String seriesName, int index) {
		//does nothing
		//note from ria: could not find a way to update the series name of a bar graph
		//in jfreechart
	}
	
	/**
	 * Updates the graph image displayed of this.  Image includes graph data, axis, and legend.
	 */
	public void updateImage(){
		if(!lock){
			ImageIcon icon = new ImageIcon(chart.createBufferedImage(150, 100, 150.0, 100.0, null));
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
		StringBuilder output = new StringBuilder();
		DefaultCategoryDataset d = (DefaultCategoryDataset) chart.getCategoryPlot().getDataset();
		for (int i = 0 ; i<d.getRowCount(); i++){
			output.append(d.getRowKey(i)+","+d.getValue(d.getRowKey(i),Integer.valueOf(0))+"\n");
		}
		return output.toString();
	}
	
	private class BarData extends ChartData {
		public BarData(String title) {
			super(title);
		}
		
		public JFreeChart makeChart() {
			JFreeChart chart;
			chart = ChartFactory.createBarChart3D("", "", "", 
					new DefaultCategoryDataset(), PlotOrientation.VERTICAL, false, false, false);
			ValueAxis rangeAxis = chart.getCategoryPlot().getRangeAxis();
			if(rangeAxis instanceof NumberAxis){
	        	((NumberAxis)rangeAxis).setNumberFormatOverride(new DecimalFormat("######.###"));
	        }
			return chart;
		}
	}
}