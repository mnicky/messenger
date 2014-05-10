package mnicky.messenger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class ADownloader {

	abstract protected String categoryBaseURLRegexp();
	abstract protected int maxCategorySubpageNumber();

	abstract protected String[] categoryArticleLinkSelectors();
	abstract protected String[] articleDateSelectors();
	abstract protected String[] articleTitleSelectors();
	abstract protected String[] articlePerexSelectors();
	abstract protected String[] articleTextSelectors();

	protected boolean debugMode = false;
	protected Pattern categoryBaseURLPattern = null;

	protected Map<String,String> sessionCookies = null;

	/** Download last 'n' articles from given category.
	 *
	 * @param n how many articles to download
	 * @param category category to get articles from
	 * @param delay how much miliseconds to wait after every article download
	 * @return list of downloaded Articles
	 */
	public List<Article> fetchLast(final int n, final ICategory category, final int delay) {
		final List<Article> articles = new ArrayList<Article>();
		final int max = n;
		final String categoryUrl = category.getUrl();

		int categorySubpage = firstCategorySubpageNumber();
		while (articles.size() < max && categorySubpage <= maxCategorySubpageNumber()) {


			//get article urls from the next category subpage
			List<String> articleUrls = new ArrayList<String>();
			try {
				articleUrls = getArticleURLs(categoryUrl + categorySubpage);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			Util.sleep(delay);

			//fetch articles
			for (final String url : articleUrls) {
				Article article = null;
				try {
					article = getArticle(url, category);
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
				if (article != null) {
					articles.add(article);
				}
				else {
					if (debugMode)
						System.err.println("[ERROR] Can't fetch or parse article from url: " + url);
				}
				Util.sleep(delay);
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
			final Document page = getPage(categoryUrl);
			if (page != null) {
				//FIXME: getElements() doesn't help here, because the first selector already exists, just doesn't contain everything
				final Elements articleLinks = ADownloader.getElements(page, categoryArticleLinkSelectors());
				if (articleLinks.isEmpty()) {
					if (debugMode)
						System.err.println("[WARN] Can't find article links.");
				}
				for (final Element e : articleLinks) {
					final String url = e.attr("href").trim();
					if (!url.isEmpty())
						urls.add(url.startsWith("http://") ? url : baseURL(categoryUrl) + url);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception when parsing article urls from category page: " + categoryUrl, e);
		}
		return urls;
	}

	/** Returns parsed article or null if can't parse given url. */
	private Article getArticle(final String articleUrl, final ICategory category) {
		final String fetchUrl = transformArticleURL(articleUrl);
		Article article = null;

		try {
			final Document page = getPage(fetchUrl);

			if (fetchUrl != null && page != null) {

				// check for paid content etc.
				if (skipArticle(page)) {
					if (debugMode)
						System.out.println("[INFO] Skipping article "
								+ articleUrl);
					return null;
				}

				final String url = articleUrl.startsWith("http://") ? articleUrl : baseURL(category) + articleUrl;

				final String dateString = parseArticleDate(page);
				Date date = null;
				if (dateString == null && debugMode) {
					System.err.println("[ERROR] Can't find date.");
					date = new Date();
				}
				else {
					date = Util.parseDate(parseArticleDate(page));
					if (date == null && debugMode) {
						System.err.println("[ERROR] Can't parse date. Found element was: '" + dateString + "'");
						date = new Date();
					}
				}

				final String title = parseArticleTitle(page);
				if (title == null && debugMode)
					System.err.println("[ERROR] Can't find title.");

				String perex = parseArticlePerex(page);
				if (perex == null) {
					if (debugMode)
						System.err.println("[ERROR] Can't find perex.");
					perex = "";
				}

				final String text = parseArticleText(page);
				if (text == null && debugMode)
					System.err.println("[ERROR] Can't find text.");

				if (url == null || date == null || title == null || perex == null || text == null || text.length() < minTextSize())
					return null;

				//TODO: postCheck() method? (for e.g. minimal number of chars in text, obligatory/voluntary perex etc...)
				article = new Article(url, date, title, perex, text);
			}

		} catch (Exception e) {
			throw new RuntimeException("Exception when fetching article from: "
					+ articleUrl + " - " + fetchUrl, e);
		}

		return article;
	}

	/** Returns null on error. */
	protected String parseArticleText(final Document page) {
		final Elements textElem = ADownloader.getElements(page, articleTextSelectors());
		if (textElem.isEmpty()) {
			return null;
		}
		return textElem.text().trim();
	}

	/** Returns null on error. */
	protected String parseArticlePerex(final Document page) {
		final Elements perexElem = ADownloader.getElements(page, articlePerexSelectors());
		if (perexElem.isEmpty()) {
			return null;
		}
		return perexElem.first().text().trim();
	}

	/** Returns null on error. */
	protected String parseArticleTitle(final Document page) {
		final Elements titleElem = ADownloader.getElements(page, articleTitleSelectors());
		if (titleElem.isEmpty()) {
			return null;
		}
		return titleElem.first().text().trim();
	}

	/** Returns null on error. */
	protected String parseArticleDate(final Document page) {
		final Elements dateElem = ADownloader.getElements(page, articleDateSelectors());
		if (dateElem.isEmpty()) {
			return null;
		}
		return dateElem.first().text().trim();
	}

	protected Document getPage(final String URL) {

		//handle session cookies
		if (sessionCookies == null) {
			try {
				if (debugMode)
					System.out.println("[INFO] Getting session cookies from " + URL);
				Response response = Jsoup.connect(URL).method(Connection.Method.GET).execute();
				sessionCookies = response.cookies();
			} catch (IOException e) {
				throw new RuntimeException("Exception when getting session cookies from URL: " + URL, e);
			}
		}

		Document doc = null;
		Connection conn = Jsoup.connect(URL);
		if (userAgent() != null)
			conn = conn.userAgent(userAgent());
		if (sessionCookies != null)
			conn = conn.cookies(sessionCookies);
		if (constantCookies() != null)
			conn = conn.cookies(constantCookies());
		try {
			if (debugMode)
				System.out.println("[INFO] Connecting to " + URL);
			doc = conn.timeout(15000).get();
		} catch (IOException e) {
			throw new RuntimeException("Exception when fetching from URL: " + URL, e);
		}
		return doc;
	}

	protected String userAgent() {
		return null;
	}

	protected Map<String,String> constantCookies() {
		return null;
	}

	protected int firstCategorySubpageNumber() {
		return 1;
	}

	protected int minTextSize() {
		return 30;
	}

	protected boolean skipArticle(final Document doc) {
		return false;
	}

	protected String transformArticleURL(final String articleUrl) {
		return articleUrl;
	}
	private String baseURL(final ICategory category) {
		return baseURL(category.getUrl());
	}

	//TODO: refactor into one function
	private String baseURL(final String categoryUrl) {

		if (categoryBaseURLPattern == null)
			categoryBaseURLPattern = Pattern.compile(categoryBaseURLRegexp());

		final Matcher m = categoryBaseURLPattern.matcher(categoryUrl);
		if (m.matches() && m.groupCount() > 0)
			return m.group(1);
		else
			return "";
	}

	//TODO: add AND functionality? (this provides OR). think of proper (composable) abstraction
	/** Returns parsed elements (tries all selectors in given order) or null if can't find any of given selectors. */
	public static Elements getElements(final Document doc, final String... selectorsToTry) {
		Elements elements = new Elements();
		for (int i = 0; i < selectorsToTry.length; i++) {
			elements = doc.select(selectorsToTry[i]);
			if (!elements.isEmpty()) {
				break;
			}
			//TODO: add prints in debug mode
		}
		return elements;
	}

}
