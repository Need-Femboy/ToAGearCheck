package com.toagearcheck;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import javax.imageio.ImageIO;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TfaGearCheckTest
{
	private ToAGearCheckPlugin plugin;
	private Client client;
	private ClientToolbar toolbar;
	private ToaGearCheckPanel panel;
	private Player local;

	private void field(String name, Object value) throws Exception
	{
		Field field = ToAGearCheckPlugin.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(plugin, value);
	}

	private void event(String name, Object event) throws Exception
	{
		Method method = ToAGearCheckPlugin.class.getDeclaredMethod(name, event.getClass());
		method.setAccessible(true);
		method.invoke(plugin, event);
	}

	@Before
	public void setUp() throws Exception
	{
		plugin = new ToAGearCheckPlugin();
		client = mock(Client.class);
		toolbar = mock(ClientToolbar.class);
		panel = mock(ToaGearCheckPanel.class);
		local = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(local);
		when(local.getWorldLocation()).thenReturn(new WorldPoint(4096, 5504, 0));
		field("client", client);
		field("pluginToolbar", toolbar);
		field("raidGearCheckPanel", panel);
		for (String raid : new String[]{"toa", "tob", "tfa"})
		{
			BufferedImage icon = ImageIO.read(getClass().getResource("/com/toagearcheck/" + raid + ".png"));
			assertNotNull(icon);
			field(raid + "Button", icon);
		}
	}

	@Test
	public void addsTfaNavigationInLobby() throws Exception
	{
		event("onGameTick", new GameTick());
		ArgumentCaptor<NavigationButton> button = ArgumentCaptor.forClass(NavigationButton.class);
		verify(toolbar).addNavigation(button.capture());
		assertEquals("TFA Party Gear Checker", button.getValue().getTooltip());
		assertNotNull(button.getValue().getIcon());
	}

	@Test
	public void switchesDirectlyFromTfaToTob() throws Exception
	{
		event("onGameTick", new GameTick());
		when(local.getWorldLocation()).thenReturn(new WorldPoint(3648, 3200, 0));
		event("onGameTick", new GameTick());
		ArgumentCaptor<NavigationButton> buttons = ArgumentCaptor.forClass(NavigationButton.class);
		verify(toolbar, times(2)).addNavigation(buttons.capture());
		verify(toolbar).removeNavigation(buttons.getAllValues().get(0));
		assertEquals("ToB Party Gear Checker", buttons.getAllValues().get(1).getTooltip());
	}

	@Test
	public void removesNavigationWhenLeavingLobby() throws Exception
	{
		event("onGameTick", new GameTick());
		when(local.getWorldLocation()).thenReturn(new WorldPoint(3200, 3200, 0));
		event("onGameTick", new GameTick());
		verify(toolbar).removeNavigation(any(NavigationButton.class));
	}

	@Test
	public void missingLocalPlayerDoesNotFailDuringLogin() throws Exception
	{
		when(client.getLocalPlayer()).thenReturn(null);
		event("onGameTick", new GameTick());
		verifyNoInteractions(toolbar);
	}

	@Test
	public void readsWornGearForNearbyApplicantWithFormattedName() throws Exception
	{
		event("onGameTick", new GameTick());
		Widget list = mock(Widget.class);
		Widget name = mock(Widget.class);
		when(client.getWidget(InterfaceID.TFA_PARTYDETAILS, 52)).thenReturn(list);
		when(list.getChildren()).thenReturn(new Widget[]{null, name});
		when(name.getIndex()).thenReturn(1);
		when(name.getText()).thenReturn("<col=00ff00><img=32> ALICE</col>");
		Player applicant = mock(Player.class);
		when(applicant.getName()).thenReturn("Alice");
		PlayerComposition equipment = mock(PlayerComposition.class);
		when(applicant.getPlayerComposition()).thenReturn(equipment);
		when(equipment.getEquipmentId(any(KitType.class))).thenReturn(-1);
		when(equipment.getEquipmentId(KitType.WEAPON)).thenReturn(11802);
		Player bystander = mock(Player.class);
		when(bystander.getName()).thenReturn("Bystander");
		when(client.getPlayers()).thenReturn(java.util.Arrays.asList(applicant, bystander));
		ItemManager items = mock(ItemManager.class);
		ItemComposition weapon = mock(ItemComposition.class);
		when(items.getItemComposition(11802)).thenReturn(weapon);
		field("itemManager", items);
		ClientThread clientThread = mock(ClientThread.class);
		doAnswer(invocation -> { ((Runnable) invocation.getArgument(0)).run(); return null; })
			.when(clientThread).invoke(any(Runnable.class));
		field("clientThread", clientThread);
		WidgetLoaded opened = new WidgetLoaded();
		opened.setGroupId(InterfaceID.TFA_PARTYDETAILS);
		event("onWidgetLoaded", opened);
		ArgumentCaptor<HashMap> shown = ArgumentCaptor.forClass(HashMap.class);
		verify(panel).updatePanel(shown.capture(), any(HashMap.class));
		assertEquals(Collections.singletonList(weapon), shown.getValue().get(applicant));
		assertEquals(1, shown.getValue().size());
	}
}
