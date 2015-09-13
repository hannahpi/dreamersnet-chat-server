package net.dreamersnet.ChatServer;

import java.util.HashMap;

/**
 * ServerObject : a class that allows clients to create an object for users to interact with.
 * 
 * @author Ben Parker
 */
class ServerObject {
	Server serv;
	HashMap<String, Object> property = new HashMap<String, Object>(); // allows custom properties
	String itemName = new String ("SO");
	String desc = new String ("Newly Created Object");
	String creator = new String(" ");
	boolean destroyed=false;
	int health = 100;
	int durability = 1;
	int dmg = 0;
	int delay = 0;
	
	ServerObject(Server serv, String itemName, String creator) {
		this.serv = serv;
		this.itemName= itemName;
		this.creator=creator;
	}
	
	ServerObject(Server serv, String itemName, String creator, String desc ) {
		this.serv = serv;
		this.itemName = itemName;
		this.desc = desc;
		this.creator = creator;
	}
	
	void setStat(int health, int durability, int dmg, int delay) {
		this.health= health;
		this.durability = durability;
		this.dmg = dmg;
		this.delay = delay;
	}
	
	int calcDamage() {
		return dmg/durability;
	}
	
	void takeDamage(int baseDmg) {
		health -= baseDmg/durability;
		if (health<=0)
		{
			health = 0;
			destroyed = true;
		}
	}
	
	String getStat(){
		String tmp = "Health: " + this.health + " Durability: " + this.durability + " Damage: " + this.dmg + " Delay: " + this.delay;
		return tmp;
	}
	
	String getDesc(){
		if (this.destroyed)
			return "a destroyed " + this.desc;
		else
			return this.desc;
	}
	
	String getName() {
		return this.itemName;
	}
	
	String getCreator() {
		return this.creator;
	}
	
	void addProperty(String str, Object obj) {
		property.put(str, obj);
	}
	
	boolean isUsable(){
		return !(this.destroyed);
	}
}

