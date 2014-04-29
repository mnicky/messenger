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

public class PravdaDownloader implements IDownloader {

	public static enum Category implements ICategory {
		DOMACE, SVET, EKONOMIKA, REGIONY, KULTURA, NAZORY
	}

	//TODO: use desktop useragent? 
	//private final String USERAGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36";
	private final Pattern CATEGORY_BASEURL_PATTERN = Pattern.compile("(http://.*pravda.sk/)(?:.*)?");
	private final int MAX_CATEGORY_SUBPAGE = 300;

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
		while (articles.size() < max && categorySubpage <= MAX_CATEGORY_SUBPAGE) {
			
			//get article urls from the next category subpage
			final List<String> articleUrls = getArticleURLs(categoryUrl + categorySubpage);
			
			//fetch articles
			for (final String url : articleUrls) {
				final Article article = getArticle(url, (Category)category);
				if (article != null) {
					articles.add(article);
				}
				else {
					System.err.println("WARNING: can't fetch or parse article from url: " + url);
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
			//FIXME: getElements() doesn't help here, because the first selector already exists, just doesn't contain everything
			final Elements articleLinks = Util.getElements(page, ".rubrikovy_nahlad_clanku h3 a.nadpis_nahlad_clanku", ".rubrikovy_nahlad_clanku_top h2 a");
			for (final Element e : articleLinks) {
				final String url = e.attr("href").trim();
				if (!url.isEmpty())
					urls.add(url.startsWith("http://") ? url : baseURL(categoryUrl) + url);		
			}
		} catch (Exception e) {
			System.err.println("Exception when parsing article urls from category page: " + categoryUrl);
			e.printStackTrace();
		}
		return urls;
	}

	/** Returns parsed article or null if can't parse given url. */
	private Article getArticle(final String articleUrl, final Category category) {
		final String fetchUrl = articleUrl;
		Article article = null;
		
		if (fetchUrl != null) {
			try {
				final Document page = Jsoup.connect(fetchUrl).get();
				
				//make url
				final String url = articleUrl.startsWith("http://") ? articleUrl : baseURL(category) + articleUrl;
				
				//parse date
				final Elements dateElem = Util.getElements(page, ".article-metadata .article-datetime");
				Date date = null;
				if (!dateElem.isEmpty())
					date = Util.parseDate(dateElem.first().text().trim());
				if (date == null) {
					date = new Date();
					System.err.println("WARNING: can't parse date (and time). The element was: '" + dateElem.toString() + "'");
				}
	
				//parse title
				final Elements titleElem = Util.getElements(page, ".content_case h1");
				if (titleElem.isEmpty())
					return null;
				final String title = titleElem.first().text().trim();
				
				//parse perex
				final Elements perexElem = Util.getElements(page, ".content_case .article-perex");
				String perex = "";
				if (!perexElem.isEmpty())
					perex = perexElem.first().text().trim();
				
				//parse article text
				final Elements textElem = Util.getElements(page, ".content_case .pokracovanie_clanku p");
				if (textElem.isEmpty())
					return null;
				final String text = textElem.text().trim();				
				
				article = new Article(url, date, title, perex, text);
	
			} catch (Exception e) {
				System.err.println("Exception when fetching article from: " + articleUrl + " - " + fetchUrl);
				e.printStackTrace();
			}
		}
		
		return article;
	}

	private String categoryURL(final Category category) {
		switch (category) {
			case DOMACE:
				return "http://spravy.pravda.sk/domace/strana-";
			case SVET:
				return "http://spravy.pravda.sk/svet/strana-";
			case EKONOMIKA:
				return "http://spravy.pravda.sk/ekonomika/strana-";
			case REGIONY:
				return "http://spravy.pravda.sk/regiony/strana-";
			case KULTURA:
				return "http://kultura.pravda.sk/strana-";
			case NAZORY:
				return "http://nazory.pravda.sk/strana-";
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
	
	//TODO: refactor into one function
	private String baseURL(final String categoryUrl) {
		final Matcher m = CATEGORY_BASEURL_PATTERN.matcher(categoryUrl);
		if (m.matches() && m.groupCount() > 0)
			return m.group(1);
		else
			return "";
	}

	/* just for tests */
	public static void main(String[] args) {
		PravdaDownloader sme = new PravdaDownloader();
		
		long start1 = System.nanoTime();
		List<Article> dom = sme.fetchLast(51, Category.KULTURA, 100);
		long end1 = System.nanoTime();
		for (Article a : dom)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end1 - start1)/1e9 + "s");
		
		System.out.println("******************************************");
		
		long start2 = System.nanoTime();
		List<Article> zah = sme.fetchLast(51, Category.NAZORY, 100);
		long end2 = System.nanoTime();
		for (Article a : zah)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end2 - start2)/1e9 + "s");
	}

}
