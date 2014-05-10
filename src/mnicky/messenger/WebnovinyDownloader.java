package mnicky.messenger;

import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebnovinyDownloader extends ADownloader {

	public static enum Category implements ICategory {

		SLOVENSKO ("http://www.webnoviny.sk/slovensko/clanky/14/stranka-"),
		SVET      ("http://www.webnoviny.sk/svet/clanky/15/stranka-"),
		EKONOMIKA ("http://www.webnoviny.sk/ekonomika/clanky/16/stranka-"),
		KULTURA   ("http://www.webnoviny.sk/kultura/clanky/6/stranka-");

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
		return "(http://.*webnoviny.sk)(?:/.*)?";
	}

	@Override
	protected int maxCategorySubpageNumber() {
		return 300; //up to 1500-9000 available in fact
	}

	@Override
	protected String[] categoryArticleLinkSelectors() {
		final String[] selectors = {".box-links a.title"};
		return selectors;
	}

	@Override
	protected String[] articleDateSelectors() {
		final String[] selectors = {".article .info"};
		return selectors;
	}

	@Override
	protected String[] articleTitleSelectors() {
		final String[] selectors = {".article h1"};
		return selectors;
	}

	@Override
	protected String[] articlePerexSelectors() {
		final String[] selectors = {"meta[name=description]"};
		return selectors;
	}

	/** Returns null on error. */
	@Override
	protected String parseArticlePerex(final Document page) {
		final Elements perexElem = WebnovinyDownloader.getElements(page, articlePerexSelectors());
		if (perexElem.isEmpty()) {
			return null;
		}
		return perexElem.first().attr("content").trim();
	}

	@Override
	protected String[] articleTextSelectors() {
		final String[] selectors = {".article .obsahclanku"};
		return selectors;
	}

	/* just for tests */
	public static void main(String[] args) {
		WebnovinyDownloader downloader = new WebnovinyDownloader();
		downloader.debugMode = true;

		long start1 = System.nanoTime();
		List<Article> dom = downloader.fetchLast(12, Category.SVET, 100);
		long end1 = System.nanoTime();
		for (Article a : dom)
			System.out.println(a);
		System.out.println("Time elapsed: " + (end1 - start1)/1e9 + "s");

		System.out.println("******************************************");

		long start2 = System.nanoTime();
		List<Article> zah = downloader.fetchLast(12, Category.KULTURA, 100);
		long end2 = System.nanoTime();
		for (Article a : zah)
			System.out.println(a);
		System.out.println("Time elapsed: " + (end2 - start2)/1e9 + "s");
	}


}
