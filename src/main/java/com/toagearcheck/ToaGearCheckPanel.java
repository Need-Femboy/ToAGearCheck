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
	
	
	public void updatePanel(HashMap<Player, List<ItemComposition>> playerList)
	{
		SwingUtilities.invokeLater(() ->
		{
			String lastUserSelected = "";
			
			if (partyTabs.getSelectedIndex() != -1)
			{
				lastUserSelected = partyTabs.getTitleAt(partyTabs.getSelectedIndex());
			}
			
			partyTabs.removeAll();
			
			if (playerList.size() == 0)
			{
				return;
			}
			
			for (Map.Entry<Player, List<ItemComposition>> player : playerList.entrySet())
			{
				JPanel equipmentPanels = new JPanel(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1;
				c.gridx = 0;
				c.gridy = 0;
				
				for (ItemComposition itemComposition : player.getValue())
				{
					AsyncBufferedImage itemImage = itemManager.getImage(itemComposition.getId());
					equipmentPanels.add(new ItemPanel(itemComposition, itemImage), c);
					c.gridy++;
				}
				partyTabs.addTab(player.getKey().getName(), equipmentPanels);
				
				if (player.getKey().getName().equals(lastUserSelected))
				{
					log.info("Setting tab selection to {}", player.getKey().getName());
					partyTabs.setSelectedComponent(equipmentPanels);
				}
			}
			
			
			GridBagConstraints tabConstraints = new GridBagConstraints();
			tabConstraints.fill = GridBagConstraints.BOTH;
			
			repaint();
			revalidate();
		});
	}
}
