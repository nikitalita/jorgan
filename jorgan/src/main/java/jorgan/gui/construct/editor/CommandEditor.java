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
package jorgan.gui.construct.editor;

import java.beans.PropertyEditorSupport;

/**
 * Property editor for a boolean property.
 */
public class CommandEditor extends PropertyEditorSupport {

  private String[] tags;

  public CommandEditor() {
    tags = new String[]{"NOTE_ON", "POLY_PRESSURE"};   
  }

  public String[] getTags() {
    return tags;
  }

  public String getAsText() {

    Integer value = (Integer)getValue();
    if (value == null) {
      return "";
    } else {
      if (value.intValue() == 144) {
        return tags[0];
      } else if (value.intValue() == 160) {
        return tags[1];
      }
      throw new IllegalStateException("unkown command");
    }
  }

  public void setAsText(String string) {

    if (tags[0].equals(string)) {
      setValue(new Integer(144));
    } else if (tags[1].equals(string)) {
      setValue(new Integer(160));
    } else {
        throw new IllegalArgumentException("unkown command");
    }
  }
}
