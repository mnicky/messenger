package mnicky.messenger;

import java.util.List;

//TODO: change to abstract class and provide methods
public interface IDownloader {
	List<Article> fetchLast(int n, ICategory category, int delay);
}
