package jp.nita.getlyricsmusicextension;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class AsyncLyricsSearcher extends AsyncTask<String, Void, Void> {
	Activity activity;
	final static Handler handler = new Handler();

	AsyncLyricsSearcher(Activity a){
		activity=a;
	}

	public String get(String uriStr){
		HttpURLConnection http = null;
		InputStream in = null;
		StringBuilder src=new StringBuilder();

		try {
			// URL‚ÉHTTPÚ‘±
			URL url = new URL(uriStr);
			http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36");
			http.connect();
			in = http.getInputStream();
			InputStreamReader isr = new InputStreamReader(in,"UTF-8");
			BufferedReader reader = new BufferedReader(isr);
			String line;
			while ((line = reader.readLine()) != null) {
                src.append(line);
                src.append("\n");
            }
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ExtensionActivity","",e);
			throw new RuntimeException();
		} finally {
			try {
				if (http != null)
					http.disconnect();
				if (in != null)
					in.close();
			} catch (Exception e) {
				Log.e("ExtensionActivity","",e);
				throw new RuntimeException();
			}
		}
		return src.toString();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Void doInBackground(String... params) {
		String artist=URLEncoder.encode(params[1]);
		String title=URLEncoder.encode(params[0]);
		String uri="http://www.kget.jp/search/index.php?c=0&r="+artist+"&t="+title;
		String temp=get(uri);

		String keywordClassLyricAnchor="class=\"lyric-anchor";
		int indexOfClassLyricAnchor=temp.indexOf(keywordClassLyricAnchor);
		String keywordAHref="href=\"";
		int indexOfAHref=temp.indexOf(keywordAHref,
				indexOfClassLyricAnchor+keywordClassLyricAnchor.length()+2);
		String keywordQuote="\"";
		int indexOfQuote=temp.indexOf(keywordQuote,
				indexOfAHref+keywordAHref.length()+2);
		String href=temp.substring(indexOfAHref+keywordAHref.length(),indexOfQuote);

		String uri1="http://www.kget.jp/"+href;
		String lyricsTemp=get(uri1);

		String keywordIdLyricTrunk="id=\"lyric-trunk";
		int indexOfIdLyricTrunk=lyricsTemp.indexOf(keywordIdLyricTrunk);
		String keywordGreaterThan=">";
		int indexOfGreaterThan=lyricsTemp.indexOf(keywordGreaterThan
				,indexOfIdLyricTrunk+keywordIdLyricTrunk.length());
		String keywordCloseDiv="</div>";
		int indexOfCloseDiv=lyricsTemp.indexOf(keywordCloseDiv,
				indexOfGreaterThan+keywordGreaterThan.length()+2);
		String tempLyrics=lyricsTemp.substring(indexOfGreaterThan+keywordGreaterThan.length()+1,indexOfCloseDiv);
		tempLyrics=tempLyrics.replace("<br>","\n");
		tempLyrics=tempLyrics.replace("<br/>","\n");
		tempLyrics=tempLyrics.replace("<br />","\n");
		tempLyrics=NCR.ncr(tempLyrics);


		final String lyrics=tempLyrics;

		new Thread(new Runnable(){
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						((TextView)activity.findViewById(R.id.lyrics)).setText(lyrics);
					}
				});
			}
		}).start();

		return null;
	}

	public static String decode(String str) {
		Pattern pattern = Pattern.compile("&#(\\d+);|&#([\\da-fA-F]+);");
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		Character buf;
		while(matcher.find()){
			if(matcher.group(1) != null){
				buf = new Character(
						(char)Integer.parseInt(matcher.group(1)));
			}else{
				buf = new Character(
						(char)Integer.parseInt(matcher.group(2), 16));
			}
			matcher.appendReplacement(sb, buf.toString());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public static class NCR {
		private static final Pattern P = Pattern.compile(
				"&#(x([0-9a-f]+)|([0-9]+));",
				Pattern.CASE_INSENSITIVE
				);

		private static boolean isHex(final String str) {
			final char x = str.charAt(0);
			return 'x' == x || 'X' == x;
		}

		public static String ncr(final String str) {
			final StringBuffer rtn = new StringBuffer();
			final Matcher matcher = P.matcher(str);
			while (matcher.find()) {
				final String group = matcher.group(1);
				int parseInt;
				if (isHex(group)) {
					parseInt = Integer.parseInt(group.substring(1), 16);
				} else {
					parseInt = Integer.parseInt(group, 10);
				}

				final char c;
				if (0 != (0x0ffff & parseInt)) {
					c = (char) parseInt;
				} else {
					c = '?';
				}
				matcher.appendReplacement(rtn, Character.toString(c));
			}
			matcher.appendTail(rtn);

			return rtn.toString();
		}
	}

}
