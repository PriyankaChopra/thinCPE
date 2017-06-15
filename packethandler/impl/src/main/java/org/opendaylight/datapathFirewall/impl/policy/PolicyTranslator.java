/*
 * Copyright © 2016 Verizon and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.datapathFirewall.impl.policy;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.FirewallPolicy;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.FirewallPolicy.Action;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.FirewallPolicyBuilder;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.firewall.policy.L3MatchBuilder;
import org.opendaylight.yang.gen.v1.datapathfirewall.policy.rev161021.fwpolicy.firewall.policy.L4MatchBuilder;

public class PolicyTranslator {

	//	public static Properties mainProperties;

	public FirewallPolicy mapPolicyDetails(String policyName, 
			String srcIp, String srcMask, String dstIp, String dstMask, int protocolNumber,
			int srcPort, int dstPort, 
			String action){

		FirewallPolicyBuilder policyBuilder = new FirewallPolicyBuilder();

		L3MatchBuilder l3MatchBuilder = new L3MatchBuilder();
		l3MatchBuilder.setSourceIp(srcIp);
		l3MatchBuilder.setSourceSubnetMask(srcMask);
		l3MatchBuilder.setDestinationIp(dstIp);
		l3MatchBuilder.setDestinationSubnetMask(dstMask);
		l3MatchBuilder.setProtocolNumber(protocolNumber);

		L4MatchBuilder l4MatchBuilder = new L4MatchBuilder();
		l4MatchBuilder.setSourcePort(srcPort);
		l4MatchBuilder.setDestinationPort(dstPort);

		policyBuilder.setPolicyName(policyName);
		policyBuilder.setL3Match(l3MatchBuilder.build());
		policyBuilder.setL4Match(l4MatchBuilder.build());
		if(action.equalsIgnoreCase("allow"))
			policyBuilder.setAction(Action.Allow);
		else
			policyBuilder.setAction(Action.Drop);

		return policyBuilder.build();
	}

	public List<FirewallPolicy> parseACL(){
		//		String path = "/home/sdnuser/new/policy.txt";
		//        File CP_file = new File(path);
		ArrayList<FirewallPolicy> policyRuleList = new ArrayList<FirewallPolicy>();

		try
		{
			//        	mainProperties = new Properties();
			//        	FileInputStream file = new FileInputStream(CP_file);
			//        	mainProperties.load(file);
			//        	file.close();
			//            String versionString = mainProperties.getProperty("app.version");
			//            System.out.println(versionString);

			JSONParser parser = new JSONParser();
			JSONArray policies = (JSONArray) parser.parse(new FileReader("/home/sdnuser/new/policyReader/result.json"));
//			System.out.println(policies.toJSONString());
			Iterator<JSONObject> iterator = policies.iterator();
			while (iterator.hasNext()) {
				JSONObject policy = iterator.next();
				String policyName = (String) policy.get("policyName");
				String srcIp = (String) policy.get("srcIp");
				String srcMask = (String) policy.get("srcMask");
				String srcSubnet = (String) policy.get("srcSubnet");
				String dstIp = (String) policy.get("dstIp");
				String dstMask = (String) policy.get("dstMask");
				String dstSubnet = (String) policy.get("dstSubnet");
				String action = (String) policy.get("action");
				String protocol = (String) policy.get("protocol");
				if(protocol.equalsIgnoreCase("icmp"))
					protocol = "1";
				else if(protocol.equalsIgnoreCase("tcp"))
					protocol = "6";
				else if(protocol.equalsIgnoreCase("udp"))
					protocol = "17";
				else
					continue;
				System.out.println(policyName +"----"+srcIp+"----"+srcMask+"----"+srcSubnet+"----"+dstIp+"----"+dstMask+"----"+dstSubnet+"----"+protocol+"----"+action);
				PolicyTranslator policyTranslator = new PolicyTranslator();
				FirewallPolicy policyRule = policyTranslator.mapPolicyDetails(policyName, srcIp, srcMask, dstIp, dstMask, Integer.valueOf(protocol), 0, 0, action);
				policyRuleList.add(policyRule);
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return policyRuleList;

	}

}
