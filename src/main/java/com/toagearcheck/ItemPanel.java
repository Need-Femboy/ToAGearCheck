//Borrowed from https://github.com/botanicvelious/Equipment-Inspector/blob/master/src/main/java/equipmentinspector/ItemPanel.java
package com.toagearcheck;

import net.runelite.api.ItemComposition;
import net.runelite.api.kit.KitType;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import org.apache.commons.lang3.StringUtils;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ItemPanel extends JPanel
{
	ItemPanel(ItemComposition item, AsyncBufferedImage icon)
	{
		setBorder(new EmptyBorder(3, 3, 3, 3));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		
		JLabel name = new JLabel(item.getName());
		
		JLabel imageLabel = new JLabel();
		icon.addTo(imageLabel);
		
		layout.setVerticalGroup(layout.createParallelGroup()
				.addComponent(imageLabel)
				.addGroup(layout.createSequentialGroup()
						.addComponent(name)
				)
		
		);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(imageLabel)
				.addGap(8)
				.addGroup(layout.createParallelGroup()
						.addComponent(name)
				)
		
		);
		
		
		// AWT's Z order is weird. This put image at the back of the stack
		setComponentZOrder(imageLabel, getComponentCount() - 1);
	}
}
