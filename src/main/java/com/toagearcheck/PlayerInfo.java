package com.toagearcheck;

import lombok.Getter;

import java.util.ArrayList;

public class PlayerInfo
{
	private final ArrayList<String> list;
	private final int maxSize = 5;
	@Getter
	private Role role = Role.NONE;
	
	public PlayerInfo()
	{
		list = new ArrayList<>(maxSize);
	}
	
	public void addChatMessage(String element)
	{
		if (list.size() >= maxSize)
		{
			list.remove(0);
		}
		list.add(element);
	}
	
	public ArrayList<String> getList()
	{
		return list;
	}
	
	public void setRole(Role role)
	{
		if (role == null) //Stops random messages from unmarking your roll call
		{
			return;
		}
		
		this.role = role;
	}
}
