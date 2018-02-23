package budget;

import java.io.File;
import javax.swing.JFrame;

import budget.gui.Budget_GUI;

public class Start {
	public static void main(String args[]) {
		Budget_Database budget_database = new Budget_Database();
		System.out.println("Starting your budget program!");

		String path_to_transactions_database = "Transactions.db";
		if (args.length > 0)
			path_to_transactions_database = args[1];

		File transactions_database_file = new File(path_to_transactions_database);
		if (!transactions_database_file.exists()) {
			System.err.println("Can't locatate database file at: " + path_to_transactions_database);
			return;
		}

		budget_database.open(transactions_database_file);

		Budget_GUI frame = new Budget_GUI(budget_database);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocation(150, 150);
		frame.setVisible(true);
	}
}
