package jp.nita.getlyricsmusicextension;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class AsyncLyricsSearcher extends AsyncTask<String, Void, Void> {
	Activity activity;
	final static Handler handler = new Handler();

	AsyncLyricsSearcher(Activity a){
		activity=a;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Void doInBackground(String... params) {
		String artist=URLEncoder.encode(params[1]);
		String title=URLEncoder.encode(params[0]);
		String uri="http://www.kget.jp/search/index.php?c=0&r="+artist+"&t="+title;

		try {
			Document doc0 = Jsoup.connect(uri).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36").get();
			Element res0 = doc0.getElementById("search-result");
			if(res0 == null) throw new AsyncLyricsSearcherNotFoundException();
			Element lyricAnchor = res0.getElementsByClass("lyric-anchor").get(0);
			String lyricsUri = "http://www.kget.jp/"+lyricAnchor.attributes().get("href");

			Document doc1 = Jsoup.connect(lyricsUri).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36").get();
			Element trunk = doc1.getElementById("lyric-trunk");

			String tempLyrics=processHtml(trunk.html());
			final String lyrics = tempLyrics;

			new Thread(new Runnable(){
				@Override
				public void run() {
					handler.post(new Runnable() {
						public void run() {
							((TextView)activity.findViewById(R.id.lyrics)).setText(lyrics);
							((View)activity.findViewById(R.id.progressBar)).setVisibility(View.GONE);
						}
					});
				}
			}).start();
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

	public String processHtml(String html){
		html=html.replace("<br>","\n");
		html=html.replace("<br/>","\n");
		html=html.replace("<br />","\n");
		
		html=html.replace("&amp;","&");
		html=html.replace("&quot;","\"");
		html=html.replace("&lt;","<");
		html=html.replace("&gt;",">");
		
		String regex1 = "<.*?>";
		Pattern p1 = Pattern.compile(regex1);
		Matcher m1 = p1.matcher(html);
		html=m1.replaceAll(" ");
		
		String regex2 = "&.*?;";
		Pattern p2 = Pattern.compile(regex2);
		Matcher m2 = p2.matcher(html);
		html=m2.replaceAll(" ");
		
		html=html.replace("\n ","\n");
		
		return html;
	}

	public class AsyncLyricsSearcherNotFoundException extends Exception{

	}

}
