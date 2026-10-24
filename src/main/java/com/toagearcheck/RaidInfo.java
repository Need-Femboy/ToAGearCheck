package com.toagearcheck;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.runelite.client.util.Text;

@AllArgsConstructor
@Slf4j
public enum RaidInfo
{
	ToA(13454, 774, 48, 114, 22),
	ToB(14642, 50, 42, 160, 13),
	TFA(16470, InterfaceID.TFA_PARTYDETAILS, InterfaceID.TfaPartydetails.APPLICANTS_LIST & 0xffff, 0, 0);

	// Build 241, clientscript 10058 (TFA applicant row): background, then name,
	// followed by the remaining columns. Members use a different, 14-widget row.
	private static final int TFA_APPLICANT_ROW_SIZE = 22;
	
	private final int regionId;
	private final int widgetGroup;
	private final int widgetChild;
	private final int widgetWidth;
	private final int widgetHeight;
	
	public List<String> getPlayers(Client client)
	{
		Widget baseWidget = client.getWidget(widgetGroup, widgetChild);
		if (baseWidget == null || baseWidget.isHidden() || baseWidget.getChildren() == null)
		{
			return Collections.emptyList();
		}
		
		return Arrays.stream(baseWidget.getChildren())
				.filter(Objects::nonNull)
				.filter(widget -> this == TFA
						? widget.getIndex() % TFA_APPLICANT_ROW_SIZE == 1
						: widget.getOriginalWidth() == widgetWidth && widget.getOriginalHeight() == widgetHeight)
				.map(Widget::getText)
				.filter(Objects::nonNull)
				.map(Text::standardize)
				.filter(name -> !name.isEmpty())
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
