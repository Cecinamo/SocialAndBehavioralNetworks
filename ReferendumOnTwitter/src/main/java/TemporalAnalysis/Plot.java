
package TemporalAnalysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryTick;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.ui.ApplicationFrame;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

public class Plot extends ApplicationFrame {

    public Plot(String applicationTitle, String chartTitle, DefaultCategoryDataset dataset, String fileName, int n) throws IOException {
        super(applicationTitle);
        JFreeChart lineChart = ChartFactory.createLineChart(
                chartTitle,
                "Days", "N. of tweets",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        CategoryPlot plot = (CategoryPlot) lineChart.getPlot();
        plot.getRenderer().setSeriesPaint(0, Color.BLACK);
        plot.getRenderer().setSeriesPaint(1, Color.GREEN);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
        

        for (int i = 0; i < dataset.getColumnCount(); i++) {
            plot.getRenderer().setSeriesStroke(i, new BasicStroke(2));
        }

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickUnit(new NumberTickUnit(n));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setLabelFont(new Font("Verdana", Font.BOLD, 14));
        domainAxis.setTickLabelFont(new Font("Verdana", Font.PLAIN, 10));
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setCategoryLabelPositionOffset(0);
        domainAxis.setLowerMargin(00);
        domainAxis.setUpperMargin(00);
        domainAxis.setTickMarksVisible(true);
        domainAxis.setMaximumCategoryLabelLines(5);
        

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(900, 600));
        setContentPane(chartPanel);

        ChartUtilities.saveChartAsPNG(new File(fileName), lineChart, 800, 500);
    }
}
