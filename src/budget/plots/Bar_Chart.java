package budget.plots;

import java.awt.Color;
import java.awt.GradientPaint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.SortOrder;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;

import budget.records.Category_Record;
import budget.records.Transaction_Record;

public class Bar_Chart extends JFrame implements ActionListener {

	private static class Category_Slice {
		double value;
		boolean selected;
		double average;
		final float months;

		List<Transaction_Record> transactions = new ArrayList<Transaction_Record>();
		AbstractTableModel transactionsModel;

		Category_Slice(Transaction_Record first_transaction, long diff) {
			months = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) / 30.44f;

			// this.value = first_transaction.amount * -1;
			// transactions.add(first_transaction);
			selected = true;

			Object[] column_names = { "Date", "Amount", "Type", "Name", "Category" };

			transactionsModel = new AbstractTableModel() {

				private static final long serialVersionUID = 1L;

				public String getColumnName(int col) {
					return column_names[col].toString();
				}

				@Override
				public Object getValueAt(int row, int col) {
					switch (col) {
					case 0:
						return transactions.get(row).deposited;
					case 1:
						return transactions.get(row).amount;
					case 2:
						return transactions.get(row).type;
					case 3:
						return transactions.get(row).name;
					case 4:
						return transactions.get(row).category.name;
					}

					return null;
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}

				@Override
				public int getRowCount() {
					return transactions.size();
				}

				@Override
				public int getColumnCount() {
					return 5;
				}

				@Override
				public Class<?> getColumnClass(int column) {
					switch (column) {
					case 0:
						return Date.class;
					case 1:
						return Float.class;
					case 2:
						return String.class;
					case 3:
						return String.class;
					case 4:
						return String.class;
					case 5:
						return Boolean.class;
					}
					return null;
				}
			};
		}

		public float getAverage() {
			return (float) (average / months);
		}

		public void addTransaction(Transaction_Record t) {
			value += t.amount * -1;
			transactions.add(t);
		}
	}

	private JTable category_table;
	private static final long serialVersionUID = 1L;
	private final List<Transaction_Record> transactions;
	private Map<Category_Record, Category_Slice> categories = new HashMap<Category_Record, Category_Slice>();
	private JComboBox<RegularTimePeriod> dates_combo;
	DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	private JFreeChart chart;
	private Actions currentPlot;
	private float total_pay;
	private CategoryPlot plot;
	private JTable transaction_table;
	private final String series1 = "Spending";
	private final String series2 = "Average";
	private final String series3 = "Goal";

	private enum Actions {
		PLOT_MONTH
	}

	public Bar_Chart(List<Transaction_Record> transactions, JFrame owner) {

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

		add(tool_bar, BorderLayout.PAGE_START);

		JScrollPane transaction_table = makeTransactionTable();
		JScrollPane category_table = makeCategoryTable();

		JSplitPane left_split_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chart_panel, transaction_table);
		left_split_pane.setOneTouchExpandable(true);

		JSplitPane split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left_split_pane, category_table);
		split_pane.setOneTouchExpandable(true);
		add(split_pane, BorderLayout.CENTER);

		pack();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
	}

	private JScrollPane makeCategoryTable() {
		Object[] column_names = { "Category", "Spending", "Average", "Goal", "Plot" };

		AbstractTableModel plots_model = new AbstractTableModel() {

			private static final long serialVersionUID = 1L;

			public String getColumnName(int col) {
				return column_names[col].toString();
			}

			@Override
			public Object getValueAt(int row, int col) {
				Category_Record record = (Category_Record) categories.keySet().toArray()[row];
				switch (col) {
				case 0:
					return record.name;
				case 1:
					return categories.get(record).value;
				case 2:
					return categories.get(record).getAverage();
				case 3:
					return record.goal;
				case 4:
					return categories.get(record).selected;
				}
				return null;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 4;
			}

			@Override
			public int getRowCount() {
				return categories.size();
			}

			@Override
			public int getColumnCount() {
				return column_names.length;
			}

			@Override
			public void setValueAt(Object aValue, int row, int col) {
				Category_Record record = (Category_Record) categories.keySet().toArray()[row];

				boolean selected = (Boolean) aValue;
				categories.get(record).selected = selected;

				switch (col) {
				case 4: {
					switch (currentPlot) {
					case PLOT_MONTH: {
						RegularTimePeriod selectedMonth = (RegularTimePeriod) dates_combo.getSelectedItem();
						float total_spending = 0;
						for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
							if (v.getValue().selected)
								total_spending += v.getValue().value;
						}

						dataset.clear();
						for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
							if (v.getValue().selected) {
								dataset.addValue(v.getValue().value, series1, v.getKey().name);
								dataset.addValue(v.getValue().getAverage(), series2, v.getKey().name);
								dataset.addValue(v.getKey().goal, series3, v.getKey().name);
							}
						}
						chart.setTitle("Total Pay " + total_pay + ", Total Spending " + total_spending + " for " + selectedMonth);
						break;
					}
					default:
						break;
					}

					break;
				}

				}
			}
		};

		category_table = new JTable(plots_model) {

			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return String.class;
				case 1:
					return Float.class;
				case 2:
					return Float.class;
				case 3:
					return Float.class;
				case 4:
					return Boolean.class;
				}
				return null;
			}
		};

		category_table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		category_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (category_table.getSelectedRow() > -1) {
					Category_Record record = (Category_Record) categories.keySet().toArray()[category_table.getSelectedRow()];
					transaction_table.setModel(categories.get(record).transactionsModel);
				}
			}
		});

		return new JScrollPane(category_table);
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

		chart = ChartFactory.createBarChart("", // chart
				// title
				"Category", // domain axis label
				"Value", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
		);

		plot = (CategoryPlot) chart.getPlot();
		plot.setDomainGridlinesVisible(true);
		plot.setRangePannable(true);
		plot.setRangeZeroBaselineVisible(true);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		LayeredBarRenderer renderer = new LayeredBarRenderer();
		renderer.setDrawBarOutline(false);
		plot.setRenderer(renderer);

		// for this renderer, we need to draw the first series last...
		plot.setRowRenderingOrder(SortOrder.DESCENDING);

		// set up gradient paints for series...
		GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue, 0.0f, 0.0f, new Color(0, 0, 64));
		GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green, 0.0f, 0.0f, new Color(0, 64, 0));
		GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f, 0.0f, new Color(64, 0, 0));
		renderer.setSeriesPaint(0, gp0);
		renderer.setSeriesPaint(1, gp1);
		renderer.setSeriesPaint(2, gp2);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

		return chart;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Actions action = Actions.valueOf(e.getActionCommand());
		transaction_table.setModel(new DefaultTableModel());

		category_table.clearSelection();
		currentPlot = action;

		switch (action) {
		case PLOT_MONTH: {
			RegularTimePeriod selectedMonth = (RegularTimePeriod) dates_combo.getSelectedItem();
			long start = selectedMonth.getStart().getTime();
			long end = selectedMonth.getEnd().getTime();

			categories.clear();

			final long diff = transactions.get(transactions.size() - 1).deposited.getTime() - transactions.get(0).deposited.getTime();

			total_pay = 0;
			float total_spending = 0;
			for (Transaction_Record t : transactions) {
				long date = t.deposited.getTime();
				if (!t.category.name.equals("NO REPORT")) {
					Category_Slice r = categories.get(t.category);
					if (r == null && !t.category.name.equals("Pay")) {
						categories.put(t.category, r = new Category_Slice(t, diff));
					}

					if (r != null) {
						r.average += t.amount * -1;
					}

					if (date >= start && date <= end) {
						if (r != null) {
							r.addTransaction(t);
						}

						if (t.amount < 0)
							total_spending += t.amount * -1;

						if (t.category.name.equals("Pay")) {
							total_pay += t.amount;
						}
					}
				}
			}

			dataset.clear();
			for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
				dataset.addValue(v.getValue().value, series1, v.getKey().name);
				dataset.addValue(v.getValue().getAverage(), series2, v.getKey().name);
				dataset.addValue(v.getKey().goal, series3, v.getKey().name);
			}

			chart.setTitle("Total Pay " + total_pay + ", Total Spending " + total_spending + " for " + selectedMonth);
			((AbstractTableModel) category_table.getModel()).fireTableRowsInserted(0, categories.size());
			break;
		}
		default:
			break;
		}
	}

	private JScrollPane makeTransactionTable() {
		transaction_table = new JTable();
		transaction_table.setAutoCreateRowSorter(true);
		transaction_table.setPreferredScrollableViewportSize(transaction_table.getPreferredSize());
		return new JScrollPane(transaction_table);
	}
}