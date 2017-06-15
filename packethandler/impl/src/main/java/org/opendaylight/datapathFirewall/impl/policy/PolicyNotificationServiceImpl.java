/*
 * Copyright © 2016 Verizon and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.datapathFirewall.impl.policy;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.datapathFirewall.impl.flow.FlowProvisioner;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.FirewallPolicy;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.PolicyNotifInput;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.PolicyNotifOutput;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.PolicyNotifOutputBuilder;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.PolicyNotifRpcService;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.policynotif.input.FwPolicies;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.base.Optional;

public class PolicyNotificationServiceImpl implements PolicyNotifRpcService{

	private final SalFlowService salFlowService;
	private final DataBroker dataBroker;
	
	public PolicyNotificationServiceImpl(DataBroker dataBroker, SalFlowService salFlowService) {
		this.dataBroker = dataBroker;
		this.salFlowService = salFlowService;
	}

	@Override
	public Future<RpcResult<PolicyNotifOutput>> policyNotif(PolicyNotifInput input) {
//chnage for removing node ID
		String nodeId = input.getNodeId();
		FlowProvisioner flowProvisioner = new FlowProvisioner(dataBroker, salFlowService);

		for(FwPolicies policyObj : input.getFwPolicies()){
			if(policyObj.getFirewallPolicy() != null){
				FirewallPolicy policy = policyObj.getFirewallPolicy();
				System.out.println(policy.getAction());
				InstanceIdentifier<Nodes> nodesInstanceIdentifier = InstanceIdentifier.builder(Nodes.class).build();
				ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
				List<Node> nodeList;
				Optional<Nodes> NodesOptional = null;
				try {
					NodesOptional = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, nodesInstanceIdentifier).get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//change for removing node ID
				/*if (NodesOptional.isPresent()) {
					nodeList = NodesOptional.get().getNode();
					if(nodeList.size() > 0){
						for (Node localNode : nodeList) {
							System.out.println("localNode.getId().toString() " + localNode.getId().toString());
							InstanceIdentifier<Node> instanceIdentifier = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(localNode.getId().getValue().toString()))).build();
							String nodeId = localNode.getId().getValue().toString();
							System.out.println("Node ID : " + localNode.getId().getValue().toString());
							flowProvisioner.pushIpv4Flow(policy, null, null, nodeId, policyObj.getPriority().intValue());
						}
					}
				}*/
//				chnage for removing node ID
				flowProvisioner.pushIpv4Flow(policy, null, null, nodeId, policyObj.getPriority().intValue());
			}
		}
		PolicyNotifOutput output = new PolicyNotifOutputBuilder().build();
		return RpcResultBuilder.success(output).buildFuture();
	}

}
