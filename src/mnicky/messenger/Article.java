package mnicky.messenger;


public class Article {
	final String url;
	//TODO: final Date date;
	
	final String title;
	final String perex;
	final String text;
	
	public Article(final String url, /* final Date date,*/ final String title, final String perex, final String text) {
		this.url = url;
		//this.date = date;
		
		this.title = title;
		this.perex = perex;
		this.text = text;
	}

	@Override
	public String toString() {
		return "Article [\n"
				+ "  url=" + url + "\n"
				+ "  title=" + title + "\n"
				+ "  perex=" + perex + "\n"
				+ "  text=" + text + "\n"
				+ "]\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Article other = (Article) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
}
