package budget.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import budget.Budget_Database;
import budget.records.Category_Record;
import budget.records.Transaction_Record;

public class Transaction_Table {
	private List<Transaction_Record> transactions;
	private HashSet<Transaction_Record> selected_transactions = new HashSet<Transaction_Record>();
	private Budget_Database budget_database;
	private JTable transaction_table;

	public Transaction_Table(Budget_Database budget_database) {
		this.budget_database = budget_database;
		transactions = budget_database.getAllTransactions();

		Object[] column_names = { "Date", "Amount", "Type", "Name", "Category", "Select" };

		AbstractTableModel transactionsModel = new AbstractTableModel() {

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
				case 5:
					return selected_transactions.contains(transactions.get(row));
				}

				return null;
			}

			@Override
			public void setValueAt(Object aValue, int row, int column) {
				if (column == 5) {
					boolean selected = (Boolean) aValue;
					if (selected)
						selected_transactions.add(transactions.get(row));
					else
						selected_transactions.remove(transactions.get(row));
				}

			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == column_names.length;
			}

			@Override
			public int getRowCount() {
				return transactions.size();
			}

			@Override
			public int getColumnCount() {
				return 6;
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

		transaction_table = new JTable(transactionsModel);
		transaction_table.setAutoCreateRowSorter(true);
		TableColumnModel columnModel = transaction_table.getColumnModel();
		columnModel.getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

				int model_row = transaction_table.convertRowIndexToModel(row);
								
				if (transactions.get(model_row).category.id == 1) {
					comp.setForeground(Color.RED);
				} else {
					comp.setForeground(null);
				}

				return comp;
			}
		});

		transaction_table.setPreferredScrollableViewportSize(transaction_table.getPreferredSize());
	}

	
	public List<Transaction_Record> getTransactions() {
		return transactions;
	}
	
	public JComponent getComponent() {
		return new JScrollPane(transaction_table);
	}

	public void updateSelectedToCategory(Category_Record category) {
		if (category != null) {
			for (Transaction_Record transaction : selected_transactions) {
				budget_database.setTansactionCategory(transaction, category);
			}
			selected_transactions.clear();
			((AbstractTableModel) transaction_table.getModel()).fireTableDataChanged();
		}
	}

	public void updateTransactionCategory(Category_Record original_category, Category_Record new_category) {
		if (original_category != null) {
			for (Transaction_Record transaction : transactions) {
				if (transaction.category == original_category)
					transaction.category = new_category;
			}

			((AbstractTableModel) transaction_table.getModel()).fireTableDataChanged();
		}
	}

	public void searchRegex(Category_Record new_category, Category_Record original_category) {
		if (original_category != null && new_category != null && !new_category.regex.isEmpty()) {
			selected_transactions.clear();

			Pattern p = Pattern.compile(new_category.regex);

			for (Transaction_Record t : transactions) {
				if (t.category.id == original_category.id) {
					Matcher m = p.matcher(t.name);
					if (m.find()) {
						selected_transactions.add(t);
					}
				}
			}

			((AbstractTableModel) transaction_table.getModel()).fireTableDataChanged();
		}
	}


	public void addTransactions(List<Transaction_Record> all_new_transactions) {
		List<Transaction_Record> addedTransactions = budget_database.addTransactions(all_new_transactions, transactions);		
		int size = transactions.size();
		addedTransactions.sort(new Comparator<Transaction_Record>() {
			@Override
			public int compare(Transaction_Record transaction_1, Transaction_Record transaction_2) {				
				return transaction_1.deposited.compareTo(transaction_2.deposited);
			}
		});
		transactions.addAll(addedTransactions);
		((AbstractTableModel) transaction_table.getModel()).fireTableRowsInserted(size, addedTransactions.size());
	}
}
