package budget.records;

import java.awt.Paint;

import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;

public class Category_Record {
	public String name;
	public float goal;
	public String regex;
	public int goal_count;
	public int id;
	public final Paint color;
	
	private static DrawingSupplier supplier = new DefaultDrawingSupplier();

	public Category_Record(String name, float goal, String regex, int goal_count) {
		this(name, goal, regex, goal_count, -1);		
	}
	
	public Category_Record(String name, float goal, String regex, int goal_count, int id) {
		super();
		this.name = name;
		this.goal = goal;
		this.regex = regex;
		this.goal_count = goal_count;
		this.id = id;
		this.color = supplier.getNextPaint();
	}

	@Override
	public String toString() {
		return "CategoryRecord [name=" + name + ", goal=" + goal + ", regex=" + regex + ", goal_count=" + goal_count + ", id=" + id + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category_Record other = (Category_Record) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
