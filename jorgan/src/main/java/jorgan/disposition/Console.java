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
package jorgan.disposition;

/**
 * A console.
 */
public class Console extends Element {

	/**
	 * The maximum supported zoom.
	 */
	public static final float MAX_ZOOM = 2.0f;

	/**
	 * The minimum supported zoom.
	 */
	public static final float MIN_ZOOM = 0.5f;

	/**
	 * The device for input.
	 */
	private String device;

	/**
	 * The skin.
	 */
	private String skin;

	/**
	 * The zoom.
	 */
	private float zoom = 1.0f;

	private String screen;

	protected boolean canReference(Class clazz) {
		return Element.class.isAssignableFrom(clazz) && Console.class != clazz;
	}

	protected Reference createReference(Element element) {
		return new ConsoleReference(element);
	}

	public String getDevice() {
		return device;
	}

	public String getSkin() {
		return skin;
	}

	public float getZoom() {
		return zoom;
	}

	public String getScreen() {
		return screen;
	}

	public void setDevice(String device) {
		this.device = device;

		fireElementChanged(true);
	}

	public void setSkin(String skin) {
		this.skin = skin;

		fireElementChanged(true);
	}

	public void setZoom(float zoom) {
		if (zoom < MIN_ZOOM) {
			zoom = MIN_ZOOM;
		}
		if (zoom > MAX_ZOOM) {
			zoom = MAX_ZOOM;
		}

		this.zoom = zoom;

		fireElementChanged(true);
	}

	public void setScreen(String screen) {
		this.screen = screen;

		fireElementChanged(true);
	}

	public void setLocation(Element element, int x, int y) {
		ConsoleReference reference = (ConsoleReference) getReference(element);

		reference.setX(x);
		reference.setY(y);

		fireReferenceChanged(reference, true);
	}

	public int getX(Element element) {
		ConsoleReference reference = (ConsoleReference) getReference(element);

		return reference.getX();
	}

	public int getY(Element element) {
		if (element == this) {
			return 0;
		} else {
			ConsoleReference reference = (ConsoleReference) getReference(element);

			return reference.getY();
		}
	}

	/**
	 * A reference of a console to another element.
	 */
	public static class ConsoleReference extends Reference {

		private int x;

		private int y;

		public ConsoleReference(Element element) {
			super(element);
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public void setX(int i) {
			x = i;
		}

		public void setY(int i) {
			y = i;
		}
	}

	/**
	 * Move to front the reference to the given element.
	 * 
	 * @param element
	 *            element to move to front
	 */
	public void toFront(Element element) {
		Reference reference = getReference(element);
		if (reference == null) {
			throw new IllegalArgumentException("unkown element");
		}

		references.remove(reference);
		references.add(reference);

		fireElementChanged(true);
	}

	/**
	 * Move to back the reference to the given element.
	 * 
	 * @param element
	 *            element to move to back
	 */
	public void toBack(Element element) {
		Reference reference = getReference(element);
		if (reference == null) {
			throw new IllegalArgumentException("unkown element");
		}

		references.remove(reference);
		references.add(0, reference);

		fireElementChanged(true);
	}
}