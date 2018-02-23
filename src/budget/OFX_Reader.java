package budget;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import budget.records.Category_Record;
import budget.records.Transaction_Record;

public class OFX_Reader {
	public OFX_Reader() {

	}

	private static final String TRNTYPE = "<TRNTYPE>";
	private static final String DTPOSTED = "<DTPOSTED>";
	private static final String TRNAMT = "<TRNAMT>";
	private static final String NAME = "<NAME>";
	private static final String MEMO = "<MEMO>";

	// 20180210000000[-6:CST]
	DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

	private Transaction_Record readTransaction(BufferedReader bufferedReader) throws IOException {

		Date deposited = new Date();
		float amount = 0;
		String type = "";
		String name = "";

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			String trim = line.trim();
			if (trim.equals("</STMTTRN>")) {
				break;
			}

			if (trim.startsWith(TRNTYPE)) {
				type = line.substring(line.indexOf(TRNTYPE) + TRNTYPE.length());
			}

			if (trim.startsWith(NAME)) {
				name += line.substring(line.indexOf(NAME) + NAME.length());
			}

			if (trim.startsWith(MEMO)) {
				name += " " + line.substring(line.indexOf(MEMO) + MEMO.length());
			}

			if (trim.startsWith(TRNAMT)) {
				amount = Float.parseFloat(line.substring(line.indexOf(TRNAMT) + TRNAMT.length()));
			}

			if (trim.startsWith(DTPOSTED)) {
				String date = line.substring(line.indexOf(DTPOSTED) + DTPOSTED.length());
				try {
					deposited = dateFormat.parse(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		Category_Record category = new Category_Record("None", 0, "", 0, 1);
		name = name.replace("'", "");
		return new Transaction_Record(deposited, amount, type, name, category);
	}

	public List<Transaction_Record> read(File ofxFile) {
		ArrayList<Transaction_Record> transactions = new ArrayList<Transaction_Record>();

		try {
			FileReader fileReader = new FileReader(ofxFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String trim = line.trim();
				if (trim.equals("<STMTTRN>")) {
					transactions.add(readTransaction(bufferedReader));
				}
			}

			fileReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return transactions;
	}

}
