/*
 * Copyright © 2016 Verizon and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.datapathFirewall.impl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.datapathFirewall.impl.flow.LinkDetector;
import org.opendaylight.datapathFirewall.impl.flow.NodeDetector;
import org.opendaylight.datapathFirewall.impl.flow.TestNodeDetection;
import org.opendaylight.datapathFirewall.impl.policy.PolicyNotificationServiceImpl;
import org.opendaylight.yang.gen.v1.datapathfirewall.policynotif.rev170302.PolicyNotifRpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackethandlerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PackethandlerProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcRegistry;
    private final SalFlowService salFlowService;
    private final NotificationProviderService notificationProviderService;
	public static JSONArray customerSiteMapping;

    public PackethandlerProvider(final DataBroker dataBroker, RpcProviderRegistry rpcRegistry, SalFlowService salFlowService, NotificationProviderService notificationProviderService) {
        this.dataBroker = dataBroker;
        this.rpcRegistry = rpcRegistry;
        this.salFlowService = salFlowService;
        this.notificationProviderService = notificationProviderService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void initProvider() {
        LOG.info("PackethandlerProvider Session Initiated");
        System.out.println("-------------------------------------------------------------------------------------------------------------------");
    	System.out.println("dataBroker : "+dataBroker);
    	System.out.println("rpcRegistry : "+rpcRegistry);
    	System.out.println("salFlowService : "+salFlowService);
    	
        NodeDetector nodeDetector = new NodeDetector(dataBroker, notificationProviderService, salFlowService);
    	notificationProviderService.registerNotificationListener(nodeDetector);
    	nodeDetector.registerAsDataChangeListener(dataBroker);
    	
    	
    	TestNodeDetection detection = new TestNodeDetection();
    	notificationProviderService.registerNotificationListener(detection);
    	
    	
    	LinkDetector linkDetector = new LinkDetector(dataBroker);
//    	linkDetector.registerAsDataChangeListener();
    	
    	PolicyNotificationServiceImpl policyNotificationServiceImpl = new PolicyNotificationServiceImpl(dataBroker, salFlowService);
    	rpcRegistry.addRpcImplementation(PolicyNotifRpcService.class, policyNotificationServiceImpl);
    	
    	JSONParser parser = new JSONParser();
			try {
				customerSiteMapping = (JSONArray) parser.parse(new FileReader("/home/sdnuser/new/policyReader/customerSiteMapping.json"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void closeProvider() {
        LOG.info("PackethandlerProvider Closed");
    }
}