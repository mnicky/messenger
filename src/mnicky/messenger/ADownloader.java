package mnicky.messenger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
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

	protected Pattern categoryBaseURLPattern = null;

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

		int categorySubpage = 1;
		while (articles.size() < max && categorySubpage <= maxCategorySubpageNumber()) {

			//get article urls from the next category subpage
			final List<String> articleUrls = getArticleURLs(categoryUrl + categorySubpage);
			Util.sleep(delay);

			//fetch articles
			for (final String url : articleUrls) {
				final Article article = getArticle(url, category);
				if (article != null) {
					articles.add(article);
				}
				else {
					System.err.println("WARNING: can't fetch or parse article from url: " + url);
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
				for (final Element e : articleLinks) {
					final String url = e.attr("href").trim();
					if (!url.isEmpty())
						urls.add(url.startsWith("http://") ? url : baseURL(categoryUrl) + url);
				}
			}
		} catch (Exception e) {
			System.err.println("Exception when parsing article urls from category page: " + categoryUrl);
			e.printStackTrace();
		}
		return urls;
	}

	/** Returns parsed article or null if can't parse given url. */
	private Article getArticle(final String articleUrl, final ICategory category) {
		final String fetchUrl = transformArticleURL(articleUrl);
		Article article = null;
		final Document page = getPage(fetchUrl);

		if (fetchUrl != null && page != null) {
			try {

				//make url
				final String url = articleUrl.startsWith("http://") ? articleUrl : baseURL(category) + articleUrl;

				//parse date
				final Elements dateElem = ADownloader.getElements(page, articleDateSelectors());
				Date date = null;
				if (!dateElem.isEmpty())
					date = Util.parseDate(dateElem.first().text().trim());
				if (date == null) {
					date = new Date();
					System.err.println("WARNING: can't parse date (and time). The element was: '" + dateElem.toString() + "'");
				}

				//parse title
				final Elements titleElem = ADownloader.getElements(page, articleTitleSelectors());
				if (titleElem.isEmpty())
					return null;
				final String title = titleElem.first().text().trim();

				//parse perex
				final Elements perexElem = ADownloader.getElements(page, articlePerexSelectors());
				String perex = "";
				if (!perexElem.isEmpty())
					perex = perexElem.first().text().trim();

				//parse article text
				final Elements textElem = ADownloader.getElements(page, articleTextSelectors());
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

	protected Document getPage(final String URL) {
		Document doc = null;
		Connection conn = Jsoup.connect(URL);
		if (userAgent() != null)
			conn = conn.userAgent(userAgent());
		try {
			doc = conn.timeout(15000).get();
		} catch (IOException e) {
			System.err.println("Exception when fetching from URL: " + URL);
			e.printStackTrace();
		}
		return doc;
	}

	protected String userAgent() {
		return null;
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
