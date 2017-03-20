
package org.opendaylight.datapathFirewall.impl.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.datapathFirewall.impl.PackethandlerProvider;
import org.opendaylight.datapathFirewall.impl.flow.FlowProvisioner;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.FirewallPolicy;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.PolicyNotifInput;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.PolicyNotifOutput;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.PolicyNotifOutputBuilder;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.PolicyNotifRpcService;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.policynotif.input.FwPolicies;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class PolicyNotificationServiceImpl implements PolicyNotifRpcService{

	private final SalFlowService salFlowService;
	private final DataBroker dataBroker;
	
	public PolicyNotificationServiceImpl(DataBroker dataBroker, SalFlowService salFlowService) {
		this.dataBroker = dataBroker;
		this.salFlowService = salFlowService;
	}

	@Override
	public Future<RpcResult<PolicyNotifOutput>> policyNotif(PolicyNotifInput input) {

		String nodeId = input.getNodeId();
		FlowProvisioner flowProvisioner = new FlowProvisioner(dataBroker, salFlowService);

		for(FwPolicies policyObj : input.getFwPolicies()){
			if(policyObj.getFirewallPolicy() != null){
				FirewallPolicy policy = policyObj.getFirewallPolicy();
				System.out.println(policy.getAction());
				flowProvisioner.pushIpv4Flow(policy, null, null, nodeId, policyObj.getPriority().intValue());
			}
		}
		PolicyNotifOutput output = new PolicyNotifOutputBuilder().build();
		return RpcResultBuilder.success(output).buildFuture();
	}

}
