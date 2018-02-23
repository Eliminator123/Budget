package budget;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import budget.records.Category_Record;
import budget.records.Transaction_Record;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Budget_Database {

	private Connection connection;

	public void open(File database) {
		String url = "jdbc:sqlite:" + database.getAbsolutePath();
		connection = null;
		try {
			connection = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void close() {
		try {
			connection.close();
			System.out.println("Closed database...");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void execute(String sql) {
		try (Statement stmt = connection.createStatement();) {
			stmt.execute(sql);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	//////////////////
	// Transactions //

	public List<Transaction_Record> getAllTransactions() {
		List<Category_Record> categories = getAllCategories();
		Map<Integer, Category_Record> category_map = new HashMap<Integer, Category_Record>();
		for (Category_Record category : categories) {
			category_map.put(category.id, category);
		}

		String sql = "SELECT * FROM transactions order by date asc";

		List<Transaction_Record> transactions = new ArrayList<Transaction_Record>();

		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			// loop through the result set
			while (rs.next()) {
				Category_Record c = category_map.get(rs.getInt("categoryId"));
				transactions.add(new Transaction_Record(new Date(rs.getLong("date")), (float) rs.getDouble("amount"), rs.getString("type"), rs.getString("name"), c, rs.getInt("transactionsId")));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return transactions;
	}

	public List<Transaction_Record> addTransactions(List<Transaction_Record> newTransactions, List<Transaction_Record> allTransactions) {
		List<Transaction_Record> addedTransactions = new ArrayList<Transaction_Record>();
		HashSet<Transaction_Record> allHash = new HashSet<Transaction_Record>(allTransactions);
		for (Transaction_Record trnsaction : newTransactions) {
			if (!allHash.contains(trnsaction)) {
				String sql = "INSERT INTO transactions VALUES(null," + trnsaction.deposited.getTime() + "," + trnsaction.amount + ",'" + trnsaction.type + "','" + trnsaction.name + "', "
						+ trnsaction.category.id + ")";
				try (Statement stmt = connection.createStatement();) {
					stmt.execute(sql);
					ResultSet rs = stmt.getGeneratedKeys();
					while (rs.next()) {
						trnsaction.id = rs.getInt(1);
					}
					addedTransactions.add(trnsaction);
				} catch (SQLException e) {
					System.out.println(e.getMessage() + " : " + sql);
				}
			} else {
				System.out.println("Transaction already in the database: " + trnsaction);
			}
		}
		return addedTransactions;
	}

	public void setTansactionCategory(final Transaction_Record transaction, Category_Record category) {
		transaction.category = category;
		String sql = "UPDATE transactions SET categoryId = " + transaction.category.id + " WHERE transactionsId = " + transaction.id + ";";
		execute(sql);
	}

	////////////////
	// Categories //

	public List<Category_Record> getAllCategories() {

		List<Category_Record> categories = new ArrayList<Category_Record>();
		String sql = "SELECT * FROM categories";

		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			// loop through the result set
			while (rs.next()) {
				categories.add(new Category_Record(rs.getString("name"), (float) rs.getDouble("goal"), rs.getString("regex"), rs.getInt("goalCount"), rs.getInt("categoriesId")));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return categories;
	}

	public void updateCategory(Category_Record category) {
		String sql = "UPDATE categories SET name = '" + category.name + "', goal = " + category.goal + ", regex = '" + category.regex + "', goalCount = " + category.goal_count + "   WHERE categoriesId = " + category.id + ";";
		execute(sql);
	}

	public void deleteCategory(Category_Record category) {
		execute("UPDATE transactions SET categoryId = 1 WHERE categoryId = " + category.id + ";");
		execute("DELETE FROM categories WHERE categoriesId = " + category.id + ";");
	}

	public void addCategory(Category_Record category) {
		String sql = "INSERT INTO categories VALUES(null,'" + category.name + "'," + category.goal + ",'" + category.regex + "'," + category.goal_count + ")";
		try (Statement stmt = connection.createStatement();) {
			stmt.execute(sql);
			ResultSet rs = stmt.getGeneratedKeys();
			while (rs.next()) {
				category.id = rs.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}
