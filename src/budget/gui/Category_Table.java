package budget.gui;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import budget.Budget_Database;
import budget.gui.imageJ.GenericDialog;
import budget.records.Category_Record;

public class Category_Table {
	private JTable category_table;
	private List<Category_Record> categories;
	private Budget_Database budget_database;

	public Category_Table(Budget_Database budget_database) {
		this.budget_database = budget_database;
		categories = budget_database.getAllCategories();

		Object[] column_names = { "Name", "Regex", "Goal" };

		AbstractTableModel category_model = new AbstractTableModel() {

			private static final long serialVersionUID = 1L;

			public String getColumnName(int col) {
				return column_names[col].toString();
			}

			@Override
			public void setValueAt(Object aValue, int row, int col) {
				switch (col) {
				case 0:
					categories.get(row).name = (String) aValue;
					break;
				case 1:
					categories.get(row).regex = (String) aValue;
					break;
				case 2:
					categories.get(row).goal = (float) aValue;
					break;
				case 3:
					categories.get(row).goal_count = (int) aValue;
					break;
				}

				budget_database.updateCategory(categories.get(row));
			}

			@Override
			public Object getValueAt(int row, int col) {
				switch (col) {
				case 0:
					return categories.get(row).name;
				case 1:
					return categories.get(row).regex;
				case 2:
					return categories.get(row).goal;
				case 3:
					return categories.get(row).goal_count;
				}

				return null;
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return rowIndex > 2;
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
			public Class<?> getColumnClass(int column) {
				switch (column) {
				case 0:
					return String.class;
				case 1:
					return String.class;
				case 2:
					return Float.class;
				case 3:
					return Integer.class;
				}
				return null;
			}
		};

		category_table = new JTable(category_model);
		category_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		category_table.setPreferredScrollableViewportSize(category_table.getPreferredSize());
	}

	public Category_Record getNoneCategory() {
		return categories.get(0);
	}

	public JComponent getComponent() {
		return new JScrollPane(category_table);
	}

	public Category_Record getSelectedCategory() {
		int selected = getSelected();
		if (selected >= 0)
			return categories.get(selected);
		return null;
	}

	private int getSelected() {
		if (category_table.getSelectedRow() == -1)
			JOptionPane.showMessageDialog(null, "No selection", "First select a category", JOptionPane.INFORMATION_MESSAGE);

		return category_table.getSelectedRow();
	}

	public void createCategory() {

		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(category_table);
		GenericDialog category_creation_dialog = new GenericDialog("Create Category", frame);
		category_creation_dialog.addStringField("Name: ", "");
		category_creation_dialog.addNumericField("Goal", 0.0, 2, 10, "Currency");
		category_creation_dialog.addStringField("Regex: ", "");
//		category_creation_dialog.addNumericField("Goal Frequency", 0, 0, 10, "Count");
		category_creation_dialog.showDialog();
		if (category_creation_dialog.wasCanceled())
			return;

		String name = category_creation_dialog.getNextString();
		float goal = (float) category_creation_dialog.getNextNumber();
		String regex = category_creation_dialog.getNextString();
		// TODO goal_count may come back but for now hide it 
//		int goal_count = (int) category_creation_dialog.getNextNumber();
		Category_Record new_category = new Category_Record(name, goal, regex, 0);
		budget_database.addCategory(new_category);
		categories.add(new_category);
		((AbstractTableModel) category_table.getModel()).fireTableRowsInserted(categories.size(), categories.size());
	}

	public Category_Record deleteSelectedCategory() {
		int selected = getSelected();
		if (selected >= 0) {
			Category_Record category_to_delete = categories.get(selected);
			// TODO also can not delete Pay or NO REPORT
			if (category_to_delete.id == 1) {
				JOptionPane.showMessageDialog(null, "Error", "Can not delete selected category", JOptionPane.ERROR_MESSAGE);
			} else {
				int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete category " + category_to_delete.name + "?", "Confirm Deletion", JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					budget_database.deleteCategory(category_to_delete);
					categories.remove(selected);
					((AbstractTableModel) category_table.getModel()).fireTableRowsDeleted(selected, selected);
					return category_to_delete;
				}
			}
		}
		return null;
	}
}
