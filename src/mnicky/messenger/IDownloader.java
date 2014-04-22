package mnicky.messenger;

import java.util.List;

public interface IDownloader {
	List<Article> fetchLast(int n, ICategory category, int delay);
}
