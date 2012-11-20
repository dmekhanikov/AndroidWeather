package ru.ifmo.mobdev.mekhanikov.weather;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.*;

public class ForecastParser {
	private Forecast result = new Forecast();
	
	public Forecast parse(String src) {
		try {
			System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
			XMLReader xr = XMLReaderFactory.createXMLReader();
			XMLParser handler = new XMLParser();
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			InputStream xmlStream = new ByteArrayInputStream(src.getBytes("UTF-8"));
			xr.parse(new InputSource(xmlStream));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private class XMLParser extends DefaultHandler {
		boolean inCurrentSec = false;
		boolean inTempSec = false;
		boolean inWindSec = false;
		boolean inPressSec = false;
		boolean inHumiditySec = false;
		int day = -1;
		int hour = 30;
		int daypart = -1;
		String thisSec;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			if (day > 3) {
				return;
			}
			if (qName.equals("current")) {
				inCurrentSec = true;
			} else if (qName.equals("t")) {
				inTempSec = true;
			} else if (qName.equals("wind")) {
				inWindSec = true;
			} else if (qName.equals("p")) {
				inPressSec = true;
			} else if (qName.equals("hmid")) {
				inHumiditySec = true;
			} else if (qName.equals("day")) {
				int newHour = Integer.parseInt(atts.getValue(1));
				if (newHour == 3) {
					newHour = 27;
				}
				if (newHour < hour) {
					++day;
				}
				hour = newHour;
				switch (hour) {
				case 9:
					daypart = 0;
					break;
				case 15:
					daypart = 1;
					break;
				case 21:
					daypart = 2;
					break;
				case 27:
					daypart = 3;
					break;
				}
			}
			thisSec = qName;
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
			if (day > 3) {
				return;
			}
			if (localName.equals("current")) {
				inCurrentSec = false;
			} else if (localName.equals("t")) {
				inTempSec = false;
			} else if (localName.equals("wind")) {
				inWindSec = false;
			} else if (localName.equals("p")) {
				inPressSec = false;
			} else if (localName.equals("hmid")) {
				inHumiditySec = false;
			}
			thisSec = "";
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (day > 3) {
				return;
			}
			String line = new String(ch, start, length);
			if (thisSec.equals("pict")) {
				if (inCurrentSec) {
					result.current.picName = line.substring(0, line.length() - 4);
				} else {
					result.forecast[day][daypart].picName = line.substring(0, line.length() - 4);;
				}
			} else if (thisSec.equals("t")) {
				if (inCurrentSec) {
					result.current.temperature = line;
				} else {
					result.forecast[day][daypart].temperature = line;
				}
			} else if (thisSec.equals("p")) {
				if (inCurrentSec) {
					result.current.pressure = line;
				} else {
					result.forecast[day][daypart].pressure = line;
				}
			} else if (thisSec.equals("w")) {
				if (inCurrentSec) {
					result.current.wind = line;
				} else {
					result.forecast[day][daypart].wind = line;
				}
			} else if (thisSec.equals("h")) {
				if (inCurrentSec) {
					result.current.humidity = line;
				} else {
					result.forecast[day][daypart].humidity = line;
				}
			} else if (thisSec.equals("min")) {
				if (inTempSec) {
					result.forecast[day][daypart].temperature = line;
				} else if (inWindSec) {
					result.forecast[day][daypart].wind = line;
				} else if (inPressSec) {
					result.forecast[day][daypart].pressure = line;
				} else if (inHumiditySec) {
					result.forecast[day][daypart].humidity = line;
				}
			} else if (thisSec.equals("max")) {
				if (inTempSec) {
					result.forecast[day][daypart].temperature += " to " + line;
				} else if (inWindSec) {
					result.forecast[day][daypart].wind += " to " + line;
				} else if (inPressSec) {
					result.forecast[day][daypart].pressure += " to " + line;
				} else if (inHumiditySec) {
					result.forecast[day][daypart].humidity += " to " + line;
				}
			}
		}
	}
}
