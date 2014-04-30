package mnicky.messenger;

import java.util.List;

public class PravdaDownloader extends ADownloader {
	
	public static enum Category implements ICategory {
		
		DOMACE    ("http://spravy.pravda.sk/domace/strana-"),
		SVET      ("http://spravy.pravda.sk/svet/strana-"),
		EKONOMIKA ("http://spravy.pravda.sk/ekonomika/strana-"),
		REGIONY   ("http://spravy.pravda.sk/regiony/strana-"),
		KULTURA   ("http://kultura.pravda.sk/strana-"),
		NAZORY    ("http://nazory.pravda.sk/strana-");
		
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
		return "(http://.*pravda.sk/)(?:.*)?";
	}
	
	@Override
	protected int maxCategorySubpageNumber() {
		return 300;
	}
	
	@Override
	protected String[] categoryArticleLinkSelectors() {
		final String[] selectors = {".rubrikovy_nahlad_clanku h3 a.nadpis_nahlad_clanku", ".rubrikovy_nahlad_clanku_top h2 a"};
		return selectors;
	}
	
	@Override
	protected String[] articleDateSelectors() {
		final String[] selectors = {".article-metadata .article-datetime"};
		return selectors;
	}
	
	@Override
	protected String[] articleTitleSelectors() {
		final String[] selectors = {".content_case h1"};
		return selectors;
	}
	
	@Override
	protected String[] articlePerexSelectors() {
		final String[] selectors = {".content_case .article-perex"};
		return selectors;
	}
	
	@Override
	protected String[] articleTextSelectors() {
		final String[] selectors = {".content_case .pokracovanie_clanku p"};
		return selectors;
	}

	/* just for tests */
	public static void main(String[] args) {
		PravdaDownloader pravda = new PravdaDownloader();
		
		long start1 = System.nanoTime();
		List<Article> dom = pravda.fetchLast(5, Category.KULTURA, 100);
		long end1 = System.nanoTime();
		for (Article a : dom)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end1 - start1)/1e9 + "s");
		
		System.out.println("******************************************");
		
		long start2 = System.nanoTime();
		List<Article> zah = pravda.fetchLast(5, Category.NAZORY, 100);
		long end2 = System.nanoTime();
		for (Article a : zah)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end2 - start2)/1e9 + "s");
	}

	
}
