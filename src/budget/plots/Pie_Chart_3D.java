package budget.plots;

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

public class Pie_Chart_3D extends JFrame implements ActionListener {

	private static class Category_Slice {
		double value;
		boolean selected;
		List<Transaction_Record> transactions = new ArrayList<Transaction_Record>();
		AbstractTableModel transactionsModel;

		Category_Slice(Transaction_Record first_transaction) {
			this.value = first_transaction.amount * -1;
			transactions.add(first_transaction);
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
	private final DefaultPieDataset dataset = new DefaultPieDataset();
	private JFreeChart chart;
	private Actions currentPlot;
	private float total_pay;
	private PiePlot3D plot;
	private JTable transaction_table;

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
		Object[] column_names = { "Category", "Plot" };

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
					return ((Category_Slice) categories.get(record)).selected;
				}
				return null;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return !(columnIndex == 0);
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
				case 1: {
					switch (currentPlot) {
					case PLOT_AVERAGE_DAILY: {
						long diff = transactions.get(transactions.size() - 1).deposited.getTime() - transactions.get(0).deposited.getTime();
						long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
						String dateTitle = " from " + new Day(transactions.get(0).deposited) + " to " + new Day(transactions.get(transactions.size() - 1).deposited);
						float total_spending = 0;
						for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
							if (v.getValue().selected)
								total_spending += v.getValue().value;
						}
						dataset.clear();
						for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
							if (v.getValue().selected) {
								float percentage = (float) ((v.getValue().value / total_spending) * 100.0);
								String averageDaily = " ($" + String.format("%.2f", v.getValue().value / days) + ") ";
								String comparableKey = v.getKey().name + averageDaily + String.format(" (%.2f", percentage) + "%)";
								dataset.setValue(comparableKey, v.getValue().value);
								plot.setSectionPaint(comparableKey, v.getKey().color);
							}
						}
						chart.setTitle("Total Pay " + total_pay + ", Average Daily Spending " + total_spending + dateTitle);
						break;
					}
					case PLOT_AVERAGE_MONTHLY: {
						long diff = transactions.get(transactions.size() - 1).deposited.getTime() - transactions.get(0).deposited.getTime();
						String dateTitle = " from " + new Day(transactions.get(0).deposited) + " to " + new Day(transactions.get(transactions.size() - 1).deposited);

						float months = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) / 30.44f;
						float total_spending = 0;
						for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
							if (v.getValue().selected)
								total_spending += v.getValue().value;
						}
						dataset.clear();
						for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
							if (v.getValue().selected) {
								float percentage = (float) ((v.getValue().value / total_spending) * 100.0);
								String averageMonthly = " ($" + String.format("%.2f", v.getValue().value / months) + ") ";
								String comparableKey = v.getKey().name + averageMonthly + String.format(" (%.2f", percentage) + "%)";
								dataset.setValue(comparableKey, v.getValue().value);
								plot.setSectionPaint(comparableKey, v.getKey().color);
							}
						}
						chart.setTitle("Total Pay " + total_pay + ", Average Monthly Spending " + total_spending + dateTitle);
						break;
					}
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
								float percentage = (float) ((v.getValue().value / total_spending) * 100.0);
								String totalSpent = " ($" + String.format("%.2f", v.getValue().value) + ")";
								String comparableKey = v.getKey().name + totalSpent + String.format(" (%.2f", percentage) + "%)";
								dataset.setValue(comparableKey, v.getValue().value);
								plot.setSectionPaint(comparableKey, v.getKey().color);
							}
						}
						chart.setTitle("Total Pay " + total_pay + ", Total Spending " + total_spending + " for " + selectedMonth);
						break;
					}
					case PLOT_TOTAL: {
						float total_spending = 0;
						for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
							if (v.getValue().selected)
								total_spending += v.getValue().value;
						}
						dataset.clear();
						for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
							if (v.getValue().selected) {
								float percentage = (float) ((v.getValue().value / total_spending) * 100.0);
								String totalSpent = " ($" + String.format("%.2f", v.getValue().value) + ")";
								String comparableKey = v.getKey().name + totalSpent + String.format(" (%.2f", percentage) + "%)";
								dataset.setValue(comparableKey, v.getValue().value);
								plot.setSectionPaint(comparableKey, v.getKey().color);
							}
						}
						String dateTitle = " from " + new Day(transactions.get(0).deposited) + " to " + new Day(transactions.get(transactions.size() - 1).deposited);
						chart.setTitle("Total Pay " + total_pay + ", Total Spending " + total_spending + dateTitle);
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

		chart = ChartFactory.createPieChart3D("", // title
				dataset, // data
				false, // include legend
				true, false);

		plot = (PiePlot3D) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.5f);
		plot.setNoDataMessage("No data to display");
		return chart;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Actions action = Actions.valueOf(e.getActionCommand());
		transaction_table.setModel(new DefaultTableModel());

		category_table.clearSelection();
		currentPlot = action;

		if (action == Actions.PLOT_AVERAGE_DAILY || action == Actions.PLOT_AVERAGE_MONTHLY || action == Actions.PLOT_TOTAL) {

			categories.clear();

			total_pay = 0;
			float total_spending = 0;
			for (Transaction_Record t : transactions) {
				if (!t.category.name.equals("NO REPORT")) {
					Category_Slice r = categories.get(t.category);
					if (r != null) {
						r.addTransaction(t);
					} else if (!t.category.name.equals("Pay")) {
						categories.put(t.category, new Category_Slice(t));
					}

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
				for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
					float percentage = (float) ((v.getValue().value / total_spending) * 100.0);
					String averageDaily = " ($" + String.format("%.2f", v.getValue().value / days) + ") ";
					String comparableKey = v.getKey().name + averageDaily + String.format(" (%.2f", percentage) + "%)";
					dataset.setValue(comparableKey, v.getValue().value);
					plot.setSectionPaint(comparableKey, v.getKey().color);
				}
				chart.setTitle("Total Pay " + total_pay + ", Average Daily Spending " + total_spending + dateTitle);
				break;
			}
			case PLOT_AVERAGE_MONTHLY: {
				float months = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) / 30.44f;

				for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
					float percentage = (float) ((v.getValue().value / total_spending) * 100.0);
					String averageMonthly = " ($" + String.format("%.2f", v.getValue().value / months) + ") ";
					String comparableKey = v.getKey().name + averageMonthly + String.format(" (%.2f", percentage) + "%)";
					dataset.setValue(comparableKey, v.getValue().value);
					plot.setSectionPaint(comparableKey, v.getKey().color);
				}
				chart.setTitle("Total Pay " + total_pay + ", Average Monthly Spending " + total_spending + dateTitle);
				break;
			}
			case PLOT_TOTAL: {
				for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
					float percentage = (float) ((v.getValue().value / total_spending) * 100.0);
					String totalSpent = " ($" + String.format("%.2f", v.getValue().value) + ")";
					String comparableKey = v.getKey().name + totalSpent + String.format(" (%.2f", percentage) + "%)";
					dataset.setValue(comparableKey, v.getValue().value);
					plot.setSectionPaint(comparableKey, v.getKey().color);
				}
				chart.setTitle("Total Pay " + total_pay + ", Total Spending " + total_spending + dateTitle);
				break;
			}
			default:
				break;
			}
			((AbstractTableModel) category_table.getModel()).fireTableRowsInserted(0, categories.size());
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

			categories.clear();

			total_pay = 0;
			float total_spending = 0;
			for (Transaction_Record t : transactions) {
				long date = t.deposited.getTime();
				if (!t.category.name.equals("NO REPORT") && date >= start && date <= end) {
					Category_Slice r = categories.get(t.category);
					if (r != null) {
						r.addTransaction(t);
					} else if (!t.category.name.equals("Pay")) {
						categories.put(t.category, new Category_Slice(t));
					}

					if (t.amount < 0)
						total_spending += t.amount * -1;

					if (t.category.name.equals("Pay")) {
						total_pay += t.amount;
					}
				}
			}

			dataset.clear();
			for (Entry<Category_Record, Category_Slice> v : categories.entrySet()) {
				float percentage = (float) ((v.getValue().value / total_spending) * 100.0);
				String totalSpent = " ($" + String.format("%.2f", v.getValue().value) + ")";
				String comparableKey = v.getKey().name + totalSpent + String.format(" (%.2f", percentage) + "%)";
				dataset.setValue(comparableKey, v.getValue().value);
				plot.setSectionPaint(comparableKey, v.getKey().color);
			}
			chart.setTitle("Total Pay " + total_pay + ", Total Spending " + total_spending + " for " + selectedMonth);
			((AbstractTableModel) category_table.getModel()).fireTableRowsInserted(0, categories.size());
			break;
		}
		case PLOT_TOTAL: {
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