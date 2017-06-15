/*
 * Copyright © 2016 Verizon and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.datapathFirewall.impl.flow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.datapathFirewall.impl.PackethandlerProvider;
import org.opendaylight.datapathFirewall.impl.policy.PolicyTranslator;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.FirewallPolicy;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.FirewallPolicyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class NodeDetector implements DataChangeListener, OpendaylightInventoryListener{


	private final DataBroker databroker;
	private final FlowProvisioner flowProvisioner;


	public NodeDetector(DataBroker databroker, NotificationProviderService notificationService, SalFlowService salFlowService) {
		this.databroker = databroker;
		this.flowProvisioner = new FlowProvisioner(databroker,salFlowService);
		notificationService.registerNotificationListener(this);
	}

	@Override
	public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
		System.out.println("NodeDetector----------onNodeConnectorRemoved -------" + notification);
	}

	@Override
	public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
		System.out.println("NodeDetector-------onNodeConnectorUpdated ");
		System.out.println("NodeDetector-------onNodeConnectorUpdated -------" + notification);
	}

	@Override
	public void onNodeRemoved(NodeRemoved notification) {
		System.out.println("NodeDetector-------onNodeRemoved -------" + notification);
	}

	@Override
	public void onNodeUpdated(NodeUpdated notification) {
		System.out.println("NodeDetector-------onNodeUpdated -------" + notification);
		System.out.println(notification.getId().getValue().toString());
	}

	@Override
	public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change){

		final Map<InstanceIdentifier<?>, DataObject> originalData = change.getOriginalData() != null
				? change.getOriginalData() : Collections.emptyMap();

				Map<InstanceIdentifier<?>, DataObject> createdData = change.getCreatedData();
				if(createdData !=null && !createdData.isEmpty()) {
					Set<InstanceIdentifier<?>> nodeIds = createdData.keySet();
					if(nodeIds != null && !nodeIds.isEmpty()) {
						for(InstanceIdentifier<?> nodeId : nodeIds) {
							if(Node.class.isAssignableFrom(nodeId.getTargetType())) {
								InstanceIdentifier<Node> invNodeId = (InstanceIdentifier<Node>)nodeId;
								if(invNodeId.firstKeyOf(Node.class,NodeKey.class).getId().getValue().contains("openflow:")) {
									String nodeIdentifier = invNodeId.firstKeyOf(Node.class,NodeKey.class).getId().getValue().toString();
									System.out.println("node id :: "+nodeIdentifier);

									System.out.println(PackethandlerProvider.customerSiteMapping);
									Iterator<JSONObject> customerSiteMappingIterator = PackethandlerProvider.customerSiteMapping.iterator();
									while (customerSiteMappingIterator.hasNext()) {
										JSONObject siteDetails = customerSiteMappingIterator.next();
										String customerName = (String) siteDetails.get("customerName");
										JSONArray cpeDetails = (JSONArray) siteDetails.get("cpeDetails");
										Iterator<JSONObject> cpeDetailsIterator = cpeDetails.iterator();
										while (cpeDetailsIterator.hasNext()) {
											JSONObject cpeDetail = cpeDetailsIterator.next();
											if(cpeDetail.containsKey(nodeIdentifier)){
												cpeDetail.replace(nodeIdentifier, "up");
												if(siteDetails.get("vCpeStatus") != null){
													if(!siteDetails.get("vCpeStatus").toString().equals("instantiated")){
														provisionCpeInstance(customerName,"create");
														siteDetails.replace("vCpeStatus", "instantiated");
														System.out.println("vCPE instantiated ----------- ");
													}
												}
											}
										}
									}
									System.out.println(PackethandlerProvider.customerSiteMapping);

									PolicyTranslator policyTranslator = new PolicyTranslator();
									FirewallPolicy arpRule =  new FirewallPolicyBuilder().build();
									flowProvisioner.pushArpFlow(nodeIdentifier,5);

									FirewallPolicy noMatchRule =  new FirewallPolicyBuilder().build();
									//flowProvisioner.pushIpv4Flow(noMatchRule,"NORMAL","10.1.44.44/24",nodeIdentifier,2);
									//VINOD CHANGES
									flowProvisioner.pushIpv4Flow(noMatchRule,"NORMAL",null,nodeIdentifier,2);

									
/*									List<FirewallPolicy> policyList = policyTranslator.parseACL();
									for(FirewallPolicy policy : policyList){
										if(policy.getAction().equals(Action.Allow)){
											flowProvisioner.pushIpv4Flow(policy,String.valueOf(outPort),null, nodeIdentifier,10);
											if(policy.getL3Match().getDestinationIp() != null){
												if(!policy.getL3Match().getDestinationIp().isEmpty()){
													// FirewallPolicy reversePatchRule = policyTranslator.mapPolicyDetails("reversePatchRule", policy.getL3Match().getDestinationIp(), policy.getL3Match().getDestinationSubnetMask(), null, null, policy.getL3Match().getProtocolNumber().intValue(), 0, 0, "allow");
													FirewallPolicy reversePatchRule = policyTranslator.mapPolicyDetails("reversePatchRule", policy.getL3Match().getDestinationIp(), policy.getL3Match().getDestinationSubnetMask(), null, null, policy.getL3Match().getProtocolNumber().intValue(), 0, 0, "allow");
													flowProvisioner.pushIpv4Flow(reversePatchRule,"flood",null, nodeIdentifier,10);
												}
											}
										}
										else
											flowProvisioner.pushIpv4Flow(policy,null,null, nodeIdentifier,10);

									}
*/								}
							}
						}
					}
				}

				Set<InstanceIdentifier<?>> removedData = change.getRemovedPaths();
				if(removedData !=null && !removedData.isEmpty()) {
					for (InstanceIdentifier<?> key : removedData) {
						if (Node.class.equals(key.getTargetType())) {
							final InstanceIdentifier<Node> ident = key.firstIdentifierOf(Node.class);
							final DataObject removeValue = originalData.get(key);
							Node nodeVal = (Node)removeValue;
							String nodeIdentifier = nodeVal.getId().getValue();
							System.out.println(nodeIdentifier);
							System.out.println(PackethandlerProvider.customerSiteMapping);
							Iterator<JSONObject> customerSiteMappingIterator1 = PackethandlerProvider.customerSiteMapping.iterator();
							while (customerSiteMappingIterator1.hasNext()) {
								JSONObject siteDetails = customerSiteMappingIterator1.next();
								String customerName = (String) siteDetails.get("customerName");
								JSONArray cpeDetails = (JSONArray) siteDetails.get("cpeDetails");
								Iterator<JSONObject> cpeDetailsIterator1 = cpeDetails.iterator();
								while (cpeDetailsIterator1.hasNext()) {
									JSONObject cpeDetail = cpeDetailsIterator1.next();
									if(cpeDetail.containsKey(nodeIdentifier)){
										cpeDetail.replace(nodeIdentifier, "down");
										if(cpeDetail.containsValue("up")){
											continue;
										}
										else {
											provisionCpeInstance(customerName,"delete");
											siteDetails.replace("vCpeStatus", "terminated");
											System.out.println("vCPE terminated");
										}
									}
								}
							}
							System.out.println(PackethandlerProvider.customerSiteMapping);
						}
					}
				}
	}


	public ListenerRegistration<DataChangeListener> registerAsDataChangeListener(DataBroker dataBroker) {
		InstanceIdentifier<Node> nodeInstanceIdentifier = InstanceIdentifier.builder(Nodes.class)
				.child(Node.class).build();
		return dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, nodeInstanceIdentifier, this, AsyncDataBroker.DataChangeScope.BASE);
	}


	public void provisionCpeInstance(String deviceId , String insType){
		String host = "10.75.46.56";
		String user = "root";
		//String password = "";
            	String privateKey = "vcpe.key";

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();
		Channel channel = null;

		try {/*
			Session session = jsch.getSession(user, host, 22);
			jsch.addIdentity(privateKey);
			//session.setPassword(password);
   		 System.out.println("identity added ");
   		 session.setConfig(config);
			session.connect();
			System.out.println("Connecting Ssh ... Session is =  "+session.isConnected());
			System.out.println("Instant Type:::"+insType);
			channel = session.openChannel("exec");
			InputStream outStream = channel.getInputStream();

			String instance="VCPE_FW:".concat(deviceId);
			String cmd;
			if (insType.contains("del")) {
				cmd="sh  /home/cloud/VCPE_FW_launch_script.sh  1 " + instance + " 0  >  /home/cloud/run.txt";
			}			
			else {
				cmd = "sh /home/cloud/VCPE_FW_launch_script.sh  1 " + instance + " 1 > /home/v875003/run.txt" ;
			}
			System.out.println("Command to be executed = " + cmd);

			((ChannelExec) channel).setCommand(cmd);
			channel.connect();
			channel.disconnect();
			session.disconnect();
		*/
			String line;
			Process process1;
			if (insType.contains("del")) {
				process1 = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "sh /home/cloud/launchFwInstance.sh "+ deviceId +" 0"});
				System.out.println("sh /home/cloud/launchFwInstance.sh "+ deviceId +" 0");
			} else {
				process1 = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "sh /home/cloud/launchFwInstance.sh "+ deviceId +" 1"});
				System.out.println("sh /home/cloud/launchFwInstance.sh "+ deviceId +" 1");
			}
			process1.waitFor();
			Integer result = process1.exitValue();
			System.out.println("Exit_status : "+ result);
			InputStream stderr = process1.getErrorStream ();
			InputStream stdout = process1.getInputStream ();

			BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
			BufferedReader errorReader = new BufferedReader (new InputStreamReader(stderr));
			
	        while ((line = reader.readLine ()) != null) {
	                System.out.println ("Stdout: " + line);
	        }
	        while ((line = errorReader.readLine ()) != null) {
	                System.out.println ("Stderr: " + line);
	        }
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exceptions = " +e.getMessage().toString());
			System.out.println(e.getMessage().substring(1, 300).toString());

		}
	}

	
	public void provisionCpeInstanceThruAnsible(String deviceId , String insType){
		String host = "192.168.250.13";
		String user = "choprpr";
		String password = "priyanka";
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();
		Channel channel = null;

		try {
			Session session = jsch.getSession(user, host, 22);
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			System.out.println("Connecting Ssh ... Session is =  "+session.isConnected());
			System.out.println("Instant Type:::"+insType);
			channel = session.openChannel("exec");
			InputStream outStream = channel.getInputStream();

			String instance="VCPE_FW:".concat(deviceId);
			String cmd;
				cmd="sh configureCPE.sh " + instance;
			System.out.println("Command to be executed = " + cmd);

			((ChannelExec) channel).setCommand(cmd);
			channel.connect();
			channel.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exceptions = " +e.getMessage().toString());
			System.out.println(e.getMessage().substring(1, 300).toString());

		}
	}

	
	
	public void fetchFirewallRules(){
		try {

			URL url = new URL("http://10.1.13.37/login");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-type", "application/json");
			String input = "{\"data\": [{\"key\": \"username\",\"value\": \"admin\",\"type\": \"text\",\"enabled\": true},+"
					+ "{\"key\": \"secretkey\",\"value\": \"fortinet\",\"type\": \"text\",\"enabled\": true}],\"dataMode\": \"urlencoded\"}";

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	

	public static void main(String[] args){/*
		try {

			URL url = new URL("http://10.1.13.37/logincheck");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			String input = "{\"data\": [{\"key\": \"username\",\"value\": \"admin\",\"type\": \"text\",\"enabled\": true},+"
					+ "{\"key\": \"password\",\"value\": \"fortinet\",\"type\": \"text\",\"enabled\": true}],\"dataMode\": \"urlencoded\"}";

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			
			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			for (Map.Entry<String, List<String>> k : conn.getHeaderFields().entrySet()) {
			    for (String v : k.getValue()){
			         System.out.println(k.getKey() + ":" + v);
			    }
			}
			
			List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
			String cookie = "";
			for(String str : cookies){
				cookie = cookie+" "+str;
			}
			System.out.println(cookie);
			
			System.out.println(conn.getHeaderField("Set-cookie"));

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

				String output;
				System.out.println("Output from Server .... \n");
				while ((output = br.readLine()) != null) {
					System.out.println(output);
				}
			
			conn.disconnect();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	*/}

}
