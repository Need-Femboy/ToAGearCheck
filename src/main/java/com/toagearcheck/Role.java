package com.toagearcheck;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

@AllArgsConstructor
public enum Role
{
	SFRZ(" [SF]", ImmutableSet.of("sf", "sfrz", "sfr")),
	NFRZ(" [NF]", ImmutableSet.of("nf", "nfrs", "nfr", "magician of the northern region")),
	RDPS(" [R]", ImmutableSet.of("rdps", "range")),
	MDPS(" [M]", ImmutableSet.of("mdps", "melee")),
	NONE("", null);
	
	@Getter
	private final String shortName;
	
	private final Set<String> roleCalls;
	
	public static Role getRole(String message)
	{
		if (message == null || message.isEmpty())
		{
			return NONE;
		}
		
		String lowerMessage = message.toLowerCase();
		return Arrays.stream(values())
				.filter(role -> role != NONE)
				.filter(role -> role.roleCalls.stream().anyMatch(lowerMessage::contains))
				.findFirst()
				.orElse(NONE);
	}
}
