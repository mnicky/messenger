package mnicky.messenger;

import java.util.List;

public interface IDownloader extends Iterable<Article> {
	List<Article> fetchLast(int n, ICategory category, int delay);
}
