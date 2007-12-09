/*
 * jOrgan - Java Virtual Organ
 * Copyright (C) 2003 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jorgan.io.disposition;

import java.util.HashMap;
import java.util.Map;

import jorgan.disposition.Element;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.SequenceGenerator;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converter for {@link Element}s that writes an id on marshalling an reads an
 * id on unmarshalling.
 * 
 * @see #marshallId(Element, HierarchicalStreamWriter, MarshallingContext)
 * @see #unmarshallId(Element, HierarchicalStreamReader, UnmarshallingContext)
 */
public class ElementConverter implements Converter {

	private Converter nested;

	public ElementConverter(XStream xstream) {
		nested = xstream.getConverterLookup().lookupConverterForType(
				Object.class);
	}

	public boolean canConvert(Class clazz) {
		return Element.class.isAssignableFrom(clazz);
	}

	/**
	 * @see #marshallId(Element, HierarchicalStreamWriter, MarshallingContext)
	 */ 
	public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		Element element = (Element) value;

		marshallId(element, writer, context);

		nested.marshal(element, writer, context);
	}

	/**
	 * Write an id attribute for the given element.
	 * 
	 * @param element
	 *            element
	 * @param writer
	 *            writer
	 * @param context
	 *            context
	 */
	protected void marshallId(Element element, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		writer.addAttribute("id", Marshal.get(context).getId(element));
	}

	/**
	 * @see #unmarshallId(Element, HierarchicalStreamReader, UnmarshallingContext)
	 */ 
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {

		Element element = (Element) nested.unmarshal(reader, context);

		unmarshallId(element, reader, context);

		return element;
	}

	protected void unmarshallId(Element element, HierarchicalStreamReader reader,
			UnmarshallingContext context) {

		Unmarshal.get(context).putElement(reader.getAttribute("id"),
				element);
	}

	public static class Marshal {

		private Map<Element, String> map = new HashMap<Element, String>();

		private SequenceGenerator sequenceGenerator = new SequenceGenerator(1);

		public static Marshal get(MarshallingContext context) {
			Marshal state = (Marshal) context
					.get(Marshal.class);
			if (state == null) {
				state = new Marshal();
				context.put(Marshal.class, state);
			}
			return state;
		}

		public String getId(Element element) {

			String id = map.get(element);
			if (id == null) {
				id = sequenceGenerator.next();
				map.put(element, id);
			}
			return id;
		}
	}

	public static class Unmarshal {

		private Map<String, Element> map = new HashMap<String, Element>();

		public static Unmarshal get(UnmarshallingContext context) {
			Unmarshal state = (Unmarshal) context
					.get(Unmarshal.class);
			if (state == null) {
				state = new Unmarshal();
				context.put(Unmarshal.class, state);
			}
			return state;
		}

		public void putElement(String id, Element element) {
			Element previous = map.put(id, element);
			if (previous != null) {
				throw new ConversionException("duplicate id '" + id + "'");
			}
		}

		public Element getElement(String id) {
			
			Element element = map.get(id);
			if (element == null) {
				throw new ConversionException("unkown element id '" + id + "'");
			}
			return element;
		}
	}
}