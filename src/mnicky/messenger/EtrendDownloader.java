package mnicky.messenger;

import java.util.ArrayList;
import java.util.List;

public class EtrendDownloader extends ADownloader {

	public static enum Category implements ICategory {

		FIRMY		("http://www.etrend.sk/services/RSSwidget.html?count=COUNT&subtitle=false&name=TREND.sk&target=false&width=300&height=250&sections[]=6#"),
		EKONOMIKA	("http://www.etrend.sk/services/RSSwidget.html?count=COUNT&subtitle=false&name=TREND.sk&target=false&width=300&height=250&sections[]=7#");

		private final String url;

		Category(final String url) {
			this.url = url;
		}

		public String getUrl() {
			return this.url;
		}
	}

	protected static double ARTICLE_COUNT_RESERVE = 1.2;

	@Override
	protected String categoryBaseURLRegexp() {
		return "(http://.*etrend.sk)(?:/.*)?";
	}

	@Override
	protected int maxCategorySubpageNumber() {
		return 1; //up to 1000s of articles available on the widget page
	}

	@Override
	protected String[] categoryArticleLinkSelectors() {
		final String[] selectors = {".articles .article a"};
		return selectors;
	}

	@Override
	protected String[] articleDateSelectors() {
		final String[] selectors = {".article-detail .infoline"};
		return selectors;
	}

	@Override
	protected String[] articleTitleSelectors() {
		final String[] selectors = {".article-detail h1"};
		return selectors;
	}

	@Override
	protected String[] articlePerexSelectors() {
		final String[] selectors = {".article-detail .perex"};
		return selectors;
	}

	@Override
	protected String[] articleTextSelectors() {
		final String[] selectors = {".article-detail p.bodytext"};
		return selectors;
	}



	/** Download last 'n' articles from given category.
	 *
	 * @param n how many articles to download
	 * @param category category to get articles from
	 * @param delay how much miliseconds to wait after every article download
	 * @return list of downloaded Articles
	 */
	@Override
	public List<Article> fetchLast(final int n, final ICategory category, final int delay) {
		final List<Article> articles = new ArrayList<Article>();
		final int max = n;
		final String categoryUrl = category.getUrl();

		//get article urls from the widget page
		List<String> articleUrls = new ArrayList<String>();
		try {
			articleUrls = getArticleURLs(categoryUrl.replace("COUNT", Integer.valueOf((int)(max*ARTICLE_COUNT_RESERVE+5)).toString()));
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

		return articles;
	}

	/* just for tests */
	public static void main(String[] args) {
		EtrendDownloader downloader = new EtrendDownloader();
		downloader.debugMode = true;

		long start1 = System.nanoTime();
		List<Article> as1 = downloader.fetchLast(10, Category.EKONOMIKA, 100);
		long end1 = System.nanoTime();
		for (Article a : as1)
			System.out.println(a);
		System.out.println("Time elapsed: " + (end1 - start1)/1e9 + "s");

		System.out.println("******************************************");

		long start2 = System.nanoTime();
		List<Article> as2 = downloader.fetchLast(10, Category.FIRMY, 100);
		long end2 = System.nanoTime();
		for (Article a : as2)
			System.out.println(a);
		System.out.println("Time elapsed: " + (end2 - start2)/1e9 + "s");
	}


}
