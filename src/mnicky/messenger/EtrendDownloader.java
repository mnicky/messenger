package mnicky.messenger;

import java.util.List;

public class EtrendDownloader extends ADownloader {

	public static enum Category implements ICategory {

		FIRMY		("http://www.etrend.sk/services/RSSwidget.html?count=50&subtitle=false&name=TREND.sk&target=false&width=300&height=250&sections[]=6#"),
		EKONOMIKA	("http://www.etrend.sk/services/RSSwidget.html?count=50&subtitle=false&name=TREND.sk&target=false&width=300&height=250&sections[]=7#");

		private final String url;

		Category(final String url) {
			this.url = url;
		}

		public String getUrl() {
			return this.url;
		}
	}

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

	/* just for tests */
	public static void main(String[] args) {
		EtrendDownloader downloader = new EtrendDownloader();
		downloader.debugMode = true;

		long start1 = System.nanoTime();
		List<Article> dom = downloader.fetchLast(1, Category.EKONOMIKA, 100);
		long end1 = System.nanoTime();
		for (Article a : dom)
			System.out.println(a);
		System.out.println("Time elapsed: " + (end1 - start1)/1e9 + "s");

//		System.out.println("******************************************");
//
//		long start2 = System.nanoTime();
//		List<Article> zah = downloader.fetchLast(5, Category.FIRMY, 100);
//		long end2 = System.nanoTime();
//		for (Article a : zah)
//			System.out.println(a);
//		System.out.println("Time elapsed: " + (end2 - start2)/1e9 + "s");
	}


}
