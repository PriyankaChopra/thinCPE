/*
 * Copyright © 2016 Verizon and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.datapathFirewall.impl.flow;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;

public class TestNodeDetection implements OpendaylightInventoryListener{

	@Override
	public void onNodeUpdated(NodeUpdated notification) {
		System.out.println("on node updated -------------------------");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNodeRemoved(NodeRemoved notification) {
		System.out.println("on node removed -------------------------------");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
		// TODO Auto-generated method stub
		
	}

}
