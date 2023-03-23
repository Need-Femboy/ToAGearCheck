package com.toagearcheck;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@PluginDescriptor(
		name = "ToA Party Gear Checker",
		description = "Checks gear of party applicants",
		tags = {"toa", "raids3", "tombs of amascut"},
		enabledByDefault = false
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
	
	@Getter(AccessLevel.PACKAGE)
	private static final int PartyApplicantWidgetGroup = 774;
	@Getter(AccessLevel.PACKAGE)
	private static final int PartyApplicantWidgetChild = 48;
	
	private NavigationButton navButton;
	
	private boolean navButtonAdded = false;
	private boolean checkingApplicants = false;
	private ToaGearCheckPanel toaGearCheckPanel;
	
	private final int LOBBY = 13454;
	
	@Override
	protected void startUp() throws Exception
	{
		toaGearCheckPanel = injector.getInstance(ToaGearCheckPanel.class);
		
		BufferedImage icon = ImageUtil.loadImageResource(ToAGearCheckPlugin.class, "panelimage.png");
		
		navButton = NavigationButton.builder()
				.tooltip("ToA Party Gear Checker")
				.icon(icon)
				.priority(10)
				.panel(toaGearCheckPanel)
				.build();
	}
	
	@Override
	protected void shutDown() throws Exception
	{
		pluginToolbar.removeNavigation(navButton);
	}
	
	@Subscribe
	private void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == PartyApplicantWidgetGroup)
		{
			checkingApplicants = true;
			refreshPanel();
		}
	}
	
	@Subscribe
	private void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == PartyApplicantWidgetGroup)
		{
			checkingApplicants = false;
		}
	}
	
	@Subscribe
	private void onGameTick(GameTick event)
	{
		Player lp = client.getLocalPlayer();
		boolean inRegion = lp != null && lp.getWorldLocation().getRegionID() == LOBBY;
		
		if (inRegion && !navButtonAdded)
		{
			pluginToolbar.addNavigation(navButton);
		}
		else if (!inRegion)
		{
			pluginToolbar.removeNavigation(navButton);
		}
		
		navButtonAdded = inRegion;
		
		refreshPanel();
	}
	
	public void refreshPanel()
	{
		if (!checkingApplicants)
		{
			return;
		}
		
		Widget[] partyApplicantWidget = client.getWidget(ToAGearCheckPlugin.getPartyApplicantWidgetGroup(), ToAGearCheckPlugin.getPartyApplicantWidgetChild()).getChildren();
		
		if (partyApplicantWidget == null) //Shouldn't ever be null
		{
			return;
		}
		
		Stream<Widget> partyApplicantWidgetChildren = Arrays.stream(partyApplicantWidget).filter(x -> x.getOriginalWidth() == 114 && x.getOriginalHeight() == 22); //Easiest way to filter out the widget containing the player name
		List<String> playerListStr = new ArrayList<>();
		HashMap<Player, List<ItemComposition>> playerList = new HashMap<>();
		
		partyApplicantWidgetChildren.forEach(widget ->
		{
			playerListStr.add(widget.getText());
		});
		
		for (Player player : client.getPlayers())
		{
			if (!playerListStr.contains(player.getName()))
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
		toaGearCheckPanel.updatePanel(playerList);
		
	}
}
