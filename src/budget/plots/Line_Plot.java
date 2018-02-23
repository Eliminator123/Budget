package budget.plots;

import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RefineryUtilities;

import budget.records.Category_Record;
import budget.records.Transaction_Record;

/**
 * A time series demo, with monthly data, where the tick unit on the axis is set
 * to one month also (this switches off the auto tick unit selection, and *can*
 * result in overlapping labels).
 */
public class Line_Plot extends JFrame {

	private static final long serialVersionUID = 1L;
	private List<Transaction_Record> transactions;
	private TimeSeriesCollection dataset = new TimeSeriesCollection();

	public Line_Plot(List<Transaction_Record> transactions, JFrame owner) {
		super();

		this.transactions = transactions;

		JFreeChart chart = createChart(dataset);
		ChartPanel chart_panel = new ChartPanel(chart);

		Set<Category_Record> categories = new HashSet<Category_Record>();
		for (Transaction_Record transaction : transactions) {
			if (!transaction.category.name.equals("NO REPORT")) {
				categories.add(transaction.category);
			}
		}
		plots.add(new LineSeries("Total Spending", null));
		for (Category_Record category : categories) {
			plots.add(new LineSeries(category.name, category));
		}

		JScrollPane plot_table = makePlotTable();
		JSplitPane split_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chart_panel, plot_table);
		split_pane.setOneTouchExpandable(true);
		split_pane.setDividerLocation(500);

		// Provide minimum sizes for the two components in the split pane
		Dimension minimum_size = new Dimension(100, 50);
		chart_panel.setMinimumSize(minimum_size);
		plot_table.setMinimumSize(minimum_size);

		setContentPane(split_pane);

		pack();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
	}

	static class LineSeries {
		String name;
		boolean total_by_month;
		TimeSeries series_total_by_month;

		boolean total_by_day;
		TimeSeries series_total_by_day;

		boolean average_by_month;
		TimeSeries series_average_by_month;
		
		boolean average_by_day;
		TimeSeries series_average_by_day;

		Category_Record category; // c is null for total spending

		LineSeries(String name, Category_Record category) {
			this.name = name;
			this.category = category;
		}
	}

	List<LineSeries> plots = new ArrayList<LineSeries>();

	private JScrollPane makePlotTable() {
		Object[] column_names = { "Name", "Total By Month", "Total By Day", "Average By Month", "Average By Day" };

		AbstractTableModel plots_model = new AbstractTableModel() {

			private static final long serialVersionUID = 1L;

			public String getColumnName(int col) {
				return column_names[col].toString();
			}

			@Override
			public Object getValueAt(int row, int col) {
				switch (col) {
				case 0:
					return plots.get(row).name;
				case 1:
					return plots.get(row).total_by_month;
				case 2:
					return plots.get(row).total_by_day;
				case 3:
					return plots.get(row).average_by_month;
				case 4:
					return plots.get(row).average_by_day;
				}
				return null;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return !(columnIndex == 0);
			}

			@Override
			public int getRowCount() {
				return plots.size();
			}

			@Override
			public int getColumnCount() {
				return column_names.length;
			}

			@Override
			public void setValueAt(Object aValue, int row, int col) {
				LineSeries plot = plots.get(row);
				boolean selected = (Boolean) aValue;

				switch (col) {
				case 1: {
					plot.total_by_month = selected;

					if (selected) {
						if (plot.category != null) {

							float multiplier = -1;
							if (plot.name.equals("Pay"))
								multiplier = 1;

							plot.series_total_by_month = new TimeSeries("Total By Month " + plot.name);
							HashMap<RegularTimePeriod, summ> payRecordsMonth = getData(plot.category, multiplier, false);
							for (Entry<RegularTimePeriod, summ> v : payRecordsMonth.entrySet()) {
								plot.series_total_by_month.add(v.getKey(), v.getValue().v);
							}
						} else {
							plot.series_total_by_month = new TimeSeries("Total Spending By Month");
							HashMap<RegularTimePeriod, summ> spendingRecordsMonth = getTotalSpending(false);
							for (Entry<RegularTimePeriod, summ> v : spendingRecordsMonth.entrySet()) {
								plot.series_total_by_month.add(v.getKey(), v.getValue().v);
							}
						}
						dataset.addSeries(plot.series_total_by_month);
					} else if (plot.series_total_by_month != null) {
						dataset.removeSeries(plot.series_total_by_month);
					}
					break;
				}
				case 2: {
					plot.total_by_day = selected;

					if (selected) {
						if (plot.category != null) {

							float multiplier = -1;
							if (plot.name.equals("Pay"))
								multiplier = 1;

							plot.series_total_by_day = new TimeSeries("Total By Day " + plot.name);
							HashMap<RegularTimePeriod, summ> payRecordsMonth = getData(plot.category, multiplier, true);
							for (Entry<RegularTimePeriod, summ> v : payRecordsMonth.entrySet()) {
								plot.series_total_by_day.add(v.getKey(), v.getValue().v);
							}
						} else {
							plot.series_total_by_day = new TimeSeries("Total Spending By Day");
							HashMap<RegularTimePeriod, summ> spendingRecordsMonth = getTotalSpending(true);
							for (Entry<RegularTimePeriod, summ> v : spendingRecordsMonth.entrySet()) {
								plot.series_total_by_day.add(v.getKey(), v.getValue().v);
							}
						}
						dataset.addSeries(plot.series_total_by_day);
					} else if (plot.series_total_by_day != null) {
						dataset.removeSeries(plot.series_total_by_day);
					}
					break;
				}
				case 3: {
					plot.average_by_month = selected;
					
					long diff = transactions.get(transactions.size() - 1).deposited.getTime() - transactions.get(0).deposited.getTime();
				    float months = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)/30.44f;
					   
					if (selected) {
						if (plot.category != null) {

							float multiplier = -1;
							if (plot.name.equals("Pay"))
								multiplier = 1;

							plot.series_average_by_month = new TimeSeries("Average By Month " + plot.name);
							HashMap<RegularTimePeriod, summ> categoryRecordsMonth = getData(plot.category, multiplier, false);
							float totalValue = 0;
							for (Entry<RegularTimePeriod, summ> v : categoryRecordsMonth.entrySet()) {
								totalValue += v.getValue().v;
							}
							for (Entry<RegularTimePeriod, summ> v : categoryRecordsMonth.entrySet()) {
								plot.series_average_by_month.add(v.getKey(), totalValue/months);
							}
						} else {
							plot.series_average_by_month = new TimeSeries("Average Total Spending By Month");
							HashMap<RegularTimePeriod, summ> categoryRecordsMonth = getTotalSpending(false);
							float totalValue = 0;
							for (Entry<RegularTimePeriod, summ> v : categoryRecordsMonth.entrySet()) {
								totalValue += v.getValue().v;
							}
							for (Entry<RegularTimePeriod, summ> v : categoryRecordsMonth.entrySet()) {
								plot.series_average_by_month.add(v.getKey(), totalValue/months);
							}
						}
						dataset.addSeries(plot.series_average_by_month);
					} else if (plot.series_average_by_month != null) {
						dataset.removeSeries(plot.series_average_by_month);
					}
					
					break;
				}
				case 4: {
					plot.average_by_day = selected;
					
				    long diff = transactions.get(transactions.size() - 1).deposited.getTime() - transactions.get(0).deposited.getTime();
				    long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
				  	   
					if (selected) {
						if (plot.category != null) {

							float multiplier = -1;
							if (plot.name.equals("Pay"))
								multiplier = 1;

							plot.series_average_by_day = new TimeSeries("Average By Day " + plot.name);						   
							HashMap<RegularTimePeriod, summ> categoryRecordsDay = getData(plot.category, multiplier, true);							
							float totalValue = 0;
							for (Entry<RegularTimePeriod, summ> v : categoryRecordsDay.entrySet()) {
								totalValue += v.getValue().v;							
							}
							for (Entry<RegularTimePeriod, summ> v : categoryRecordsDay.entrySet()) {
								plot.series_average_by_day.add(v.getKey(), totalValue/days);
							}
						} else {
							plot.series_average_by_day = new TimeSeries("Average Total Spending By Day");
							HashMap<RegularTimePeriod, summ> totalSpendingRecordsDay = getTotalSpending(true);
							float totalValue = 0;
							for (Entry<RegularTimePeriod, summ> v : totalSpendingRecordsDay.entrySet()) {
								totalValue += v.getValue().v;
							}
							for (Entry<RegularTimePeriod, summ> v : totalSpendingRecordsDay.entrySet()) {
								plot.series_average_by_day.add(v.getKey(), totalValue/days);
							}
						}
						dataset.addSeries(plot.series_average_by_day);
					} else if (plot.series_average_by_day != null) {
						dataset.removeSeries(plot.series_average_by_day);
					}
					break;
				}
				}
			}
		};

		JTable table = new JTable(plots_model) {

			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return String.class;
				case 1:
					return Boolean.class;
				case 2:
					return Boolean.class;
				case 3:
					return Boolean.class;
				case 4:
					return Boolean.class;
				}
				return null;
			}
		};

		JScrollPane scrollPane = new JScrollPane(table);
		return scrollPane;
	}

	private static class summ {
		double v;

		summ(double v) {
			this.v = v;
		}
	}

	private HashMap<RegularTimePeriod, summ> getTotalSpending(boolean day) {

		HashMap<RegularTimePeriod, summ> records = new HashMap<RegularTimePeriod, summ>();
		for (Transaction_Record t : transactions) {
			if (!t.category.name.equals("NO REPORT") && !t.category.name.equals("Pay")) {
				RegularTimePeriod time = day ? new Day(t.deposited) : new Month(t.deposited);
				summ s = records.get(time);
				if (s != null)
					s.v += t.amount * -1;
				else
					records.put(time, new summ(t.amount * -1));
			}
		}
		return records;
	}

	private HashMap<RegularTimePeriod, summ> getData(Category_Record c, float multiplier, boolean day) {

		HashMap<RegularTimePeriod, summ> records = new HashMap<RegularTimePeriod, summ>();
		for (Transaction_Record t : transactions) {
			if (t.category.id == c.id) {
				RegularTimePeriod time = day ? new Day(t.deposited) : new Month(t.deposited);
			
				summ s = records.get(time);
				if (s != null)
					s.v += t.amount * multiplier;
				else
					records.put(time, new summ(t.amount * multiplier));
			}
		}
		return records;
	}

	/**
	 * Creates a new chart.
	 *
	 * @param dataset
	 *            the dataset.
	 *
	 * @return The dataset.
	 */
	private JFreeChart createChart(XYDataset dataset) {
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Time Series", "Time", "Value", dataset, true, true, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setDomainPannable(true);
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 14, new SimpleDateFormat("MMM-d-yyyy")));
		axis.setVerticalTickLabels(true);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesFillPaint(0, Color.red);
		renderer.setSeriesFillPaint(1, Color.white);
		renderer.setUseFillPaint(true);
		renderer.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator("Tooltip {0}"));
		return chart;
	}

	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 *
	 * @return A panel.
	 */
	public JPanel createDemoPanel() {
		JFreeChart chart = createChart(dataset);
		return new ChartPanel(chart);
	}
}
