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

public abstract class Downloader implements IDownloader {

	public static enum Category implements ICategory {
		DOMACE, SVET, EKONOMIKA, REGIONY, KULTURA, NAZORY
	}

	//TODO: use desktop useragent? 
	//private final String USERAGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36";
	protected final Pattern CATEGORY_BASEURL_PATTERN = Pattern.compile("(http://.*pravda.sk/)(?:.*)?");
	protected final int MAX_CATEGORY_SUBPAGE = 300;
	
	protected final String[] CATEGORY_ARTICLE_LINK_SELECTORS = {".rubrikovy_nahlad_clanku h3 a.nadpis_nahlad_clanku", ".rubrikovy_nahlad_clanku_top h2 a"};
	protected static final String[] ARTICLE_DATE_SELECTORS = {".article-metadata .article-datetime"};
	protected static final String[] ARTICLE_TEXT_SELECTORS = {".content_case .pokracovanie_clanku p"};
	protected static final String[] ARTICLE_PEREX_SELECTORS = {".content_case .article-perex"};
	protected static final String[] ARTICLE_TITLE_SELECTORS = {".content_case h1"};

	/** Download last 'n' articles from given category.
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
			final Elements articleLinks = Util.getElements(page, CATEGORY_ARTICLE_LINK_SELECTORS);
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
				final Elements dateElem = Util.getElements(page, ARTICLE_DATE_SELECTORS);
				Date date = null;
				if (!dateElem.isEmpty())
					date = Util.parseDate(dateElem.first().text().trim());
				if (date == null) {
					date = new Date();
					System.err.println("WARNING: can't parse date (and time). The element was: '" + dateElem.toString() + "'");
				}
	
				//parse title
				final Elements titleElem = Util.getElements(page, ARTICLE_TITLE_SELECTORS);
				if (titleElem.isEmpty())
					return null;
				final String title = titleElem.first().text().trim();
				
				//parse perex
				final Elements perexElem = Util.getElements(page, ARTICLE_PEREX_SELECTORS);
				String perex = "";
				if (!perexElem.isEmpty())
					perex = perexElem.first().text().trim();
				
				//parse article text
				final Elements textElem = Util.getElements(page, ARTICLE_TEXT_SELECTORS);
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

	/**
	 * Must return base url for the given category. //TODO: make more clear: The url must have the id of the category subpage as its last element and it can't be present.
	 * @param category
	 * @return
	 */
	abstract protected String categoryURL(final Category category);
	
	private String baseURL(final Category category) {
		final Matcher m = CATEGORY_BASEURL_PATTERN.matcher(categoryURL(category));
		if (m.matches() && m.groupCount() > 0)
			return m.group(1);
		else
			return "";
	}
	
	//TODO: abstract public ICategory getCategories() ? Should it return Enum or List or... ??
	
	//TODO: refactor into one function
	private String baseURL(final String categoryUrl) {
		final Matcher m = CATEGORY_BASEURL_PATTERN.matcher(categoryUrl);
		if (m.matches() && m.groupCount() > 0)
			return m.group(1);
		else
			return "";
	}

}
