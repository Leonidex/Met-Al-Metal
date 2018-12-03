package com.leonidex.rssfeed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RSSFeedParser {
	
	private final static String TAG = "RSSFeedParser";
	
	public RSSFeedParser() {
	}

	public List<String> readRss(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		List<String> items = new ArrayList<>();
		parser.require(XmlPullParser.START_TAG, null, "rss");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("channel")) {
				items.addAll(readChannel(parser));
			} else {
				skip(parser);
			}
		}
		return items;
	}

	private List<String> readChannel(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		List<String> items = new ArrayList<>();
		parser.require(XmlPullParser.START_TAG, null, "channel");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("item")) {
				items.add(readItem(parser));
			} else {
				skip(parser);
			}
		}
		return items;
	}

	private String readItem(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		String result = null;
		parser.require(XmlPullParser.START_TAG, null, "item");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("title")) {
				result = readTitle(parser);
			} else {
				skip(parser);
			}
		}
		return result;
	}

	// Processes title tags in the feed.
	private String readTitle(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, "title");
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, null, "title");
		return title;
	}

	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
