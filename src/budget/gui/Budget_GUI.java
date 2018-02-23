package budget.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import budget.Budget_Database;
import budget.OFX_Reader;
import budget.plots.Line_Plot;
import budget.plots.Pie_Chart_3D;
import budget.records.Category_Record;

public class Budget_GUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final Category_Table category_table;
	private final Transaction_Table transaction_table;

	private enum Actions {
		CREATE_CATEGORY, DELETE_CATEGORY, ASSIGN_CATEGORY, SEARCH_MATCHES, PLOT_PIE, PLOT_LINE, IMPORT
	}

	public Budget_GUI(Budget_Database budget_database) {
		setLayout(new BorderLayout());
		transaction_table = new Transaction_Table(budget_database);
		category_table = new Category_Table(budget_database);

		JToolBar tool_bar = new JToolBar();
		add(tool_bar, BorderLayout.PAGE_START);

		JSplitPane split_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, transaction_table.getComponent(), category_table.getComponent());
		split_pane.setOneTouchExpandable(true);
		split_pane.setDividerLocation(500);

		add(split_pane, BorderLayout.CENTER);

		JButton create_category_button = new JButton("Create Category");
		create_category_button.setActionCommand(Actions.CREATE_CATEGORY.name());
		create_category_button.addActionListener(this);
		tool_bar.add(create_category_button);

		JButton assign_category_button = new JButton("Set Category");
		assign_category_button.setActionCommand(Actions.ASSIGN_CATEGORY.name());
		assign_category_button.addActionListener(this);
		tool_bar.add(assign_category_button);

		JButton search_regex_button = new JButton("Search for Matches");
		search_regex_button.setActionCommand(Actions.SEARCH_MATCHES.name());
		search_regex_button.addActionListener(this);
		tool_bar.add(search_regex_button);

		JButton delete_category_button = new JButton("Delete Category");
		delete_category_button.setActionCommand(Actions.DELETE_CATEGORY.name());
		delete_category_button.addActionListener(this);
		tool_bar.add(delete_category_button);

		JButton plot_pie_button = new JButton("Plot Pie");
		plot_pie_button.setActionCommand(Actions.PLOT_PIE.name());
		plot_pie_button.addActionListener(this);
		tool_bar.add(plot_pie_button);

		JButton plot_line_button = new JButton("Plot Line");
		plot_line_button.setActionCommand(Actions.PLOT_LINE.name());
		plot_line_button.addActionListener(this);
		tool_bar.add(plot_line_button);

		JButton import_button = new JButton("Import");
		import_button.setActionCommand(Actions.IMPORT.name());
		import_button.addActionListener(this);
		tool_bar.add(import_button);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Actions action = Actions.valueOf(e.getActionCommand());

		switch (action) {
		case ASSIGN_CATEGORY: {
			Category_Record c = category_table.getSelectedCategory();
			transaction_table.updateSelectedToCategory(c);
			break;
		}
		case CREATE_CATEGORY: {
			category_table.createCategory();
			break;
		}
		case DELETE_CATEGORY: {
			Category_Record c = category_table.deleteSelectedCategory();
			transaction_table.updateTransactionCategory(c, category_table.getNoneCategory());
			break;
		}
		case SEARCH_MATCHES: {
			Category_Record c = category_table.getSelectedCategory();
			transaction_table.searchRegex(c, category_table.getNoneCategory());
			break;
		}
		case PLOT_PIE: {
			new Pie_Chart_3D(transaction_table.getTransactions(), this);
			break;
		}
		case PLOT_LINE: {
			new Line_Plot(transaction_table.getTransactions(), this);
			break;
		}
		case IMPORT: {
			File ofx_file = showOpenDialogOFX();
			if (ofx_file != null) {
				OFX_Reader reader = new OFX_Reader();
				transaction_table.addTransactions(reader.read(ofx_file));
			}
			break;
		}
		default:
			break;
		}
	}

	private File showOpenDialogOFX() {
		JFileChooser file_chooser = new JFileChooser();
		file_chooser.addChoosableFileFilter(new FileFilter() {

			@Override
			public String getDescription() {

				return ".ofx files";
			}

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				int last_dot_index = 0;
				last_dot_index = file.getName().indexOf(".");
				if (last_dot_index != -1) {
					String extension = file.getName().substring(last_dot_index);
					return (extension != null) && extension.equals(".ofx");
				}
				return false;
			}
		});
		file_chooser.setAcceptAllFileFilterUsed(false);
		int returnVal = file_chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return file_chooser.getSelectedFile();
		else
			return null;
	}
}
