package budget.plots;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JToolBar;

/* =========s==================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * --------------------
 * PieChart3DDemo1.java
 * --------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: PieChart3DDemo1.java,v 1.8 2004/04/26 19:12:00 taqua Exp $
 *
 * Changes
 * -------
 * 19-Jun-2002 : Version 1 (DG);
 * 31-Jul-2002 : Updated with changes to Pie3DPlot class (DG);
 * 11-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 23-Dec-2003 : Renamed Pie3DChartDemo1 --> PieChart3DDemo1 (DG);
 *
 */

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Rotation;

import budget.records.Category_Record;
import budget.records.Transaction_Record;

/**
 * A simple demonstration application showing how to create a pie chart using
 * data from a {@link DefaultPieDataset}.
 *
 */
public class Pie_Chart_3D extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private List<Transaction_Record> transactions;
	private JComboBox<RegularTimePeriod> dates_combo;
	private final DefaultPieDataset dataset = new DefaultPieDataset();
	private JFreeChart chart;

	private enum Actions {
		PLOT_MONTH, PLOT_TOTAL, PLOT_AVERAGE_DAILY, PLOT_AVERAGE_MONTHLY
	}

	public Pie_Chart_3D(List<Transaction_Record> transactions, JFrame owner) {

		super();
		setLayout(new BorderLayout());
		this.transactions = transactions;

		// create the chart...
		final JFreeChart chart = createChart();

		// add the chart to a panel...
		final ChartPanel chart_panel = new ChartPanel(chart);
		chart_panel.setPreferredSize(new java.awt.Dimension(500, 270));

		JToolBar tool_bar = new JToolBar();

		HashSet<RegularTimePeriod> records = new HashSet<RegularTimePeriod>();
		for (Transaction_Record t : transactions) {
			RegularTimePeriod time = new Month(t.deposited);
			records.add(time);
		}

		RegularTimePeriod[] sortedRecords = records.toArray(new RegularTimePeriod[0]);
		Arrays.sort(sortedRecords);
		dates_combo = new JComboBox<RegularTimePeriod>();
		for (RegularTimePeriod v : sortedRecords) {
			dates_combo.addItem(v);
		}

		JButton plot_month_button = new JButton("Plot Month Spending");
		plot_month_button.setActionCommand(Actions.PLOT_MONTH.name());
		plot_month_button.addActionListener(this);
		tool_bar.add(plot_month_button);

		tool_bar.add(dates_combo);

		tool_bar.addSeparator();

		JButton plot_total_spending_button = new JButton("Plot Total Spending");
		plot_total_spending_button.setActionCommand(Actions.PLOT_TOTAL.name());
		plot_total_spending_button.addActionListener(this);
		tool_bar.add(plot_total_spending_button);

		JButton plot_average_daily_button = new JButton("Plot Average Daily Spending");
		plot_average_daily_button.setActionCommand(Actions.PLOT_AVERAGE_DAILY.name());
		plot_average_daily_button.addActionListener(this);
		tool_bar.add(plot_average_daily_button);

		JButton plot_average_monthly_button = new JButton("Plot Average Monthly Spending");
		plot_average_monthly_button.setActionCommand(Actions.PLOT_AVERAGE_MONTHLY.name());
		plot_average_monthly_button.addActionListener(this);
		tool_bar.add(plot_average_monthly_button);

		add(tool_bar, BorderLayout.PAGE_START);
		add(chart_panel, BorderLayout.CENTER);

		pack();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
	}

	private static class summ {
		double v;

		summ(double v) {
			this.v = v;
		}
	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *            the dataset.
	 * 
	 * @return A chart.
	 */
	private JFreeChart createChart() {

		chart = ChartFactory.createPieChart3D("", // title
				dataset, // data
				false, // include legend
				true, false);

		final PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.5f);
		plot.setNoDataMessage("No data to display");
		return chart;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Actions action = Actions.valueOf(e.getActionCommand());

		if (action == Actions.PLOT_AVERAGE_DAILY || action == Actions.PLOT_AVERAGE_MONTHLY || action == Actions.PLOT_TOTAL) {
			HashMap<Category_Record, summ> records = new HashMap<Category_Record, summ>();

			float total_pay = 0;
			float total_spending = 0;
			for (Transaction_Record t : transactions) {
				if (!t.category.name.equals("NO REPORT")) {
					summ r = records.get(t.category);
					if (r != null)
						r.v += t.amount * -1;
					else
						records.put(t.category, new summ(t.amount * -1));

					if (t.amount < 0)
						total_spending += t.amount * -1;

					if (t.category.name.equals("Pay")) {
						total_pay += t.amount;
					}
				}
			}

			dataset.clear();

			long diff = transactions.get(transactions.size() - 1).deposited.getTime() - transactions.get(0).deposited.getTime();
			long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
			String dateTitle = " from " + new Day(transactions.get(0).deposited) + " to " + new Day(transactions.get(transactions.size() - 1).deposited);
			
			switch (action) {
			case PLOT_AVERAGE_DAILY: {
				for (Entry<Category_Record, summ> v : records.entrySet()) {
					float percentage = (float) ((v.getValue().v / total_spending) * 100.0);
					String averageDaily = " ($" + String.format("%.2f", v.getValue().v / days) + ") ";
					dataset.setValue(v.getKey().name + averageDaily + String.format(" (%.2f", percentage) + "%)", v.getValue().v);
				}
				chart.setTitle("Total Pay " + total_pay + ", Average Daily Spending " + total_spending + dateTitle);
				break;
			}
			case PLOT_AVERAGE_MONTHLY: {
				float months = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) / 30.44f;

				for (Entry<Category_Record, summ> v : records.entrySet()) {
					float percentage = (float) ((v.getValue().v / total_spending) * 100.0);
					String averageMonthly = " ($" + String.format("%.2f", v.getValue().v / months) + ") ";
					dataset.setValue(v.getKey().name + averageMonthly + String.format(" (%.2f", percentage) + "%)", v.getValue().v);
				}
				chart.setTitle("Total Pay " + total_pay + ", Average Monthly Spending " + total_spending + dateTitle);
				break;
			}			
			case PLOT_TOTAL: {
				for (Entry<Category_Record, summ> v : records.entrySet()) {
					float percentage = (float) ((v.getValue().v / total_spending) * 100.0);
					String totalSpent = " ($" + String.format("%.2f", v.getValue().v) + ")";
					dataset.setValue(v.getKey().name + totalSpent + String.format(" (%.2f", percentage) + "%)", v.getValue().v);
				}
				chart.setTitle("Total Pay " + total_pay + ", Total Spending " + total_spending + dateTitle);
				break;
			}
			default:
				break;
			};			
		}

		switch (action) {
		case PLOT_AVERAGE_DAILY: {
			break;
		}
		case PLOT_AVERAGE_MONTHLY: {
			break;
		}
		case PLOT_MONTH: {
			RegularTimePeriod selectedMonth = (RegularTimePeriod) dates_combo.getSelectedItem();
			long start = selectedMonth.getStart().getTime();
			long end = selectedMonth.getEnd().getTime();
			
			HashMap<Category_Record, summ> records = new HashMap<Category_Record, summ>();

			float total_pay = 0;
			float total_spending = 0;
			for (Transaction_Record t : transactions) {
				long date = t.deposited.getTime();
				if (!t.category.name.equals("NO REPORT") && date >= start && date <= end) {
					summ r = records.get(t.category);
					if (r != null)
						r.v += t.amount * -1;
					else
						records.put(t.category, new summ(t.amount * -1));

					if (t.amount < 0)
						total_spending += t.amount * -1;

					if (t.category.name.equals("Pay")) {
						total_pay += t.amount;
					}
				}
			}

			dataset.clear();
			for (Entry<Category_Record, summ> v : records.entrySet()) {
				float percentage = (float) ((v.getValue().v / total_spending) * 100.0);
				String totalSpent = " ($" + String.format("%.2f", v.getValue().v) + ")";
				dataset.setValue(v.getKey().name + totalSpent + String.format(" (%.2f", percentage) + "%)", v.getValue().v);
			}
			chart.setTitle("Total Pay " + total_pay + ", Total Spending " + total_spending + " for " + selectedMonth);
			break;
		}
		case PLOT_TOTAL: {
			break;
		}
		default:
			break;
		}
	}
}