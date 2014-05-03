package mnicky.messenger;

import java.util.List;

public class HNDownloader extends ADownloader {
	
	public static enum Category implements ICategory {
		
		SLOVENSKO    ("http://hn.hnonline.sk/slovensko-119?page="),
		SVET      ("http://hn.hnonline.sk/svet-120?page=0"),
		EKONOMIKA ("http://hn.hnonline.sk/ekonomika-a-firmy-117?page="),
		FINANCIE   ("http://finweb.hnonline.sk/spravy-zo-sveta-financii-126?page=");
		
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
		return "(http://.*hnonline.sk)(?:/.*)?";
	}
	
	@Override
	protected int maxCategorySubpageNumber() {
		return 300; //up to 2000-6000 available in fact
	}
	
	@Override
	protected String[] categoryArticleLinkSelectors() {
		final String[] selectors = {".titulok h3 a", "titulok h2 a"};
		return selectors;
	}
	
	@Override
	protected String[] articleDateSelectors() {
		final String[] selectors = {".article-info-basic .date-display-single"};
		return selectors;
	}
	
	@Override
	protected String[] articleTitleSelectors() {
		final String[] selectors = {"#page-node-title h1"};
		return selectors;
	}
	
	@Override
	protected String[] articlePerexSelectors() {
		final String[] selectors = {".perex p"};
		return selectors;
	}
	
	@Override
	protected String[] articleTextSelectors() {
		final String[] selectors = {".content .field-items p"};
		return selectors;
	}

	/* just for tests */
	public static void main(String[] args) {
		HNDownloader pravda = new HNDownloader();
		
		long start1 = System.nanoTime();
		List<Article> dom = pravda.fetchLast(2, Category.SLOVENSKO, 300);
		long end1 = System.nanoTime();
		for (Article a : dom)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end1 - start1)/1e9 + "s");
		
		System.out.println("******************************************");
		
		long start2 = System.nanoTime();
		List<Article> zah = pravda.fetchLast(2, Category.EKONOMIKA, 300);
		long end2 = System.nanoTime();
		for (Article a : zah)
			System.out.println(a);
		System.out.println("Time elapsed: " + (float)(end2 - start2)/1e9 + "s");
	}

	
}
