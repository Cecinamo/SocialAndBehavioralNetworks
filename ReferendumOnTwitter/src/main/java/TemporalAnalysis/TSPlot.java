package TemporalAnalysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

/**
 * @author imssbora
 *
 */
public class TSPlot extends JFrame {

    private static final long serialVersionUID = 1L;

    public TSPlot(String title, String chartTitle, XYDataset dataset, String fileName, int n) throws IOException {
        super(title);

        // Create chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                chartTitle,
                "Days", "N. of tweets",
                dataset,
                true, true, true);

        //Changes background color
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 255, 255));
        plot.getRenderer().setSeriesPaint(0, Color.BLACK);
        plot.getRenderer().setSeriesPaint(1, Color.GREEN);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            plot.getRenderer().setSeriesStroke(i, new BasicStroke(2));
        }

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        //rangeAxis.setTickUnit(new NumberTickUnit(n));

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setLabelFont(new Font("Verdana", Font.BOLD, 14));
        domainAxis.setTickLabelFont(new Font("Verdana", Font.PLAIN, 10));
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setTickMarksVisible(true);
        domainAxis.setLabelAngle(0);
        domainAxis.setVerticalTickLabels(true);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(900, 900));
        setContentPane(chartPanel);

        ChartUtilities.saveChartAsPNG(new File(fileName), chart, 800, 600);
    }
}
