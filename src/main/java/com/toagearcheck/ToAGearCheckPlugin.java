package com.toagearcheck;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@PluginDescriptor(
		name = "Raid Party Gear Checker",
		description = "Checks gear of party applicants",
		tags = {"toa", "raids3", "tombs of amascut", "tob", "theatre of blood", "tfa", "raids4", "the fractured archive"}
)
public class ToAGearCheckPlugin extends Plugin
{
	@Inject
	private Client client;
	
	@Inject
	private ClientToolbar pluginToolbar;
	
	@Inject
	private ClientThread clientThread;
	
	@Inject
	private ItemManager itemManager;
	
	private BufferedImage toaButton;
	private BufferedImage tobButton;
	private BufferedImage tfaButton;
	
	private NavigationButton navButton;
	
	private boolean navButtonAdded = false;
	private boolean checkingApplicants = false;
	private ToaGearCheckPanel raidGearCheckPanel;
	private int regionId = -1;
	private HashMap<String, PlayerInfo> playerInfo = new HashMap<>();
	
	@Override
	protected void startUp() throws Exception
	{
		raidGearCheckPanel = injector.getInstance(ToaGearCheckPanel.class);
		toaButton = ImageUtil.loadImageResource(ToaGearCheckPanel.class, "toa.png");
		tobButton = ImageUtil.loadImageResource(ToaGearCheckPanel.class, "tob.png");
		tfaButton = ImageUtil.loadImageResource(ToaGearCheckPanel.class, "tfa.png");
	}
	
	@Override
	protected void shutDown() throws Exception
	{
		if (navButton != null)
		{
			pluginToolbar.removeNavigation(navButton);
		}
	}
	
	Set<Integer> animationList = ImmutableSet.of(855, 856, 858, 859);
	
	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		if (!(event.getActor() instanceof Player) || !animationList.contains(event.getActor().getAnimation()))
		{
			return;
		}
		
		String s;
		Role r;
		
		switch (event.getActor().getAnimation())
		{
			case 855:
				s = "sfrz"; //Yes emote
				r = Role.SFRZ;
				break;
			case 856:
				s = "nfrz"; //No emote
				r = Role.NFRZ;
				break;
			case 858:
				s = "rdps"; //Bow emote
				r = Role.RDPS;
				break;
			case 859:
				s = "mdps"; //Angry emote
				r = Role.MDPS;
				break;
			default:
				return;
		}
		
		String player = event.getActor().getName();
		PlayerInfo chatMemory = getPlayerInfo(event.getActor().getName());
		chatMemory.addChatMessage(s);
		chatMemory.setRole(r);
		playerInfo.put(player, chatMemory);
	}
	
	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.PUBLICCHAT)
		{
			return;
		}
		
		String player = Text.sanitize(event.getName());
		String message = event.getMessage();
		
		PlayerInfo chatMemory = getPlayerInfo(player);
		chatMemory.addChatMessage(message);
		chatMemory.setRole(Role.getRole(message));
		
		playerInfo.put(player, chatMemory);
	}
	
	private PlayerInfo getPlayerInfo(String player)
	{
		return playerInfo.containsKey(player) ? playerInfo.get(player) : new PlayerInfo();
	}
	
	@Subscribe
	private void onWidgetLoaded(WidgetLoaded event)
	{
		if (RaidInfo.widgetAccess(event.getGroupId()))
		{
			checkingApplicants = true;
			refreshPanel();
			if (navButtonAdded)
			{
				SwingUtilities.invokeLater(() -> pluginToolbar.openPanel(navButton));
			}
		}
	}
	
	@Subscribe
	private void onWidgetClosed(WidgetClosed event)
	{
		if (RaidInfo.widgetAccess(event.getGroupId()))
		{
			checkingApplicants = false;
		}
	}
	
	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}
		int newRegionId = client.getLocalPlayer().getWorldLocation().getRegionID();
		RaidInfo raidInfo = RaidInfo.inRegion(newRegionId);
		boolean isInRaidLobby = raidInfo != null;
		
		if (isInRaidLobby)
		{
			if (!navButtonAdded)
			{
				navButton = buildNavIcon(raidInfo);
				pluginToolbar.addNavigation(navButton);
				navButtonAdded = true;
			}
			else if (newRegionId != regionId)
			{ // If teleporting directly between raid lobbies
				pluginToolbar.removeNavigation(navButton);
				navButton = buildNavIcon(raidInfo);
				pluginToolbar.addNavigation(navButton);
			}
		}
		else if (navButtonAdded)
		{
			pluginToolbar.removeNavigation(navButton);
			navButtonAdded = false;
			playerInfo.clear();
		}
		
		regionId = newRegionId;
		
		refreshPanel();
	}
	
	public NavigationButton buildNavIcon(RaidInfo raidInfo)
	{
		String tooltip = " Party Gear Checker";
		BufferedImage icon = null;
		if (raidInfo == RaidInfo.ToA)
		{
			icon = toaButton;
			tooltip = "ToA" + tooltip;
		}
		else if (raidInfo == RaidInfo.ToB)
		{
			icon = tobButton;
			tooltip = "ToB" + tooltip;
		}
		else if (raidInfo == RaidInfo.TFA)
		{
			icon = tfaButton;
			tooltip = "TFA" + tooltip;
		}
		
		return NavigationButton.builder()
				.tooltip(tooltip)
				.icon(icon)
				.priority(10)
				.panel(raidGearCheckPanel)
				.build();
	}
	
	public void refreshPanel()
	{
		if (!checkingApplicants)
		{
			return;
		}
		
		RaidInfo raidInfo = RaidInfo.inRegion(regionId);
		
		if (raidInfo == null)
		{
			return;
		}
		
		List<String> playerListStr = raidInfo.getPlayers(client);
		HashMap<Player, List<ItemComposition>> playerList = new HashMap<>();
		
		for (Player player : client.getPlayers())
		{
			if (player.getName() == null || !playerListStr.contains(Text.standardize(player.getName())))
			{
				continue;
			}
			List<ItemComposition> listOfEquipment = new ArrayList<>();
			PlayerComposition playerComposition = player.getPlayerComposition();
			for (KitType kitType : KitType.values())
			{
				if (playerComposition.getEquipmentId(kitType) == -1)
				{
					continue;
				}
				clientThread.invoke(() ->
				{
					listOfEquipment.add(itemManager.getItemComposition(playerComposition.getEquipmentId(kitType)));
				});
			}
			playerList.put(player, listOfEquipment);
		}
		raidGearCheckPanel.updatePanel(playerList, playerInfo);
	}
}
