package pl.itraff.androidsample.DaemonDashCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
/*
 * This class will extract the live information that will be displayed to the user about
 * the brand that they have encountered and the app has identified
 * CREDIT TO JSOUP, MINIMAL-JSON, AND MARKIT ON DEMAND FOR API/CODE 
 */
public class InfoGetter {
	private ArrayList<String> articles;
	private String summary;
	//IN ORDER: LAST PRICE, CHANGE PERCENT, HIGH, LOW
	private ArrayList<String> stocks;
	
	public InfoGetter(String companyName) throws IOException{
		articles = new ArrayList<String>();
		String sanitizedName = companyName.toLowerCase().replace(' ', '+');
		String sanitizedNameUnderscore = companyName.replace(' ', '_');
		//Make urls to process requests
		String newsUrl = "http://www.google.com/search?q=" + sanitizedName + "&tbm=nws";
		String wikiUrl = "http://en.wikipedia.org/wiki/" + sanitizedNameUnderscore;
		String symbolUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/jsonp?input=" + companyName + "&callback=myFunction";
		//Retrieve HTML at urls
		Document newspage = Jsoup.connect(newsUrl).get();
		Document wikipage = Jsoup.connect(wikiUrl).get();
		//Get news articles regarding company
		Elements stories = newspage.select("div.g._cy");
		Elements links = stories.select("a[href]");
		for (Element article : links){
			String link = article.attr("href");
			if(articles.contains(link) == false){
				articles.add(link);
			}
		}
		//create summary of business from wikipedia article regarding the business in question
		summary = Jsoup.parse(wikipage.select("p").first().toString()).text().replaceAll("\\[.*?\\]", "");
		
		//See if theres is a stock symbol for the given company
		String stockSymbol = Jsoup.parse(Jsoup.connect(symbolUrl).get().select("body").toString()).text();
		if (stockSymbol.split("\"").length < 3){
			stocks = null;
		}else {
			stockSymbol = stockSymbol.split("\"")[3];
			if (stockSymbol != null) {
				//If there is a stock symbol, get a quote through the API
				stocks = new ArrayList<String>();
				String stockURL = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/jsonp?symbol=" + stockSymbol + "&callback=myFunction";
				String stockQuote = Jsoup.parse(Jsoup.connect(stockURL).get().select("body").toString()).text();
				JsonObject stockJson = Json.parse(stockQuote.substring(stockQuote.indexOf("(") + 1, stockQuote.indexOf(")"))).asObject();
				stocks.add(stockJson.get("LastPrice").toString());
				stocks.add(stockJson.get("ChangePercent").toString());
				stocks.add(stockJson.get("High").toString());
				stocks.add(stockJson.get("Low").toString());
			} else {
				stocks = null;
			}
		}
	}
	
	public InfoGetter(String companyName, String stockSymbol) throws IOException{
		articles = new ArrayList<String>();
		String sanitizedName = companyName.toLowerCase().replace(' ', '+');
		String sanitizedNameUnderscore = companyName.replace(' ', '_');
		//Make urls to process requests
		String newsUrl = "http://www.google.com/search?q=" + sanitizedName + "&tbm=nws";
		String wikiUrl = "http://en.wikipedia.org/wiki/" + sanitizedNameUnderscore;
		String stockURL = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/jsonp?symbol=" + stockSymbol + "&callback=myFunction";
		//Retrieve HTML at urls
		Document newspage = Jsoup.connect(newsUrl).get();
		Document wikipage = Jsoup.connect(wikiUrl).get();
		//Get news articles regarding company
		Elements stories = newspage.select("div.g._cy");
		Elements links = stories.select("a[href]");
		for (Element article : links){
			String link = article.attr("href");
			if(articles.contains(link) == false){
				articles.add(link);
			}
		}
		//create summary of business from wikipedia article regarding the business in question
		summary = Jsoup.parse(wikipage.select("p").first().toString()).text().replaceAll("\\[.*?\\]", "");
		
		//Get stock quote from API
		stocks = new ArrayList<String>();
		String stockQuote = Jsoup.parse(Jsoup.connect(stockURL).get().select("body").toString()).text();
		JsonObject stockJson = Json.parse(stockQuote.substring(stockQuote.indexOf("(")+1,stockQuote.indexOf(")"))).asObject();
		stocks.add(stockJson.get("LastPrice").toString());
		stocks.add(stockJson.get("ChangePercent").toString());
		stocks.add(stockJson.get("High").toString());
		stocks.add(stockJson.get("Low").toString());
	}
	
	/*The following methods are getters for the instance variables of the class*/
	public ArrayList<String> getArticleURLs(){
		return articles;
	}
	
	public String getSummary(){
		return summary;
	}
	
	public ArrayList<String> getStockQuote(){
		return stocks;
	}
	
}
