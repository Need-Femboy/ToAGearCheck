package com.toagearcheck;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public enum RaidInfo
{
	ToA(13454, 774, 48, 114, 22),
	ToB(14642, 50, 42, 160, 13);
	
	private final int regionId;
	private final int widgetGroup;
	private final int widgetChild;
	private final int widgetWidth;
	private final int widgetHeight;
	
	public List<String> getPlayers(Client client)
	{
		Widget baseWidget = client.getWidget(widgetGroup, widgetChild);
		if (baseWidget == null || baseWidget.getChildren() == null)
		{
			return Collections.emptyList();
		}
		
		return Arrays.stream(baseWidget.getChildren())
				.filter(widget -> widget.getOriginalWidth() == widgetWidth
						&& widget.getOriginalHeight() == widgetHeight)
				.map(Widget::getText)
				.collect(Collectors.toList());
	}
	
	public static RaidInfo inRegion(int regionId)
	{
		return Arrays.stream(RaidInfo.values())
				.filter(r -> r.regionId == regionId)
				.findFirst()
				.orElse(null);
	}
	
	public static boolean widgetAccess(int widgetGroup)
	{
		return Arrays.stream(values()).anyMatch(r -> r.widgetGroup == widgetGroup);
	}
}
