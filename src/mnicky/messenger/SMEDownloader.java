package mnicky.messenger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMEDownloader extends ADownloader {


	public static enum Category implements ICategory {

		DOMACE         ("http://www.sme.sk/rubrika.asp?rub=online_zdom&ref=menu&st="),
		ZAHRANICNE     ("http://www.sme.sk/rubrika.asp?rub=online_zahr&ref=menu&st="),
		EKONOMIKA_SK   ("http://ekonomika.sme.sk/r/ekon_sfsr/slovensko.html?st="),
		EKONOMIKA_SVET ("http://ekonomika.sme.sk/r/ekon_st/svet.html?st=-"),
		KULTURA        ("http://kultura.sme.sk/hs/?st="),
		KOMENTARE      ("http://komentare.sme.sk/hs/?st=");

		private final String url;

		Category(final String url) {
			this.url = url;
		}

		public String getUrl() {
			return this.url;
		}
	}

	private final String ARTICLE_FETCH_URL = "http://s.sme.sk/export/phone/html/?cf=";
	private final Pattern ARTICLE_ID_PATTERN = Pattern.compile("(?:.*sme.sk)?/c/(\\d+).*");

	@Override
	protected String transformArticleURL(final String url) {
		final Matcher matcher = ARTICLE_ID_PATTERN.matcher(url);
		if (matcher.matches() && matcher.groupCount() > 0) {
			return ARTICLE_FETCH_URL + matcher.group(1);
		} else {
			System.err.println("WARNING: article id not found in url: " + url);
			return null;
		}
	}

	@Override
	protected String categoryBaseURLRegexp() {
		return "(http://.*sme.sk)(?:/.*)?";
	}

	@Override
	protected int maxCategorySubpageNumber() {
		return 50;
	}

	@Override
	protected String[] categoryArticleLinkSelectors() {
		final String[] selectors = {"#contentw h3 > a"};
		return selectors;
	}

	@Override
	protected String[] articleDateSelectors() {
		final String[] selectors = {".pagewrap small"};
		return selectors;
	}

	@Override
	protected String[] articleTitleSelectors() {
		final String[] selectors = {".pagewrap h1"};
		return selectors;
	}

	//TODO: some articles doesn't have perex, but it could be obtained from their og:description meta element
	// e.g. this doesn't have perex: http://komentare.sme.sk/c/7195972/oprana-pravda.html
	//		this has perex only in the desktop version (not mobile): http://tech.sme.sk/c/7196840/vedci-narazili-na-velku-slnecnu-erupciu-videli-ju-najlepsie.html
	@Override
	protected String[] articlePerexSelectors() {
		final String[] selectors = {".pagewrap p strong"};
		return selectors;
	}

	@Override
	protected String[] articleTextSelectors() {
		final String[] selectors = {".pagewrap p"};
		return selectors;
	}

	/* just for tests */
	public static void main(String[] args) {
		SMEDownloader downloader = new SMEDownloader();
		downloader.debugMode = true;

		long start1 = System.nanoTime();
		List<Article> dom = downloader.fetchLast(5, Category.KOMENTARE, 300);
		long end1 = System.nanoTime();
		for (Article a : dom)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end1 - start1)/1e9 + "s");

		System.out.println("******************************************");

		long start2 = System.nanoTime();
		List<Article> zah = downloader.fetchLast(5, Category.KULTURA, 300);
		long end2 = System.nanoTime();
		for (Article a : zah)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end2 - start2)/1e9 + "s");
	}

}
