package mnicky.messenger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SMEDownloader implements IDownloader {

	public static enum Category implements ICategory {
		DOMACE, ZAHRANICNE, EKONOMIKA_SK, EKONOMIKA_SVET, KULTURA, KOMENTARE
	}

	//TODO: use desktop useragent? 
	//private final String USERAGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36";
	private final String ARTICLE_FETCH_URL = "http://s.sme.sk/export/phone/html/?cf=";
	private final Pattern ARTICLE_ID_PATTERN = Pattern.compile("(?:.*sme.sk)?/c/(\\d+).*");
	private final Pattern CATEGORY_BASEURL_PATTERN = Pattern.compile("(http://.*.sme.sk)(?:/.*)?");

	/** Download last 'n' articles from given SME.sk category.
	 * 
	 * @param n how many articles to download
	 * @param category category to get articles from
	 * @param delay how much miliseconds to wait after every article download
	 * @return list of downloaded Articles
	 */
	public List<Article> fetchLast(int n, ICategory category, int delay) {
		final List<Article> articles = new ArrayList<Article>();
		final int max = n;
		final String categoryUrl = categoryURL((Category)category);
		
		int categorySubpage = 1;
		while (articles.size() < max && categorySubpage <= 50) {
			
			//get article urls from the next category subpage
			final List<String> articleUrls = getArticleURLs(categoryUrl + categorySubpage);
			
			//fetch articles
			for (final String url : articleUrls) {
				final Article article = getArticle(url, (Category)category);
				if (article != null) {
					articles.add(article);
				}
				else {
					System.err.println("WARNING: can't fetch article from url: " + url);
				}
				try {
					Thread.sleep(delay);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				if (articles.size() >= max)
					break;
			}
			
			categorySubpage++;
		}

		return articles;
	}

	/** Return URLs of articles collected from given category (sub)page */
	private List<String> getArticleURLs(final String categoryUrl) {
		final List<String> urls = new ArrayList<String>();
		try {
			final Document page = Jsoup.connect(categoryUrl).get();
			final Elements articleLinks = page.select("#contentw h3 > a");
			for (final Element e : articleLinks) {
				final String url = e.attr("href");
				if (!url.isEmpty())
					urls.add(url.trim());		
			}
		} catch (Exception e) {
			System.err.println("Exception when parsing article urls from category page: " + categoryUrl);
			e.printStackTrace();
		}
		return urls;
	}

	private Article getArticle(final String articleUrl, final Category category) {
		final String fetchUrl = makeMobileUrl(articleUrl);
		Article article = null;
		
		if (fetchUrl != null) {
			try {
				final Document page = Jsoup.connect(fetchUrl).get();
				
				//make url
				final String url = articleUrl.startsWith("http://") ? articleUrl : baseURL(category) + articleUrl;
				
				//parse date
				final Elements dateElem = page.select(".pagewrap small");
				Date date = null;
				if (!dateElem.isEmpty())
					date = Util.parseDate(dateElem.first().text().trim());
				if (date == null) {
					date = new Date();
					System.err.println("WARNING: can't parse date (and time). The element was: '" + dateElem.toString() + "'");
				}

				//parse title
				final Elements titleElem = page.select(".pagewrap h1");
				if (titleElem.isEmpty())
					return null;
				final String title = titleElem.first().text().trim();
				
				//parse perex
				final Elements perexElem = page.select(".pagewrap p strong");
				String perex = "";
				if (!perexElem.isEmpty())
					perex = perexElem.first().text().trim();
				
				//parse article text
				final Elements textElem = page.select(".pagewrap p");
				if (textElem.isEmpty())
					return null;
				final String text = page.select(".pagewrap p").text().trim();				
				
				article = new Article(url, date, title, perex, text);
	
			} catch (Exception e) {
				System.err.println("Exception when fetching article from: " + articleUrl + " - " + fetchUrl);
				e.printStackTrace();
			}
		}
		
		return article;
	}
	
	private String makeMobileUrl(final String url) {
		final Matcher matcher = ARTICLE_ID_PATTERN.matcher(url);
		if (matcher.matches() && matcher.groupCount() > 0) {
			return ARTICLE_FETCH_URL + matcher.group(1);
		} else {
			System.err.println("WARNING: article id not found in url: " + url);
			return null;
		}
	}

	private String categoryURL(final Category category) {
		switch (category) {
			case DOMACE:
				return "http://www.sme.sk/rubrika.asp?rub=online_zdom&ref=menu&st=";
			case ZAHRANICNE:
				return "http://www.sme.sk/rubrika.asp?rub=online_zahr&ref=menu&st=";
			case EKONOMIKA_SK:
				return "http://ekonomika.sme.sk/r/ekon_sfsr/slovensko.html?st=";
			case EKONOMIKA_SVET:
				return "http://ekonomika.sme.sk/r/ekon_st/svet.html?st=";
			case KULTURA:
				return "http://kultura.sme.sk/hs/?st=";
			case KOMENTARE:
				return "http://komentare.sme.sk/hs/?st=";
			default:
				throw new RuntimeException("Unknown category: " + category);
			}
	}
	
	private String baseURL(final Category category) {
		final Matcher m = CATEGORY_BASEURL_PATTERN.matcher(categoryURL(category));
		if (m.matches() && m.groupCount() > 0)
			return m.group(1);
		else
			return "";
	}

	/* just for tests */
	public static void main(String[] args) {
		SMEDownloader sme = new SMEDownloader();
		
		long start1 = System.nanoTime();
		List<Article> dom = sme.fetchLast(21, Category.DOMACE, 300);
		long end1 = System.nanoTime();
		for (Article a : dom)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end1 - start1)/1e9 + "s");
		
		System.out.println("******************************************");
		
		long start2 = System.nanoTime();
		List<Article> zah = sme.fetchLast(21, Category.EKONOMIKA_SK, 300);
		long end2 = System.nanoTime();
		for (Article a : zah)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end2 - start2)/1e9 + "s");
	}

}
