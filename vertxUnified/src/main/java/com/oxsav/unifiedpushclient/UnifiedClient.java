/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 *
 */
package com.oxsav.unifiedpushclient;


import java.util.ArrayList;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.jboss.aerogear.unifiedpush.*;
import org.jboss.aerogear.unifiedpush.message.*;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage.Builder;

/*
 * This is Vert.x module that allow the users to send notifications to differents variant applications
 *
 * @author Oxsav
 */
public class UnifiedClient extends BusModBase implements Handler<Message<JsonObject>> {

	//global variables
	protected String address;
	protected int UnifiedPort;
	protected String URLUnifiedPush;
	protected String password;
	protected String pushApplicationId;
	protected String masterSecret;
	protected String serverName;
	protected JsonArray variants;
	protected boolean proxy;
	public JavaSender defaultJavaSender;
	
	public Message<JsonObject> clientMessage;
	
	public void start() {
		super.start();
		final Logger logger = container.logger();
		
		address = getOptionalStringConfig("address", "vertx.unifiedpush");
		URLUnifiedPush = getOptionalStringConfig("unifiedpushurl", "localhost");
		UnifiedPort = getOptionalIntConfig("unifiedport", 8080);
		serverName = getOptionalStringConfig("serverName", "unifiedpush-server-0.10.1");
		proxy = getOptionalBooleanConfig("proxy", false);
		pushApplicationId = getOptionalStringConfig("pushapplicationid", null);
		masterSecret = getOptionalStringConfig("mastersecret", null);
		variants = getOptionalArrayConfig("variants", null);
		
		//with proxy
		if(proxy){
			System.out.println("has proxy");
			/*defaultJavaSender = new SenderClient.Builder()
			 * .rootServerURL("http://localhost:8080/ag-push")
			 * .proxy("proxy.example.com", 8080)
			 * .proxyUser("proxyuser")
			 * .proxyPassword("password")
			 * .proxyType("")
			 * .build();*/
		}
		
		//without proxy
		if(!proxy){	
			defaultJavaSender =new SenderClient("http://" + URLUnifiedPush + ":" + UnifiedPort + "/" + serverName);
		}
		
		logger.info("UnifiedPush Vertx started");
		
		eb.registerHandler(address, this);
	
	}
	
	//HANDLE MESSAGES
	@Override
	
	public void handle(Message<JsonObject> message) {
		// TODO Auto-generated method stub
		String action = message.body().getString("action");
		String alert = message.body().getString("alert");
		
		if (action == null) {
			sendError(message, "action must be specified");
			return;
		}
		
		if(pushApplicationId.isEmpty()){
			throw new NullPointerException("Push Application ID is missing!");
		}
		
		if(masterSecret.isEmpty()){
			throw new NullPointerException("Master Secret is missing!");
		}
		
		if(alert == null){
			sendError(message, "Message need to be defined!");
			return;
		}
		
		switch (action) {
			case "send":
				clientMessage = message;
				sendNotification(message);
				break;
			default:
				sendError(message, "Invalid action: " + action);
				return;
		}
	}
	
	//send message function 
	
	public void sendNotification(Message<JsonObject> message){
		
		
		
		/* set Variables to unified messages */ 
		UnifiedMessage finalMessage;
		Builder unifiedMessage = new UnifiedMessage.Builder();
		
		/* define Unified Message */
		unifiedMessage.pushApplicationId(pushApplicationId);
		unifiedMessage.masterSecret(masterSecret);
		
		/* setting variants */
		ArrayList<String> defineVariants = new ArrayList<String>();
		for(int index = 0; index < variants.size(); index++){
			defineVariants.add(variants.get(index).toString());
		}
		
		if(defineVariants.size() > 0)
			unifiedMessage.variants(defineVariants);
		
		/* define aliases for the unified message */
		JsonArray messageAliases = message.body().getArray("aliases");
		if(messageAliases != null){
			ArrayList<String> aliases = new ArrayList<String>();
			
			for(int i = 0; i < messageAliases.size(); i++){
				aliases.add(messageAliases.get(i).toString());
			}
			
			unifiedMessage.aliases(aliases);
		}
			
			
		/* setting alert for unified Message */
		unifiedMessage.alert(message.body().getString("alert").toString());
		unifiedMessage.sound("default");
		/* set Final message */
		finalMessage = unifiedMessage.build(); 
		defaultJavaSender.send(finalMessage, callback);
	
	}
	
	//callback function handle the errors
	MessageResponseCallback callback = new MessageResponseCallback() {
		@Override
		public void onComplete(int statusCode) {
			JsonObject reply = new JsonObject();
			reply.putString("status", "ok");
			reply.putString("statusCode", "Status Code - " + statusCode);
			clientMessage.reply(reply);
		}
		
		@Override
		public void onError(Throwable throwable) {
			JsonObject reply = new JsonObject();
			reply.putString("status", "error");
			reply.putString("error", throwable.getMessage().toString());
			clientMessage.reply(reply);
		}
	};
}
