package com.leonidex.xmlreader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import android.os.Environment;
import android.util.Log;

import com.leonidex.db.Item;

public class xmlParser {

	private Document doc;
	private ArrayList<Item> itemsList;

	public xmlParser() {
		try {
			this.doc = parse();
			this.itemsList = new ArrayList<Item>();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private Document parse() throws DocumentException {
		
//		File sdcard = Environment.getExternalStorageDirectory();

//		File file = new File(sdcard, "LongRssExample.xml");
//		if(file.exists()) {
			SAXReader reader = new SAXReader();
			Log.d("xmlParser", "Getting document");
			Document document = reader.read("http://www.metalmetal.co.il/?cat=1&feed=rss2");//file);//"http://leonidex.byethost11.com/LongRSSExample.xml");//"http://www.metalmetal.co.il/category/episodes/app");	// TODO: does this address still return empty?
			return document;
//		} else {
//			throw new DocumentException("File not found");
//		}
	}

	public void iterate() {
		Element root = doc.getRootElement();

		// Get channel element from root element
		Iterator rootIter = root.elementIterator();
		Element channel = (Element) rootIter.next();

		for (Iterator channelIter = channel.elementIterator("item"); channelIter
				.hasNext();) {
			Element element = (Element) channelIter.next();
			Log.d("xmlParser", element.toString());

			iterateItem(element);
		}
	}

	private void iterateItem(Element itemRoot) {

		Item item = new Item();

		item.setTitle(getElement(itemRoot, "title"));
		item.setIcastLink(getElement(itemRoot, "enclosure"));
		
		String dateString = DateParser.parseDate(getElement(itemRoot, "pubDate"));
		item.setDate(dateString);
		Log.d("xmlParser", dateString);

		String content = getElement(itemRoot, "encoded");
		parseContent(content, item);

		itemsList.add(item);
	}

	private String getElement(Element itemRoot, String iterString) {
		// icastlink
		if (iterString == "enclosure") {
			// Enable for large RSS feed
//			Iterator iter = itemRoot.elementIterator(iterString);
//			if (iter.hasNext()) {
//				Element elem = (Element) iter.next();
//
//				for (Iterator attrIter = elem.attributeIterator(); attrIter
//						.hasNext();) {
//					Attribute attribute = (Attribute) attrIter.next();
//					Log.d("xmlParser", attribute.getText());
//					if (attribute.getText().contains("icast")) {
//						String text = attribute.getText();
//						return text;
//					}
//				}
//			}
			return null;
		// all else
		} else {
			Iterator iter = itemRoot.elementIterator(iterString);
			if (iter.hasNext()) {
				Element elem = (Element) iter.next();
				return elem.getText();
			} else {
				Log.d("xmlParser", iterString + " not found");
				return null;
			}
		}
	}

	private void parseContent(String content, Item item) {
//		Log.d("xmlParser", "Before CDATA - \n" + content);
//		content = content.replace("![CDATA[", "");
//		content = content.replace("]]", "");
//		Log.d("xmlParser", "CDATA removed - \n" + content);

		// Description
		while(content.indexOf("<p>") != -1 || content.indexOf("</p>") != -1) {
			content = content.replace("<p>", "");
			Log.d("<p>", "replaced");
			content = content.replace("</p>", "");
			Log.d("</p>", "replaced");
		}
		
		int tagIndex = content.indexOf("<");
		String description = content.substring(0, tagIndex);
		item.setDescription(description);

		// Poster link
		int imgSrcIndex = content.indexOf("img src='");
		if (imgSrcIndex != -1) {
			String imgSrc = content.substring(imgSrcIndex + 9);
			int imgSrcLastIndex = imgSrc.indexOf("'");
			imgSrc = imgSrc.substring(0, imgSrcLastIndex);
			Log.d("xmlParser", "imgSrc - \n" + imgSrc);

			item.setPosterLink(imgSrc);
		} else {
			Log.d("xmlParser", "imgSrc not found");
		}

		// iCast link
		int icastIndex = content.indexOf("mp3=");
		if (icastIndex != -1) {
			String icastLink = content.substring(icastIndex+4);
			int icastLastIndex = icastLink.indexOf(".mp3");
			icastLink = icastLink.substring(0, icastLastIndex+4);
			Log.d("xmlParser", "icastLink - \n" + icastLink);
			
			item.setIcastLink(icastLink);
		}
		
		// Setlist
		int setlistIndex = content.indexOf("MAM-List");
		if (setlistIndex != -1) {
			String setlist = content.substring(setlistIndex);
			int setlistTextIndex = setlist.indexOf("/>");
			setlist = setlist.substring(setlistTextIndex + 2);
			setlist = setlist.replaceAll("<br />", "\n");
			setlist = setlist.replaceAll("<br/>", "\n");
			setlist = setlist.replaceAll("<br>", "\n");
			setlist = setlist.replaceAll("</br>", "\n");
			setlist = setlist.replace("&#8211;", "-");
			setlist = setlist.replace("</div>", "");

			Log.d("xmlParser", setlist);

			item.setSetlist(setlist);
		}
	}

	public ArrayList<Item> getItemsList() {
		return itemsList;
	}
	
	private static class DateParser {
		
		private static String parseDate(String date) {
			
			String day = null;
			String month = null;
			String year = null;
			
			day = date.substring(5,7);
			
			String tempMonth = date.substring(8,11);
			switch (tempMonth) {
			case "Jan":
				month = "01";
				break;
			case "Feb":
				month = "02";
				break;
			case "Mar":
				month = "03";
				break;
			case "Apr":
				month = "04";
				break;
			case "May":
				month = "05";
				break;
			case "Jun":
				month = "06";
				break;
			case "Jul":
				month = "07";
				break;
			case "Aug":
				month = "08";
				break;
			case "Sep":
				month = "09";
				break;
			case "Oct":
				month = "10";
				break;
			case "Nov":
				month = "11";
				break;
			case "Dec":
				month = "12";
				break;
			default:
				break;
			}
			
			year = date.substring(12,16);
			
			String newDate = day + "/" + month + "/" + year;
			Log.d("xmlParser", newDate);
			
			return newDate;
		}
	}
}