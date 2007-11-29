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
package jorgan.gui.construct;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.CellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import jorgan.disposition.Element;
import jorgan.disposition.Elements;
import jorgan.disposition.Input;
import jorgan.disposition.Message;
import jorgan.disposition.Message.InputMessage;
import jorgan.disposition.event.OrganEvent;
import jorgan.disposition.event.OrganListener;
import jorgan.gui.OrganAware;
import jorgan.gui.OrganPanel;
import jorgan.gui.OrganSession;
import jorgan.gui.event.ElementSelectionEvent;
import jorgan.gui.event.ElementSelectionListener;
import jorgan.midi.DevicePool;
import jorgan.midi.MessageUtils;
import jorgan.swing.BaseAction;
import jorgan.swing.table.IconTableCellRenderer;
import jorgan.swing.table.StringCellEditor;
import jorgan.swing.table.TableUtils;
import swingx.docking.DockedPanel;
import bias.Configuration;
import bias.swing.MessageBox;

/**
 * Panel shows the messages of elements.
 */
public class MessagesPanel extends DockedPanel implements OrganAware {

	private static Configuration config = Configuration.getRoot().get(
			MessagesPanel.class);

	private static final Icon inputIcon = new ImageIcon(OrganPanel.class
			.getResource("img/input.gif"));

	/**
	 * Icon used for indication of an error.
	 */
	private static final Icon outputIcon = new ImageIcon(OrganPanel.class
			.getResource("img/output.gif"));

	/**
	 * The edited organ.
	 */
	private OrganSession session;

	private Element element;

	private List<Message> messages = new ArrayList<Message>();

	/**
	 * The listener to selection changes.
	 */
	private SelectionHandler selectionHandler = new SelectionHandler();

	private AddAction addAction = new AddAction();

	private RemoveAction removeAction = new RemoveAction();

	private RecordAction recordAction = new RecordAction();

	private JTable table = new JTable();

	private MessagesModel messagesModel = new MessagesModel();

	/**
	 * Create a tree panel.
	 */
	public MessagesPanel() {

		addTool(addAction);
		addTool(removeAction);
		addToolSeparator();
		addTool(recordAction);

		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setModel(messagesModel);
		table.getSelectionModel().addListSelectionListener(removeAction);
		table.getSelectionModel().addListSelectionListener(recordAction);
		Map<Boolean, Icon> iconMap = new HashMap<Boolean, Icon>();
		iconMap.put(true, inputIcon);
		iconMap.put(false, outputIcon);
		new IconTableCellRenderer(inputIcon, iconMap).configureTableColumn(
				table, 0);
		table.getColumnModel().getColumn(2).setCellEditor(
				new StringCellEditor());
		table.getColumnModel().getColumn(3).setCellEditor(
				new StringCellEditor());
		table.getColumnModel().getColumn(4).setCellEditor(
				new StringCellEditor());
		TableUtils.hideHeader(table);
		TableUtils.pleasantLookAndFeel(table);

		setScrollableBody(table, true, false);
	}

	/**
	 * Set the organ to be edited.
	 * 
	 * @param session
	 *            session to be edited
	 */
	public void setOrgan(OrganSession session) {
		if (this.session != null) {
			this.session.removeOrganListener(messagesModel);
			this.session.removeSelectionListener(selectionHandler);
		}

		this.session = session;

		if (this.session != null) {
			this.session.addOrganListener(messagesModel);
			this.session.addSelectionListener(selectionHandler);
		}

		updateMessages();
	}

	private void updateCurrentMessage(ShortMessage shortMessage) {
		int index = table.getSelectedRow();
		Message message = messages.get(index);

		message.init("filter " + shortMessage.getStatus(), "filter "
				+ shortMessage.getData1(), "filter " + shortMessage.getData2());

		updateMessages();
	}

	private void updateMessages() {
		CellEditor editor = table.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}

		element = null;
		messages.clear();
		messagesModel.update();
		table.setVisible(false);

		if (session != null
				&& session.getSelectionModel().getSelectionCount() == 1) {

			element = session.getSelectionModel().getSelectedElement();

			for (Message message : element.getMessages()) {
				messages.add(message);
			}

			messagesModel.update();

			table.setVisible(true);
		}

		addAction.update();
	}

	/**
	 * The handler of selections.
	 */
	private class SelectionHandler implements ElementSelectionListener {

		public void selectionChanged(ElementSelectionEvent ev) {
			updateMessages();
		}
	}

	/**
	 * Note that <em>Spin</em> ensures that the methods of this listeners are
	 * called on the EDT, although a change in the organ might be triggered by a
	 * change on a MIDI thread.
	 */
	private class MessagesModel extends AbstractTableModel implements
			OrganListener {

		private int size = 0;

		private void update() {
			if (messages.size() > size) {
				fireTableRowsInserted(size, messages.size() - 1);
			} else if (messages.size() < size) {
				fireTableRowsDeleted(messages.size(), size - 1);
			}
			size = messages.size();
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return size;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex >= 2;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Message message = messages.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return InputMessage.class.isAssignableFrom(message.getClass());
			case 1:
				return Elements.getDisplayName(message.getClass());
			case 2:
				return message.getStatus();
			case 3:
				return message.getData1();
			case 4:
				return message.getData2();
			default:
				throw new IllegalArgumentException("" + columnIndex);
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Message message = messages.get(rowIndex);

			switch (columnIndex) {
			case 2:
				message.setStatus((String) aValue);
				break;
			case 3:
				message.setData1((String) aValue);
				break;
			case 4:
				message.setData2((String) aValue);
				break;
			}
		}

		public void elementAdded(OrganEvent event) {
		}

		public void elementRemoved(OrganEvent event) {
		}

		public void elementChanged(final OrganEvent event) {
			if (event.getElement() == element) {
				updateMessages();
			}
		}
	}

	private class AddAction extends BaseAction {

		private AddAction() {
			config.get("add").read(this);

			setEnabled(false);
		}

		public void actionPerformed(ActionEvent ev) {
			CreateMessageWizard.showInDialog(MessagesPanel.this, session
					.getOrgan(), element);
		}

		public void update() {
			setEnabled(element != null);
		}
	}

	private class RemoveAction extends BaseAction implements
			ListSelectionListener {

		private RemoveAction() {
			config.get("remove").read(this);

			setEnabled(false);
		}

		public void actionPerformed(ActionEvent ev) {
			int[] indices = table.getSelectedRows();
			if (indices != null) {
				for (int i = indices.length - 1; i >= 0; i--) {
					Message message = messages.get(indices[i]);

					element.removeMessage(message);
				}
			}
		}

		public void valueChanged(ListSelectionEvent e) {
			setEnabled(table.getSelectedRow() != -1);
		}
	}

	private class RecordAction extends BaseAction implements
			ListSelectionListener {

		private MessageBox dialog = new MessageBox(MessageBox.OPTIONS_OK);

		private RecordAction() {
			config.get("record").read(this);

			config.get("record/dialog").read(dialog);

			setEnabled(false);
		}

		public void valueChanged(ListSelectionEvent e) {
			int[] indices = table.getSelectedRows();
			setEnabled(indices.length == 1
					&& messages.get(indices[0]) instanceof InputMessage);
		}

		public void actionPerformed(ActionEvent ev) {
			Input input = null;
			
			if (element instanceof Input) {
				input = (Input)element;
			} else {
				for (Element element : MessagesPanel.this.element.getReferrer()) {
					if (element instanceof Input) {
						input = (Input)element;
					}
				}
			}

			if (input != null) {
				record(input.getInput());
			}
		}

		private void record(String deviceName) {
			try {
				MidiDevice device = DevicePool.getMidiDevice(deviceName, false);

				device.open();

				Transmitter transmitter = device.getTransmitter();
				transmitter.setReceiver(new Receiver() {
					public void send(final MidiMessage message, long when) {
						if (MessageUtils.isShortMessage(message)) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									if (isRecording()) {
										updateCurrentMessage((ShortMessage) message);

										dialog.hide();
									}
								}
							});
						}
					}

					public void close() {
					}
				});

				dialog.show(MessagesPanel.this);

				transmitter.close();
				device.close();
			} catch (MidiUnavailableException cannotRecord) {
			}
		}
		
		private boolean isRecording() {
			return table.getSelectedRow() != -1;
		}
	}
}