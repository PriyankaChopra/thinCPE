/*
 * Copyright © 2016 Verizon and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.datapathFirewall.impl.flow;

import java.util.Map;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.Link;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LinkDetector implements DataChangeListener {

	private final DataBroker databroker;

	@Override
	public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
		// TODO Auto-generated method stub
		
	}
	
	public LinkDetector(DataBroker databroker) {
		super();
		this.databroker = databroker;
	}
	/*
	


	public ListenerRegistration<DataChangeListener> registerAsDataChangeListener() {
        InstanceIdentifier<Link> linkInstance = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1"))).child(Link.class).build();
        return databroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, linkInstance, this,
                AsyncDataBroker.DataChangeScope.BASE);
    }



	@Override
	public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> dataChangeEvent) {
		
		if (dataChangeEvent == null) {
            return;
        }
        Map<InstanceIdentifier<?>, DataObject> createdData = dataChangeEvent.getCreatedData();
        Set<InstanceIdentifier<?>> removedPaths = dataChangeEvent.getRemovedPaths();
        Map<InstanceIdentifier<?>, DataObject> originalData = dataChangeEvent.getOriginalData();
        boolean isGraphUpdated = false;

        if (createdData != null && !createdData.isEmpty()) {
            Set<InstanceIdentifier<?>> linksIds = createdData.keySet();
            for (InstanceIdentifier<?> linkId : linksIds) {
                if (Link.class.isAssignableFrom(linkId.getTargetType())) {
                    Link link = (Link) createdData.get(linkId);
                    if (!(link.getLinkId().getValue().contains("host"))) {
                        isGraphUpdated = true;
                        System.out.println("Graph is updated! Added Link " + link.getLinkId().getValue());
                        System.out.println("source node : " + link.getSource().getSourceNode().getValue() 
                        		+  ", source port : " + link.getSource().getSourceTp().getValue().replace(link.getSource().getSourceNode().getValue(),""));
                        System.out.println("Destination node : " + link.getDestination().getDestNode().getValue()
                        		+  ", Destination port : " + link.getDestination().getDestTp().getValue().replace(link.getDestination().getDestNode().getValue(),""));
                        break;
                    }
                }
            }
        }

        if (removedPaths != null && !removedPaths.isEmpty() && originalData != null && !originalData.isEmpty()) {
            for (InstanceIdentifier<?> instanceId : removedPaths) {
                if (Link.class.isAssignableFrom(instanceId.getTargetType())) {
                    Link link = (Link) originalData.get(instanceId);
                    if (!(link.getLinkId().getValue().contains("host"))) {
                        isGraphUpdated = true;
                        System.out.println("Graph is updated! Removed Link " + link.getLinkId().getValue());
                        break;
                    }
                }
            }
        }

        if (!isGraphUpdated) {
            return;
        }
        if (!networkGraphRefreshScheduled) {
            synchronized (this) {
                if (!networkGraphRefreshScheduled) {
                    topologyDataChangeEventProcessor.schedule(new TopologyDataChangeEventProcessor(), graphRefreshDelay,
                            TimeUnit.MILLISECONDS);
                    networkGraphRefreshScheduled = true;
                    LOG.debug("Scheduled Graph for refresh.");
                }
            }
        } else {
            LOG.debug("Already scheduled for network graph refresh.");
            threadReschedule = true;
        }
		
		
	}

*/}
