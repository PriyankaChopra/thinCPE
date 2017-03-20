package org.opendaylight.datapathFirewall.impl.flow;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.controller.config.api.annotations.Description;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.FirewallPolicy;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.base.Optional;

public class FlowProvisioner {

	private final SalFlowService salFlowService;
	private final DataBroker dataBroker;
//	private int priority = 10;
	private final int hardTime = 0;
	private final int idleTime = 0;
	private AtomicLong flowCookieInc = new AtomicLong(0x2b00000000000000L);
	private short flowTableId = 0;
	public AtomicLong flowIdInc = new AtomicLong();

	public FlowProvisioner(DataBroker databroker, SalFlowService salFlowService) {
		this.dataBroker = databroker;
		this.salFlowService = salFlowService;
	}


	/**
	 * @param nodeInstanceId
	 * @param tableInstanceId
	 * @param flowPath
	 * @param flow
	 * @return
	 */
	private Future<RpcResult<AddFlowOutput>> writeFlowToNode(InstanceIdentifier<Node> nodeInstanceId,
			InstanceIdentifier<Table> tableInstanceId,
			InstanceIdentifier<Flow> flowPath,
			Flow flow) {
//		System.out.println("Adding flow to node : "+nodeInstanceId.firstKeyOf(Node.class, NodeKey.class).getId().getValue());
		final AddFlowInputBuilder builder = new AddFlowInputBuilder(flow);
		builder.setNode(new NodeRef(nodeInstanceId));
		builder.setFlowRef(new FlowRef(flowPath));
		builder.setFlowTable(new FlowTableRef(tableInstanceId));
		builder.setTransactionUri(new Uri(flow.getId().getValue()));
//		System.out.println("flow : "+flow);
		return salFlowService.addFlow(builder.build());
	}



	/**
	 * @param nodeId 
	 * @param priority
	 */
	public void pushArpFlow(String nodeId, int priority){
		System.out.println("Starting function pushArpFlow");
		MatchBuilder matchBuilder = new MatchBuilder();

		// Set Ether Type as ARP in Match Field.
		EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
		ethTypeBuilder.setType(new EtherType(0x0806L));
		EthernetMatchBuilder ethMatchBuilder = new EthernetMatchBuilder();
		ethMatchBuilder.setEthernetType(ethTypeBuilder.build());
		matchBuilder.setEthernetMatch(ethMatchBuilder.build());

		/*ArpMatchBuilder arpMatchBuilder = new ArpMatchBuilder();
		if(srcIp != null && !srcIp.equals("")){
			System.out.println("srcIp : "+ srcIp);
			Ipv4Prefix ipv4SrcPrefix = new Ipv4Prefix(srcIp);
			arpMatchBuilder.setArpSourceTransportAddress(ipv4SrcPrefix);
		}
		if(dstIp != null && !dstIp.equals("")){
			System.out.println("dstIp : "+ dstIp);
			Ipv4Prefix ipv4DstPrefix = new Ipv4Prefix(dstIp);
			arpMatchBuilder.setArpTargetTransportAddress(ipv4DstPrefix);
		}
		matchBuilder.setLayer3Match(arpMatchBuilder.build());*/

		Match match = matchBuilder.build();
		//		System.out.println(" Match is done ..");

		// Create Action list.
		List<Action> actionList = new ArrayList<Action>();
		int order = 0;

		//Set Actions // set output port
		ActionBuilder outputNodeConnActionBuilder = setOutputPortInAction(String.valueOf(OutputPortValues.FLOOD));
		outputNodeConnActionBuilder.setOrder(order);
		actionList.add(outputNodeConnActionBuilder.build());
		order++;

		ApplyActionsCase applyActionsCase = applyActions(actionList);
		Instructions instructions = instructionBuilder(applyActionsCase);

		Flow flow = createFlow((short) 0, match, instructions, priority, hardTime, idleTime);


		/*InstanceIdentifier<Nodes> nodesInstanceIdentifier = InstanceIdentifier.builder(Nodes.class).build();
		ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
		List<Node> nodeList;
		Optional<Nodes> NodesOptional = null;
		try {
			NodesOptional = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, nodesInstanceIdentifier).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (NodesOptional.isPresent()) {
			nodeList = NodesOptional.get().getNode();
			//			System.out.println("nodeList.size()" + nodeList.size());
			if(nodeList.size() > 0){
				for (Node localNode : nodeList) {
					System.out.println("localNode.getId().toString() " + localNode.getId().toString());
					InstanceIdentifier<Node> instanceIdentifier = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(localNode.getId().getValue().toString()))).build();
					System.out.println(" going to push ARP Flow on the Node " + localNode.getId().getValue().toString());
					writeFlow(instanceIdentifier, flow, "firewallRule_arp", true);
				}
			}
			System.out.println("ARP Flows written successfully on all nodes");
		}*/
		
		InstanceIdentifier<Node> instanceIdentifier = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(nodeId))).build();
		System.out.println("Going to push ARP Flow on the Node " + nodeId);
		writeFlow(instanceIdentifier, flow, "firewallRule_arp", true);
		System.out.println("ARP Flow written successfully on node: "+nodeId);

	}


	/**
	 * @param policy
	 * @param outPort
	 * @param dstIpAction
	 * @param nodeId
	 * @param priority
	 */
	public void pushIpv4Flow(FirewallPolicy policy, String outPort, String dstIpAction, String nodeId, int priority){
		//		String policyName, String srcIp, String dstIp, String outPort, String dstIpAction
		System.out.println("Starting function pushIpv4Flow");
		// Create Match Builder to add the match tuples.
		MatchBuilder matchBuilder = new MatchBuilder();
		
		// Set Ether Type as IPV4 in Match Field.
		EthernetTypeBuilder ethTypeBuilder2 = new EthernetTypeBuilder();
		ethTypeBuilder2.setType(new EtherType(0x0800L));
		EthernetMatchBuilder ethMatchBuilder2 = new EthernetMatchBuilder();
		ethMatchBuilder2.setEthernetType(ethTypeBuilder2.build());
		matchBuilder.setEthernetMatch(ethMatchBuilder2.build());
		
		if(policy.getL3Match() != null){
			Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();

			if(policy.getL3Match().getSourceIp() != null && policy.getL3Match().getSourceSubnetMask() != null){
				if(policy.getL3Match().getSourceIp() != null && !policy.getL3Match().getSourceIp().equals("")){
				String srcIp = policy.getL3Match().getSourceIp()+"/"+policy.getL3Match().getSourceSubnetMask();
				// Set Src and Dst IP in Match Field.
					System.out.println("srcIp : "+ srcIp);
					Ipv4Prefix ipv4SrcPrefix = new Ipv4Prefix(srcIp);
					ipv4MatchBuilder.setIpv4Source(ipv4SrcPrefix);
				}
			}
			if(policy.getL3Match().getDestinationIp() != null && policy.getL3Match().getDestinationSubnetMask() != null){
				if(policy.getL3Match().getDestinationIp() != null && !policy.getL3Match().getDestinationIp().equals("")){
				String dstIp = policy.getL3Match().getDestinationIp()+"/"+policy.getL3Match().getDestinationSubnetMask();
					System.out.println("dstIp : "+ dstIp);
					Ipv4Prefix ipv4DstPrefix = new Ipv4Prefix(dstIp);
					ipv4MatchBuilder.setIpv4Destination(ipv4DstPrefix);
				}
			}
			matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
			
			if(policy.getL3Match().getProtocolNumber() != null && policy.getL3Match().getProtocolNumber() != 0){
				IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
				ipMatchBuilder.setIpProtocol((short)policy.getL3Match().getProtocolNumber().intValue());
				ipMatchBuilder.setIpProto(IpVersion.Ipv4);

				matchBuilder.setIpMatch(ipMatchBuilder.build());
			}
			
		}
		Match match2 = matchBuilder.build();

		// Create Action list.
		List<Action> actionList = new ArrayList<Action>();
		int order = 0;

		//Set Actions
		if(null != dstIpAction){
			ActionBuilder nwDstIpInActionBuilder = setNwDstIpInAction(dstIpAction);
			nwDstIpInActionBuilder.setOrder(order);
			order++;
			actionList.add(nwDstIpInActionBuilder.build());
		}
		if(null != outPort){
			if(!outPort.isEmpty()){
				System.out.println("outPort : "+outPort);
				ActionBuilder outputNodeConnActionBuilder2;
				// set output port
				if(outPort.equalsIgnoreCase("ALL")){
					outputNodeConnActionBuilder2 = setOutputPortInAction(String.valueOf(OutputPortValues.ALL));}
				else if(outPort.equalsIgnoreCase("FLOOD")){
					outputNodeConnActionBuilder2 = setOutputPortInAction(String.valueOf(OutputPortValues.FLOOD));}
				else if(outPort.equalsIgnoreCase("NORMAL")){
					outputNodeConnActionBuilder2 = setOutputPortInAction(String.valueOf(OutputPortValues.NORMAL));}
				else{
					outputNodeConnActionBuilder2 = setOutputPortInAction(String.valueOf(outPort));
				}
				outputNodeConnActionBuilder2.setOrder(order);
				order++;
				actionList.add(outputNodeConnActionBuilder2.build());
			}
			}
		if(null != policy.getAction()){
			System.out.println("policy.getAction() :: "+policy.getAction());
		if(policy.getAction().equals(org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.FirewallPolicy.Action.Drop)){
				//		if(null == dstIpAction && null == outPort){
				//drop d packet
			System.out.println("--indrop block----");
				ActionBuilder dropActionBuilder = new ActionBuilder();
				DropActionCaseBuilder drop = new DropActionCaseBuilder();
				drop.setDropAction(new DropActionBuilder().build());
				dropActionBuilder.setAction(drop.build());
				dropActionBuilder.setOrder(order);
				order++;
				dropActionBuilder.setKey(new ActionKey(order));
				actionList.add(dropActionBuilder.build());
			}
		}

		ApplyActionsCase applyActionsCase2 = applyActions(actionList);
		Instructions instructions2 = instructionBuilder(applyActionsCase2);
		Flow flow2 = createFlow((short) 0, match2, instructions2, priority, hardTime, idleTime);

		/*InstanceIdentifier<Nodes> nodesInstanceIdentifier = InstanceIdentifier.builder(Nodes.class).build();
		ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
		List<Node> nodeList;
		Optional<Nodes> NodesOptional = null;
		try {
			NodesOptional = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, nodesInstanceIdentifier).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		if (NodesOptional.isPresent()) {
			nodeList = NodesOptional.get().getNode();
			//			System.out.println("nodeList.size()" + nodeList.size());
			if(nodeList.size() > 0){
				for (Node localNode : nodeList) {
					System.out.println("localNode.getId().toString() " + localNode.getId().toString());*/

					InstanceIdentifier<Node> instanceIdentifier = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(nodeId))).build();
					System.out.println("Going to push IPv4 Flow on the Node " + nodeId);
					writeFlow(instanceIdentifier, flow2, "firewallRule"+policy.getPolicyName(), true);
				/*}
				System.out.println("IPv4 Flow has been written successfully on all nodes");
			}
		}*/
	}
	
	
	
	public void pushDefaultFlow(int inPort, String action, String nodeId, int priority){
		System.out.println("Starting function pushDefaultFlow");

		// Create Match Builder to add the match tuples.
		MatchBuilder matchBuilder = new MatchBuilder();
		
		
		
		Match match2 = matchBuilder.build();

		// Create Action list.
		List<Action> actionList = new ArrayList<Action>();
		ActionBuilder outputNodeConnActionBuilder2 = setOutputPortInAction(String.valueOf(OutputPortValues.CONTROLLER));

		ApplyActionsCase applyActionsCase2 = applyActions(actionList);
		Instructions instructions2 = instructionBuilder(applyActionsCase2);
		Flow flow2 = createFlow((short) 0, match2, instructions2, priority, hardTime, idleTime);

		InstanceIdentifier<Nodes> nodesInstanceIdentifier = InstanceIdentifier.builder(Nodes.class).build();
		ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
		List<Node> nodeList;
		Optional<Nodes> NodesOptional = null;
		try {
			NodesOptional = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, nodesInstanceIdentifier).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		if (NodesOptional.isPresent()) {
			nodeList = NodesOptional.get().getNode();
			if(nodeList.size() > 0){
				for (Node localNode : nodeList) {
					System.out.println("localNode.getId().toString() " + localNode.getId().toString());

					InstanceIdentifier<Node> instanceIdentifier = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(localNode.getId().getValue().toString()))).build();
					System.out.println("Going to push default Flow on the Node " + localNode.getId().getValue().toString());
					writeFlow(instanceIdentifier, flow2, "defaultRule", true);
				}
				System.out.println("Default Flow has been written successfully on all nodes");
			}
		}
	}
	
	

	/**
	 * {@link Description} SetNwSrcActionCaseBuilder responsible for setting
	 * Source IP in action
	 * 
	 * @param srcIp
	 * @return
	 */
	public ActionBuilder setNwSrcIpInAction(String srcIp)
	{
		SetNwSrcActionBuilder nwSrcActionBuilder = new SetNwSrcActionBuilder();
		Ipv4Builder ipv4SerBuilder = new Ipv4Builder();
		Ipv4Prefix ipv4SerPrefix = new Ipv4Prefix(srcIp);
		ipv4SerBuilder.setIpv4Address(ipv4SerPrefix);
		nwSrcActionBuilder.setAddress(ipv4SerBuilder.build());
		SetNwSrcAction setNwSrcAction = nwSrcActionBuilder.build();
		SetNwSrcActionCaseBuilder setNwSrcActionCaseBuilder = new SetNwSrcActionCaseBuilder();
		setNwSrcActionCaseBuilder.setSetNwSrcAction(setNwSrcAction);
		SetNwSrcActionCase setNwSrcActionCase = setNwSrcActionCaseBuilder
				.build();
		ActionBuilder actionBuilder = new ActionBuilder();
		actionBuilder.setAction(setNwSrcActionCase);
		return actionBuilder;
	}

	/**
	 * {@link Description} setNwDstActionCaseBuilder responsible for setting
	 * Destination IP in action
	 * 
	 * @param dstIp
	 * @return
	 */
	public ActionBuilder setNwDstIpInAction(String dstIp)
	{
		SetNwDstActionBuilder nwDstActionBuilder = new SetNwDstActionBuilder();
		Ipv4Builder ipv4CliBuilder = new Ipv4Builder();
		Ipv4Prefix ipv4CliPrefix = new Ipv4Prefix(dstIp);
		ipv4CliBuilder.setIpv4Address(ipv4CliPrefix);
		nwDstActionBuilder.setAddress(ipv4CliBuilder.build());
		SetNwDstAction setNwDstAction = nwDstActionBuilder.build();
		SetNwDstActionCaseBuilder setNwDstActionCaseBuilder = new SetNwDstActionCaseBuilder();
		setNwDstActionCaseBuilder.setSetNwDstAction(setNwDstAction);
		SetNwDstActionCase setNwDstActionCase = setNwDstActionCaseBuilder
				.build();
		ActionBuilder actionBuilder = new ActionBuilder();
		actionBuilder.setAction(setNwDstActionCase);
		return actionBuilder;
	}

	/**
	 * {@link Description} setDlDstActionCaseBuilder responsible for setting
	 * Destination MAC in action
	 * 
	 * @param dstMac
	 * @return
	 */
	public ActionBuilder setDstMacInAction(String dstMac)
	{
		SetDlDstActionBuilder setDlDstActionBuilder = new SetDlDstActionBuilder();
		MacAddress macAdd = new MacAddress(dstMac);
		setDlDstActionBuilder.setAddress(macAdd); // MacAddress
		SetDlDstAction setDlDstAction = setDlDstActionBuilder.build();
		SetDlDstActionCaseBuilder setDlDstActionCaseBuilder = new SetDlDstActionCaseBuilder();
		setDlDstActionCaseBuilder.setSetDlDstAction(setDlDstAction);
		SetDlDstActionCase setDlDstActionCase = setDlDstActionCaseBuilder
				.build();
		ActionBuilder actionBuilder = new ActionBuilder();
		actionBuilder.setAction(setDlDstActionCase);
		return actionBuilder;
	}

	/**
	 * {@link Description} outputActionCaseBuilder responsible for setting
	 * Output port in action
	 * 
	 * @param nodeConnector
	 * @return
	 */
	public ActionBuilder setOutputPortInAction(String nodeConnector)
	{
		OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
		outputActionBuilder.setMaxLength(new Integer(0xffff));
		outputActionBuilder.setOutputNodeConnector(new Uri(nodeConnector));
		OutputAction outputAction = outputActionBuilder.build();
		OutputActionCaseBuilder outputActionCaseBuilder = new OutputActionCaseBuilder();
		outputActionCaseBuilder.setOutputAction(outputAction);
		OutputActionCase outputActionCase = outputActionCaseBuilder.build();
		ActionBuilder actionBuilder = new ActionBuilder();
		actionBuilder.setAction(outputActionCase);
		return actionBuilder;
	}

	/**
	 * {@link Description} applyActionsCaseBuilder responsible for setting
	 * selected list of actions (actionList) in flow
	 * 
	 * @param actionList
	 * @return
	 */
	ApplyActionsCase applyActions(List<Action> actionList)
	{
		ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
		applyActionsBuilder.setAction(actionList);
		ApplyActions applyActions = applyActionsBuilder.build();
		ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
		applyActionsCaseBuilder.setApplyActions(applyActions);
		ApplyActionsCase applyActionsCase = applyActionsCaseBuilder.build();
		return applyActionsCase;
	}

	/**
	 * {@link Description} instructionsBuilder responsible for setting selected
	 * applyActionCases in flow
	 * 
	 * @param applyActionsCase
	 * @return
	 */
	Instructions instructionBuilder(ApplyActionsCase applyActionsCase)
	{
		InstructionBuilder instructionBuilder = new InstructionBuilder();
		instructionBuilder.setOrder(0);
		instructionBuilder.setInstruction(applyActionsCase);
		Instruction instruction = instructionBuilder.build();

		List<Instruction> instructionList = new ArrayList<Instruction>();
		instructionList.add(instruction);

		InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
		instructionsBuilder.setInstruction(instructionList);
		Instructions instructions = instructionsBuilder.build();
		return instructions;
	}

	/**
	 * {@link Description} flowBuilder responsible for creating flow object
	 * 
	 * @param tableId
	 * @param match
	 * @param instructions
	 * @param priority
	 * @param hardTimeOut
	 * @param IdleTimeOut
	 * @return
	 */
	public Flow createFlow(short tableId, Match match,
			Instructions instructions, int priority, int hardTimeOut,
			int IdleTimeOut)
	{
		FlowModFlags flowModFlags = new FlowModFlags(false, false, false,
				false, true);
		FlowBuilder flowBuilder = new FlowBuilder();
		flowBuilder.setTableId(tableId);
		// flowBuilder.setFlowName(flowName);
		flowBuilder.setId(new FlowId(Long.toString(flowBuilder.hashCode())));
		flowBuilder.setMatch(match);
		flowBuilder.setInstructions(instructions);
		flowBuilder.setPriority(priority);
		flowBuilder.setBufferId(0L);
		flowBuilder.setHardTimeout(hardTimeOut);
		flowBuilder.setIdleTimeout(IdleTimeOut);
		flowBuilder.setFlags(flowModFlags);
		flowBuilder.setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())));

		Flow flow = flowBuilder.build();
		return flow;
	}

	/**
	 * {@link Description} Responsible for writing flow on a specific node uses
	 * add flow RPC of SalFlowService of mdsal in writeFlowToController method
	 * 
	 * @param nodeRef
	 * @param flow
	 * @param flowName
	 */
	public void writeFlow(InstanceIdentifier<Node> nodeInstanceIdentifier, Flow flow, String flowName,
			boolean isFlowDBUpdate)
	{
		//		InstanceIdentifier<Node> nodeInstanceIdentifier = extractNodeInstanceIdentifier(nodeRef.getValue());
		InstanceIdentifier<Table> tableId = getTableInstanceId(nodeInstanceIdentifier);
		InstanceIdentifier<Flow> flowId = getFlowInstanceId(tableId);
		writeFlowToNode(nodeInstanceIdentifier, tableId, flowId, flow);
	}

	InstanceIdentifier<Node> extractNodeInstanceIdentifier(
			InstanceIdentifier<?> childOfNode)
	{
		return childOfNode.firstIdentifierOf(Node.class);
	}

	InstanceIdentifier<Table> getTableInstanceId(InstanceIdentifier<Node> nodeId)
	{
		// get flow table key
		TableKey flowTableKey = new TableKey(flowTableId);

		return nodeId.builder().augmentation(FlowCapableNode.class).child(
				Table.class, flowTableKey).build();
	}

	InstanceIdentifier<Flow> getFlowInstanceId(InstanceIdentifier<Table> tableId)
	{
		// generate unique flow key
		FlowId flowId = new FlowId(String.valueOf(flowIdInc.getAndIncrement()));
		FlowKey flowKey = new FlowKey(flowId);
		return tableId.child(Flow.class, flowKey);
	}


	/**
	 * Responsible for extracting NodeIDs based on input Src and Dst IPs
	 * @param ipAddressSrc
	 * @param ipAddressDst
	 * @return {@link RestData} - used for computing route
	 */
	//	public RestData callRestconfApi(String ipAddressSrc,String ipAddressDst)
	//	{
	//		String srcNodeID = null;
	//		String delimiter = ":";
	//		RestData restData = new RestData();
	//		String inventoryDetails = getNodeDetailsFromInventory();			    
	//		//try{
	//			JSONTokener jt = new JSONTokener(inventoryDetails);
	//			try{
	//			JSONObject json = new JSONObject(jt);
	//			JSONObject nodes = json.getJSONObject("nodes");
	//			JSONArray node = nodes.getJSONArray("node");
	//			System.out.println("No. of nodes is = "+node.length());
	//			for( int i = 0; i < node.length(); i++)
	//			{
	//				JSONObject pre = node.getJSONObject(i);
	//				JSONArray node_connect = pre.getJSONArray("node-connector");
	//				for( int j = 0; j < node_connect.length(); j++)
	//				{
	//					JSONObject address_tracker = node_connect.getJSONObject(j);
	//					if((address_tracker.has("address-tracker:addresses")))
	//					{
	//						String nodeID = address_tracker.getString("id");        					        			
	//						JSONArray address = address_tracker.getJSONArray("address-tracker:addresses");
	//						for( int k = 0; k < address.length(); k++)
	//						{
	//							JSONObject addressobject = address.getJSONObject(k);
	//							if(addressobject.getString("ip").equals(ipAddressSrc) ){
	//								restData.setSrcMac(addressobject.getString("mac"));
	//								restData.setSrcPort(address_tracker.getInt("flow-node-inventory:port-number"));
	//								String[] temp = nodeID.split(delimiter);
	//								srcNodeID = temp[0]+":"+temp[1];
	//								System.out.println("value for src Nid" + srcNodeID);
	//								restData.setSrcNodeID(srcNodeID);
	//							}
	//							else if (addressobject.getString("ip").equals(ipAddressDst)){
	//								{
	//									restData.setDstMac(addressobject.getString("mac"));
	//									restData.setDstPort(address_tracker.getInt("flow-node-inventory:port-number"));
	//									String[] temp = nodeID.split(delimiter);
	//									restData.setDstNodeID(temp[0]+":"+temp[1]);                                                            
	//								}
	//							}
	//						}
	//					}
	//				}
	//			}
	//		}
	//		catch (Exception e)
	//		{
	//			e.printStackTrace();
	//		}
	//		return restData;
	//	}

	/**
	 * Responsible for fetching inventory details from ODL using Restconf API
	 * @return String - API result 
	 */
	public void getNodeDetailsFromInventory(){
		InstanceIdentifier<Nodes> nodesInstanceIdentifier = InstanceIdentifier.builder(Nodes.class).build();
		System.out.println();
		ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction(); 
		List<Node> nodeList;
		//try {
		Optional<Nodes> NodesOptional = null;
		try {
			NodesOptional = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, nodesInstanceIdentifier).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (NodesOptional.isPresent()) {
			nodeList = NodesOptional.get().getNode();
			System.out.println("switch stats - nodeList.size()" + nodeList.size());
			for (Node localNode : nodeList) {
				InstanceIdentifier<Node> instanceIdentifier = InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(localNode.getId())).build();

				//                	for(NodeConnector localNodeConnector 
				//                			: localNode.getNodeConnector()){
				//                		System.out.println(localNodeConnector.getKey());
				//                		System.out.println(localNodeConnector);
				//                		System.out.println("-----------------------------------------------------------------");
				//                	}
			}
		}
	}

}
