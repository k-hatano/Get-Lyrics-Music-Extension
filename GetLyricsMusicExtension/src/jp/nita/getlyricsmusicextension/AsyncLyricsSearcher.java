
package jp.nita.getlyricsmusicextension;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class AsyncLyricsSearcher extends AsyncTask<String, Void, Void> {
	Activity activity;
	final static Handler handler = new Handler();
	
	public static List<TrackInfo> lastResult=new ArrayList<TrackInfo>();

	AsyncLyricsSearcher(Activity a){
		activity=a;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Void doInBackground(String... params) {
		new Thread(new Runnable(){
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						((TextView)activity.findViewById(R.id.lyrics)).setText(activity.getString(R.string.searching));
						((View)activity.findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
					}
				});
			}
		}).start();
		
		String artist=URLEncoder.encode(params[1]);
		String title=URLEncoder.encode(params[0]);
		String uri="http://www.kget.jp/search/index.php?c=0&r="+artist+"&t="+title;

		try {
			Document doc0 = Jsoup.connect(uri).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36").get();
			Element res0 = doc0.getElementById("search-result");
			if(res0 == null) throw new AsyncLyricsSearcherNotFoundException();
			lastResult=new ArrayList<TrackInfo>();
			Elements lyricAnchors = res0.getElementsByClass("lyric-anchor");
			for(int i=0;i<lyricAnchors.size();i++){
				Element anchor = lyricAnchors.get(i);
				String t = anchor.getElementsByClass("title").get(0).text();
				String b = anchor.attributes().get("href");
				String a = "";
				lastResult.add(new TrackInfo(t,a,b));
			}
			
			AsyncLyricsGetter getter = new AsyncLyricsGetter(activity,handler);
			getter.execute(lastResult.get(0).title,lastResult.get(0).anchor);
		} catch (AsyncLyricsSearcherNotFoundException e) {
			new Thread(new Runnable(){
				@Override
				public void run() {
					handler.post(new Runnable() {
						public void run() {
							((TextView)activity.findViewById(R.id.lyrics)).setText(activity.getString(R.string.not_found));
							((View)activity.findViewById(R.id.progressBar)).setVisibility(View.GONE);
						}
					});
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();

			new Thread(new Runnable(){
				@Override
				public void run() {
					handler.post(new Runnable() {
						public void run() {
							((TextView)activity.findViewById(R.id.lyrics)).setText(activity.getString(R.string.failed));
							((View)activity.findViewById(R.id.progressBar)).setVisibility(View.GONE);
						}
					});
				}
			}).start();
		}

		return null;
	}

	public class AsyncLyricsSearcherNotFoundException extends Exception{

	}

}
