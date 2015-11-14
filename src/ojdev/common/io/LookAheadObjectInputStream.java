package ojdev.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ojdev.common.ConnectedClientState;
import ojdev.common.SelectedAction;
import ojdev.common.WarriorCombatResult;
import ojdev.common.actions.Action;
import ojdev.common.actions.ActionDirection;
import ojdev.common.messages.AgnosticMessage;
import ojdev.common.messages.AllowedMessageContext;
import ojdev.common.messages.InvalidMessage;
import ojdev.common.messages.MessageBase;
import ojdev.common.messages.client.ClientMessage;
import ojdev.common.messages.client.GetConnectedClientsListMessage;
import ojdev.common.messages.client.SendTextMessage;
import ojdev.common.messages.client.SendTextToAllMessage;
import ojdev.common.messages.client.SetWarriorMessage;
import ojdev.common.messages.client.TakeActionMessage;
import ojdev.common.messages.server.ClientConnectedMessage;
import ojdev.common.messages.server.ClientDisconnectedMessage;
import ojdev.common.messages.server.ClientStateChangedMessage;
import ojdev.common.messages.server.ClientStateMessage;
import ojdev.common.messages.server.ConnectedClientsListMessage;
import ojdev.common.messages.server.EngagementActionSelectedMessage;
import ojdev.common.messages.server.EngagementCombatResultMessage;
import ojdev.common.messages.server.EngagementEndedMessage;
import ojdev.common.messages.server.EngagementMessage;
import ojdev.common.messages.server.EngagementStartedMessage;
import ojdev.common.messages.server.RelayedTextMessage;
import ojdev.common.messages.server.RelayedTextToAllMessage;
import ojdev.common.messages.server.ServerMessage;
import ojdev.common.messages.server.ServerTextMessage;
import ojdev.common.messages.server.SetClientIdMessage;
import ojdev.common.warriors.WarriorBase;
import ojdev.common.weapons.Weapon;

/**
 * Checks the class's type to ensure it is of an allowed type before deserialization.
 * It is understood this mitigates some types of attacks that can get any object of their choosing
 * that is in the classpath (which is quite a lot) and run code.
 */
public class LookAheadObjectInputStream extends ObjectInputStream
{		
	private static final List<Class<?>>ALLOWED_EXACT_TYPES;
	private static final List<Class<?>>ALLOWED_ASSIGNABLE_TYPES;

	static {
		List<Class<?>> tempAllowedTypeList = new ArrayList<Class<?>>();
		
		// Abstract
		tempAllowedTypeList.add(MessageBase.class);
		tempAllowedTypeList.add(ServerMessage.class);
		tempAllowedTypeList.add(ClientMessage.class);
		tempAllowedTypeList.add(AgnosticMessage.class);
		tempAllowedTypeList.add(ClientStateMessage.class);
		tempAllowedTypeList.add(EngagementMessage.class);
		
		// Agnostic
		tempAllowedTypeList.add(InvalidMessage.class);
		
		// Client To Server
		tempAllowedTypeList.add(GetConnectedClientsListMessage.class);
		tempAllowedTypeList.add(SendTextMessage.class);
		tempAllowedTypeList.add(SendTextToAllMessage.class);
		tempAllowedTypeList.add(SetWarriorMessage.class);
		tempAllowedTypeList.add(TakeActionMessage.class);
		
		// Server to Client
		tempAllowedTypeList.add(ClientConnectedMessage.class);
		tempAllowedTypeList.add(ClientDisconnectedMessage.class);
		tempAllowedTypeList.add(ClientStateChangedMessage.class);
		tempAllowedTypeList.add(ConnectedClientsListMessage.class);
		tempAllowedTypeList.add(EngagementActionSelectedMessage.class);
		tempAllowedTypeList.add(EngagementCombatResultMessage.class);
		tempAllowedTypeList.add(EngagementEndedMessage.class);
		tempAllowedTypeList.add(EngagementStartedMessage.class);
		tempAllowedTypeList.add(RelayedTextMessage.class);
		tempAllowedTypeList.add(RelayedTextToAllMessage.class);
		tempAllowedTypeList.add(ServerMessage.class);
		tempAllowedTypeList.add(ServerTextMessage.class);
		tempAllowedTypeList.add(SetClientIdMessage.class);
		
		// Non Messages
		tempAllowedTypeList.add(ActionDirection.class);
		tempAllowedTypeList.add(SelectedAction.class);
		tempAllowedTypeList.add(ConnectedClientState.class);
		tempAllowedTypeList.add(AllowedMessageContext.class);
		tempAllowedTypeList.add(WarriorCombatResult.class);
		tempAllowedTypeList.add(Enum.class);
		
		// Generic Classes
		tempAllowedTypeList.add(Date.class);
		tempAllowedTypeList.add(String.class);
		tempAllowedTypeList.add(Integer.class);
		tempAllowedTypeList.add(Boolean.class);
		tempAllowedTypeList.add(Number.class);
		
		
		ALLOWED_EXACT_TYPES = Collections.unmodifiableList(tempAllowedTypeList);
		
		
		tempAllowedTypeList = new ArrayList<Class<?>>();
		tempAllowedTypeList.add(Collection.class);
		tempAllowedTypeList.add(Map.class);
		tempAllowedTypeList.add(Weapon.class);
		tempAllowedTypeList.add(Action.class);
		tempAllowedTypeList.add(WarriorBase.class);
		
		ALLOWED_ASSIGNABLE_TYPES = Collections.unmodifiableList(tempAllowedTypeList);
	}
	
	public LookAheadObjectInputStream(InputStream in) throws IOException {
		super(in);
	}
	
	/**
	 * Checks resolved class is of an expected type.
	 * 
	 * Code adapted from: 
	 * 		http://www.ibm.com/developerworks/library/se-lookahead/index.html
	 * Code influenced by: 
	 * 		https://www.securecoding.cert.org/confluence/display/java/OBJ09-J.+Compare+classes+and+not+class+names
	 * 		http://www.contrastsecurity.com/security-influencers/java-serialization-vulnerability-threatens-millions-of-applications
	 * 
	 * Exact Type matching is favored, 
	 * however assignable (extends or implements) checking is needed due to internal Collection classes.
	 */
	@Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
            ClassNotFoundException {

		Class<?> klass = super.resolveClass(desc);
		for(Class<?> type : ALLOWED_EXACT_TYPES) {
			if(klass == type){
				return klass;
			}
		}
		for(Class<?> type : ALLOWED_ASSIGNABLE_TYPES) {
			if(type.isAssignableFrom(klass)) {
				return klass;
			}
		}
		
        throw new InvalidClassException(
        	"Unauthorized deserialization attempt",
             desc.getName());
    }
}