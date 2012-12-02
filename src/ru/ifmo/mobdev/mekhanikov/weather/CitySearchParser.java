package ru.ifmo.mobdev.mekhanikov.weather;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class CitySearchParser {
	private List<City> result = new ArrayList<City>();

	public List<City> parse(String src) {
		try {
			System.setProperty("org.xml.sax.driver",
					"org.xmlpull.v1.sax2.Driver");
			XMLReader xr = XMLReaderFactory.createXMLReader();
			XMLParser handler = new XMLParser();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			InputStream xmlStream = new ByteArrayInputStream(
					src.getBytes("UTF-8"));
			xr.parse(new InputSource(xmlStream));
			if (!result.isEmpty()) {
				result.remove(result.size() - 1);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private class XMLParser extends DefaultHandler {
		String thisSec = null;
		private City curCity = null;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			if (qName.equals("city")) {
				curCity = new City();
				curCity.cityId = atts.getValue(0);
			}
			thisSec = qName;
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
			if (localName.equals("city")) {
				result.add(curCity);
			}
			thisSec = "";
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			String line = new String(ch, start, length);
			if (thisSec.equals("name")) {
				curCity.cityName = line;
			} else if (thisSec.equals("region")) {
				if (curCity.regionName == null) {
					curCity.regionName = line;
				} else {
					curCity.regionName = curCity.regionName + ", " + line;
				}
			} else if (thisSec.equals("country")) {
				if (curCity.regionName == null) {
					curCity.regionName = line;
				} else {
					curCity.regionName = line + ", " + curCity.regionName;
				}
			}
		}
	}
}
