package com.toagearcheck;

import java.util.ArrayList;

public class ChatMemory
{
	private final ArrayList<String> list;
	private final int maxSize = 5;
	
	public ChatMemory()
	{
		list = new ArrayList<>(maxSize);
	}
	
	public void add(String element)
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
}
