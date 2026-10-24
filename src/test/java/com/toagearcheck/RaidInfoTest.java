package com.toagearcheck;

import java.util.Arrays;
import java.util.Collections;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RaidInfoTest
{
	private Widget child(int index, String text, int width, int height)
	{
		Widget widget = mock(Widget.class);
		when(widget.getIndex()).thenReturn(index);
		when(widget.getText()).thenReturn(text);
		when(widget.getOriginalWidth()).thenReturn(width);
		when(widget.getOriginalHeight()).thenReturn(height);
		return widget;
	}

	private Client client(int group, int child, Widget... children)
	{
		Client client = mock(Client.class);
		Widget list = mock(Widget.class);
		when(client.getWidget(group, child)).thenReturn(list);
		when(list.getChildren()).thenReturn(children);
		return client;
	}

	@Test
	public void recognisesOnlySupportedLobbiesAndInterfaces()
	{
		assertSame(RaidInfo.ToA, RaidInfo.inRegion(13454));
		assertSame(RaidInfo.ToB, RaidInfo.inRegion(14642));
		assertSame(RaidInfo.TFA, RaidInfo.inRegion(16470));
		assertNull(RaidInfo.inRegion(16471)); // Archive Guardian, not the lobby
		assertTrue(RaidInfo.widgetAccess(InterfaceID.TFA_PARTYDETAILS));
		assertFalse(RaidInfo.widgetAccess(InterfaceID.TFA_PARTYLIST));
	}

	@Test
	public void toaStillSelectsNamesByDimensions()
	{
		Client client = client(774, 48, child(0, "Alice", 114, 22), child(1, "99", 30, 22));
		assertEquals(Collections.singletonList("alice"), RaidInfo.ToA.getPlayers(client));
	}

	@Test
	public void tobStillSelectsNamesByDimensions()
	{
		Client client = client(50, 42, child(0, "Bob", 160, 13), child(1, "99", 30, 13));
		assertEquals(Collections.singletonList("bob"), RaidInfo.ToB.getPlayers(client));
	}

	@Test
	public void tfaReadsNameColumnsFromMultipleApplicantRows()
	{
		Widget[] children = new Widget[44];
		children[0] = child(0, "Background", 0, 0);
		children[1] = child(1, "<col=00ff00><img=32> Alice</col>", 100, 22);
		children[2] = child(2, "126", 30, 22);
		children[15] = child(15, "Not an applicant", 100, 22);
		children[23] = child(23, "BOB\u00a0SMITH", 100, 22);
		Client client = client(InterfaceID.TFA_PARTYDETAILS, 52, children);
		assertEquals(Arrays.asList("alice", "bob smith"), RaidInfo.TFA.getPlayers(client));
		verify(client, never()).getWidget(InterfaceID.TFA_PARTYDETAILS, 35); // Members list
	}

	@Test
	public void ignoresEmptyAndUnpopulatedApplicantSlots()
	{
		Client client = client(InterfaceID.TFA_PARTYDETAILS, 52,
			null, child(1, "<col=ffffff> </col>", 0, 0), child(23, null, 0, 0));
		assertTrue(RaidInfo.TFA.getPlayers(client).isEmpty());
	}

	@Test
	public void missingListIsEmpty()
	{
		assertTrue(RaidInfo.TFA.getPlayers(mock(Client.class)).isEmpty());
	}

	@Test
	public void missingChildrenAreEmpty()
	{
		Client client = client(InterfaceID.TFA_PARTYDETAILS, 52, (Widget[]) null);
		assertTrue(RaidInfo.TFA.getPlayers(client).isEmpty());
	}

	@Test
	public void hiddenApplicantsDoNotLeakIntoMemberTab()
	{
		Client client = client(InterfaceID.TFA_PARTYDETAILS, 52, child(1, "Alice", 0, 0));
		when(client.getWidget(InterfaceID.TFA_PARTYDETAILS, 52).isHidden()).thenReturn(true);
		assertTrue(RaidInfo.TFA.getPlayers(client).isEmpty());
	}
}
