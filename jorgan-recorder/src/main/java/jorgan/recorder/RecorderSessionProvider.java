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
package jorgan.recorder;

import java.io.File;
import java.io.IOException;

import jorgan.disposition.Element;
import jorgan.disposition.event.OrganAdapter;
import jorgan.play.OrganPlay;
import jorgan.problem.ElementProblems;
import jorgan.recorder.disposition.RecorderSwitch;
import jorgan.session.OrganSession;
import jorgan.session.SessionListener;
import jorgan.session.spi.SessionProvider;

public class RecorderSessionProvider implements SessionProvider {

	/**
	 * {@link Performance} is optional.
	 */
	public void init(OrganSession session) {
	}

	public Object create(final OrganSession session, Class<?> clazz) {
		if (clazz == Performance.class) {
			final Performance performance = new Performance(session
					.lookup(OrganPlay.class), session
					.lookup(ElementProblems.class)) {
				@Override
				protected File resolve(String name) {
					return session.resolve(name);
				}
			};
			session.addListener(new SessionListener() {
				public void constructingChanged(boolean constructing) {
					performance.stop();
				}

				public void saved(File file) throws IOException {
					if (performance.isLoaded()) {
						performance.save();
					}
				}

				public void destroyed() {
					performance.dispose();
				}
			});
			session.getOrgan().addOrganListener(new OrganAdapter() {
				@Override
				public void propertyChanged(Element element, String name) {
					if (RecorderSwitch.class.isInstance(element)
							&& "active".equals(name)) {
						RecorderSwitch recorderSwitch = ((RecorderSwitch) element);
						if (recorderSwitch.isActive()) {
							recorderSwitch.perform(performance);
						}
					}
				}
			});
			return performance;
		}
		return null;
	}
}