/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2007 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */
package tigase.xmpp.impl;

import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.util.logging.Logger;
import tigase.db.NonAuthUserRepository;
import tigase.server.Packet;
import tigase.xml.Element;
import tigase.xmpp.Authorization;
import tigase.xmpp.NotAuthorizedException;
import tigase.xmpp.XMPPImplIfc;
import tigase.xmpp.XMPPProcessor;
import tigase.xmpp.XMPPProcessorIfc;
import tigase.xmpp.XMPPResourceConnection;
import tigase.xmpp.XMPPException;
import tigase.util.JIDUtils;

/**
 * Describe class SimpleForwarder here.
 *
 *
 * Created: Sat Jan 13 16:58:41 2007
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public abstract class SimpleForwarder
	extends XMPPProcessor
	implements XMPPProcessorIfc {

  /**
   * Private logger for class instancess.
   */
  private static Logger log =
		Logger.getLogger("tigase.xmpp.impl.SimpleForwarder");

	// Implementation of tigase.xmpp.XMPPProcessorIfc

	private boolean containsXMLNS(String xmlns) {
		for (String ns: supNamespaces()) {
			if (xmlns.equals(ns)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Describe <code>process</code> method here.
	 *
	 * @param packet a <code>Packet</code> value
	 * @param session a <code>XMPPResourceConnection</code> value
	 * @param repo a <code>NonAuthUserRepository</code> value
	 * @param results a <code>Queue</code> value
	 */
	public void process(final Packet packet, final XMPPResourceConnection session,
		final NonAuthUserRepository repo, final Queue<Packet> results,
		final Map<String, Object> settings)
		throws XMPPException {

		if (session == null) {
			return;
		} // end of if (session == null)

		try {

			String id = JIDUtils.getNodeID(packet.getElemTo());

			if (id.equals(session.getUserId())) {
				// Yes this is message to 'this' client
				Element elem = packet.getElement().clone();
				Packet result = new Packet(elem);
				result.setTo(session.getConnectionId());
				result.setFrom(packet.getTo());
				results.offer(result);
			} else {
				// This is message to some other client
				Element result = packet.getElement().clone();
				results.offer(new Packet(result));
			} // end of else
		} catch (NotAuthorizedException e) {
			log.warning("NotAuthorizedException for packet: "	+ packet.getStringData());
			results.offer(Authorization.NOT_AUTHORIZED.getResponseMessage(packet,
					"You must authorize session first.", true));
		} // end of try-catch

	}

}
