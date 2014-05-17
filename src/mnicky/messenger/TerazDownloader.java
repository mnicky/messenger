package mnicky.messenger;

import java.util.List;

public class TerazDownloader extends ADownloader {

	public static enum Category implements ICategory {

		SLOVENSKO	("http://www.teraz.sk/slovensko?page="),
		ZAHRANICIE	("http://www.teraz.sk/zahranicie?page="),
		EKONOMIKA	("http://www.teraz.sk/ekonomika?page="),
		REGIONY		("http://www.teraz.sk/regiony?page="),
		KULTURA		("http://www.teraz.sk/kultura?page="),
		SPORT		("http://www.teraz.sk/sport?page=");

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
		return "(http://.*teraz.sk)(?:/.*)?";
	}

	@Override
	protected int maxCategorySubpageNumber() {
		return 100; //up to 500-800 available in fact
	}

	@Override
	protected String[] categoryArticleLinkSelectors() {
		//TODO: use AND instead of OR
		final String[] selectors = {".articles .articlel a", ".mainarticle h2", ".otherarticles h3"};
		return selectors;
	}

	@Override
	protected String[] articleDateSelectors() {
		final String[] selectors = {".content .datum"};
		return selectors;
	}

	@Override
	protected String[] articleTitleSelectors() {
		final String[] selectors = {".content h1"};
		return selectors;
	}

	@Override
	protected String[] articlePerexSelectors() {
		final String[] selectors = {".content .teaser"};
		return selectors;
	}

	@Override
	protected String[] articleTextSelectors() {
		final String[] selectors = {".content p"};
		return selectors;
	}

	/* just for tests */
	public static void main(String[] args) {
		TerazDownloader downloader = new TerazDownloader();
		downloader.debugMode = true;

		long start1 = System.nanoTime();
		List<Article> dom = downloader.fetchLast(15, Category.SLOVENSKO, 100);
		long end1 = System.nanoTime();
		for (Article a : dom)
			System.out.println(a);
		System.out.println("Time elapsed: " + (end1 - start1)/1e9 + "s");

//		System.out.println("******************************************");
//
//		long start2 = System.nanoTime();
//		List<Article> zah = downloader.fetchLast(5, Category.EKONOMIKA, 100);
//		long end2 = System.nanoTime();
//		for (Article a : zah)
//			System.out.println(a);
//		System.out.println("Time elapsed: " + (end2 - start2)/1e9 + "s");
	}


}
