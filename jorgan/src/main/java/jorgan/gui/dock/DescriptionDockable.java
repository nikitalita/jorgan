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
package jorgan.gui.dock;

import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import jorgan.disposition.Element;
import jorgan.disposition.event.OrganAdapter;
import jorgan.disposition.event.OrganEvent;
import jorgan.play.event.PlayAdapter;
import jorgan.session.OrganSession;
import jorgan.session.event.ElementSelectionEvent;
import jorgan.session.event.ElementSelectionListener;
import bias.Configuration;

/**
 * Panel that displays the description of an element.
 */
public class DescriptionDockable extends OrganDockable {

	private static Configuration config = Configuration.getRoot().get(
			DescriptionDockable.class);

	private JTextArea textArea;

	private OrganSession session;

	private ElementHandler elementHandler = new ElementHandler();

	private PlayHandler playHandler = new PlayHandler();

	private Element element;

	/**
	 * Constructor.
	 */
	public DescriptionDockable() {
		config.read(this);

		textArea = new JTextArea();
		textArea.setEnabled(false);
		textArea.setBackground(new Color(255, 255, 225));
		textArea.setBorder(new EmptyBorder(5, 5, 5, 5));
		textArea.setForeground(Color.BLACK);
		textArea.setDisabledTextColor(Color.BLACK);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		setContent(new JScrollPane(textArea));
	}

	@Override
	public boolean forPlay() {
		return false;
	}
	
	/**
	 * Set the organ.
	 * 
	 * @param session
	 *            session
	 */
	public void setSession(OrganSession session) {
		if (this.session != null) {
			this.session.removeOrganListener(elementHandler);
			this.session.removeSelectionListener(elementHandler);
			this.session.removePlayerListener(playHandler);

			write();
		}

		this.session = session;
		this.element = null;

		if (this.session != null) {
			this.session.addOrganListener(elementHandler);
			this.session.addSelectionListener(elementHandler);
			this.session.addPlayerListener(playHandler);

			read();
		}
	}

	private void read() {
		if (element == null) {
			textArea.setText("");
			textArea.setEnabled(false);
		} else {
			textArea.setText(element.getDescription());
			textArea.setEnabled(true);
		}
		textArea.setCaretPosition(0);
	}

	private void write() {
		if (element != null) {
			String description = textArea.getText();
			if (!element.getDescription().equals(description)) {
				element.setDescription(description);
			}
		}
	}

	/**
	 * The handler of selections.
	 */
	private class ElementHandler extends OrganAdapter implements
			ElementSelectionListener {

		public void selectionChanged(ElementSelectionEvent ev) {
			write();

			element = session.getElementSelection().getSelectedElement();

			read();
		}

		@Override
		public void changed(OrganEvent event) {
			read();
		}
	}

	private class PlayHandler extends PlayAdapter {

		@Override
		public void opened() {
			textArea.setEnabled(false);
		}

		@Override
		public void closed() {
			write();

			textArea.setEnabled(true);
		}
	}
}