package budget.records;
import java.util.Date;

public class Transaction_Record {

	public Date deposited;
	public float amount;
	public String type;
	public String name; // name = name + memo
	public Category_Record category;
	public int id;

	public Transaction_Record(Date deposited, float amount, String type, String name, Category_Record category) {
		this(deposited, amount, type, name, category, -1);
	}

	public Transaction_Record(Date deposited, float amount, String type, String name, Category_Record category, int id) {
		super();
		this.deposited = deposited;
		this.amount = amount;
		this.type = type;
		this.name = name;
		this.category = category;
		this.id = id;
	}

	@Override
	public String toString() {
		return "TransactionRecord [id=" + id + ", deposited=" + deposited + ", amount=" + amount + ", type=" + type + ", name=" + name + ", category=" + category + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(amount);
		result = prime * result + ((deposited == null) ? 0 : deposited.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Transaction_Record other = (Transaction_Record) obj;
		if (Float.floatToIntBits(amount) != Float.floatToIntBits(other.amount))
			return false;
		if (deposited == null) {
			if (other.deposited != null)
				return false;
		} else if (!deposited.equals(other.deposited))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
