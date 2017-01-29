package pl.itraff.androidsample;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.IOException;
import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class Parser {

	//parsing function
		public static void read () {
			try {
				String name = "", products = "", prices = "", reviewers = "", ratings = "", stockCode = "";

				File inputWorkbook = new File("data.xls");
				Workbook w;
				try {
					w = Workbook.getWorkbook(inputWorkbook);
					// Get the first sheet
					Sheet sheet = w.getSheet(0);
					// Loop over first 10 column and lines

					for (int j = 1; j < sheet.getRows(); j++) {

						for (int i = 0; i < sheet.getColumns(); i++) {
							//0 - Name
							//1 - Product list
							//2 - Prices
							//3 - Reviewer
							//4 - Rating
							//5 - Stock Symbol
							Cell cell = sheet.getCell(i, j);

							if (i == 0)
								name = cell.getContents();
							else if (i == 1)
								products = cell.getContents();
							else if (i == 2)
								prices = cell.getContents();
							else if (i == 3)
								reviewers = cell.getContents();
							else if (i == 4)
								ratings = cell.getContents();
							else
								stockCode = cell.getContents();
						}
				
				/*if (products.equals(""))
					products = null;
				if (prices.equals(""))
					prices = null;
				if (reviewers.equals(""))
					reviewers = null;
				if (ratings.equals(""))
					ratings = null;
				if (stockCode.equals(""))
					stockCode = null;*/

						Shop shop = new Shop(j, name, products, prices, reviewers, ratings, stockCode);
						MainActivity.database.addShop(shop);


					}
				} catch (BiffException e) {
					e.printStackTrace();
				}
			}
			catch (IOException e2)
			{
				e2.printStackTrace();
			}
		}
}