package com.toagearcheck;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Singleton
public class ToaGearCheckPanel extends PluginPanel
{
	@Inject
	private Client client;
	@Inject
	private ItemManager itemManager;
	
	private final JTabbedPane partyTabs;
	
	public ToaGearCheckPanel()
	{
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		
		JPanel partyApplicants = new JPanel(new GridBagLayout());
		partyTabs = new JTabbedPane();
		
		GridBagConstraints tabConstraints = new GridBagConstraints();
		tabConstraints.fill = GridBagConstraints.BOTH;
		tabConstraints.weightx = 1;
		tabConstraints.weighty = 1;
		
		partyApplicants.add(partyTabs, tabConstraints);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(partyApplicants)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(partyApplicants)
		);
	}
	
	
	public void updatePanel(HashMap<Player, List<ItemComposition>> playerList, HashMap<String, PlayerInfo> allPlayerInfo)
	{
		SwingUtilities.invokeLater(() ->
		{
			if (playerList.isEmpty())
			{
				partyTabs.removeAll();
				return;
			}
			
			String lastUserSelected = partyTabs.getSelectedIndex() != -1
					? partyTabs.getTitleAt(partyTabs.getSelectedIndex())
					: "";
			
			final GridBagConstraints constraints = createGridBagConstraints();
			
			partyTabs.removeAll();
			
			playerList.forEach((player, items) ->
			{
				String playerName = player.getName();
				PlayerInfo playerInfo = allPlayerInfo.getOrDefault(playerName, new PlayerInfo());
				ArrayList<String> messages = playerInfo.getList();
				
				JPanel equipmentPanel = createEquipmentPanel(items, messages, constraints);
				
				String fullName = playerName + playerInfo.getRole().getShortName();
				partyTabs.addTab(fullName, equipmentPanel);
				
				if (fullName.equals(lastUserSelected))
				{
					partyTabs.setSelectedComponent(equipmentPanel);
				}
			});
			
			repaint();
			revalidate();
		});
	}
	
	private GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		return c;
	}
	
	private JPanel createEquipmentPanel(List<ItemComposition> items, List<String> messages,
										GridBagConstraints constraints)
	{
		JPanel panel = new JPanel(new GridBagLayout());
		int gridY = 0;
		
		for (ItemComposition item : items)
		{
			AsyncBufferedImage itemImage = itemManager.getImage(item.getId());
			constraints.gridy = gridY++;
			panel.add(new ItemPanel(item, itemImage), constraints);
		}
		
		constraints.gridy = gridY++;
		panel.add(createLabel("[Ascending order of messages]"), constraints);
		
		for (int i = 0; i < 5; i++)
		{
			String message = "[" + i + "] " + (i < messages.size() ? messages.get(i) : "");
			constraints.gridy = gridY++;
			panel.add(createLabel(message), constraints);
		}
		
		return panel;
	}
	
	private JLabel createLabel(String text)
	{
		JLabel chatLabel = new JLabel(text);
		chatLabel.setForeground(Color.WHITE);
		return chatLabel;
	}
}
