package ethz.ganeshr.wot.test.BACnet;


import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;

import com.serotonin.bacnet4j.exception.BACnetException;

import de.thingweb.servient.impl.ServedThing;
import de.thingweb.thing.Action;
import de.thingweb.thing.HyperMediaLink;
import de.thingweb.thing.Thing;
import ethz.ganeshr.wot.test.ServerMain;


public class GenericActionHandler {
	public static String handleActionRequest(ServedThing thing, Action action, Object inputData, BACnetChannel channel){
		/*
		try {
			URI uri = new URI(action.getHrefs().get(2));
			String query = uri.getQuery();
			if(query != null){
				java.net.URLDecoder decoder = new URLDecoder();
				String data = decoder.decode(query);
				data = data.replace("inputData=", "");
				JSONObject obj = new JSONObject(data);
				JSONObject id = obj.getJSONObject("objectReference");
				try {
					BACnetSubscriptionHandler.handleSubscriptionRequest(thing, action, obj.toString());
				} catch (BACnetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		/*Invocation of an action would result in the affected property being written the target value.
		 * The affected property is known from the action id - which is affected propertyid + "_CMD"
		 * On deletion of the action, the priority array index 8 of the affected property is written with NULL
		 */	

		String templateFile = action.getName() + ".jsonld";
		Thing createdThing = BACnetDiscoveryHandler.handleCreateFromTDFile(templateFile);
		String uri = createdThing.getMetadata().getAll("uris").get(0);
		try{
			URI u = new URI(uri);
			uri = u.getPath();
		}catch(Exception e){}
		
		String affectedPropertyId = action.getMetadata().get("@id").replace("_CMD", "");
		channel.update(affectedPropertyId, (String)inputData);	

		final HyperMediaLink childLink = new HyperMediaLink("child", uri, "GET", "application/.td+jsonld");	
		
		if(action != null){				
			action.getMetadata().getAssociations().add(childLink);
		}
		createdThing.setDeleteCallback((dp)->{
			if(action != null){
				action.getMetadata().getAssociations().remove(childLink);
			}
			channel.update(affectedPropertyId, "{\"value\":null, \"priority\":8}");
			channel.reportDeletion(createdThing);
		});
		
		return uri;
		
	}
	
}
