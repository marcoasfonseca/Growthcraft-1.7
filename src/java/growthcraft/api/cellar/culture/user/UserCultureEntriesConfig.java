/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015, 2016 IceDragon200
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package growthcraft.api.cellar.culture.user;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;

import growthcraft.api.cellar.CellarRegistry;
import growthcraft.api.core.schema.ItemKeySchema;
import growthcraft.api.core.schema.MultiFluidStackSchema;
import growthcraft.api.core.util.BiomeUtils;
import growthcraft.api.core.util.JsonConfigDef;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BiomeDictionary;

/**
 * This allows users to define new yeast entries and map them to a biome
 * for generation in the Ferment Jar.
 */
public class UserCultureEntriesConfig extends JsonConfigDef
{
	private final UserCultureEntries defaultEntries = new UserCultureEntries();
	private UserCultureEntries entries;

	@Override
	protected String getDefault()
	{
		final ItemKeySchema brewersYeast = new ItemKeySchema("Growthcraft|Cellar", "grc.yeast", 1, 0);
		brewersYeast.setComment("Brewers Yeast");

		final ItemKeySchema lagerYeast = new ItemKeySchema("Growthcraft|Cellar", "grc.yeast", 1, 1);
		lagerYeast.setComment("Lager Yeast");

		final ItemKeySchema etherealYeast = new ItemKeySchema("Growthcraft|Cellar", "grc.yeast", 1, 3);
		etherealYeast.setComment("Ethereal Yeast");

		final MultiFluidStackSchema schema = MultiFluidStackSchema.newWithTags(40, "young");
		schema.setComment("Any fluid tagged with `young`");

		final UserCultureEntry brewers = new UserCultureEntry(brewersYeast, schema, new ArrayList<String>());
		brewers.setComment("Brewers yeast is the default yeast, which appears in all other biomes that are filled by the Lager or Ethereal");

		final UserCultureEntry lager = new UserCultureEntry(lagerYeast, schema, new ArrayList<String>());
		lager.setComment("Lager yeast is found in COLD biomes, think snow places!");

		final UserCultureEntry ethereal = new UserCultureEntry(etherealYeast, schema, new ArrayList<String>());
		ethereal.setComment("Ethereal yeast is found in MAGICAL or MUSHROOM biomes, because its special");

		for (BiomeDictionary.Type biomeType : BiomeDictionary.Type.values())
		{
			switch (biomeType)
			{
				case COLD:
					lager.biome_types.add(biomeType.name());
					break;
				case MAGICAL:
				case MUSHROOM:
					ethereal.biome_types.add(biomeType.name());
					break;
				default:
					brewers.biome_types.add(biomeType.name());
			}
		}
		defaultEntries.data.add(brewers);
		defaultEntries.data.add(lager);
		defaultEntries.data.add(ethereal);
		defaultEntries.setComment("Default Yeast Config");
		return gson.toJson(defaultEntries);
	}

	@Override
	protected void loadFromBuffer(BufferedReader reader)
	{
		this.entries = gson.fromJson(reader, UserCultureEntries.class);
	}

	private void addYeastEntry(UserCultureEntry entry)
	{
		if (entry == null)
		{
			logger.error("Culture entry was invalid.");
			return;
		}

		if (entry.item == null || entry.item.isInvalid())
		{
			logger.error("Culture item was invalid {%s}", entry);
			return;
		}

		if (entry.fluids == null || entry.fluids.isInvalid())
		{
			logger.error("Culture fluids was invalid {%s}", entry);
			return;
		}

		for (ItemStack itemstack : entry.item.getItemStacks())
		{
			for (FluidStack fluidstack : entry.fluids.getFluidStacks())
			{
				if (entry.biome_names != null)
				{
					for (String biomeName : entry.biome_names)
					{
						CellarRegistry.instance().culture().addCultureRecipeToBiomeByName(itemstack, fluidstack, biomeName);
						logger.info("Added user culture {%s} to biome '%s' with fluid {%s}", itemstack, biomeName, fluidstack);
					}
				}

				if (entry.biome_types != null)
				{
					for (String biome : entry.biome_types)
					{
						try
						{
							final BiomeDictionary.Type biomeType = BiomeUtils.fetchBiomeType(biome);
							CellarRegistry.instance().culture().addCultureRecipeToBiomeType(itemstack, fluidstack, biomeType);
							logger.info("Added user culture {%s} to biome type '%s' with fluid {%s}", itemstack, biome, fluidstack);
						}
						catch (BiomeUtils.BiomeTypeNotFound ex)
						{
							logger.error("A biome type '%s' for entry {%s} could not be found.", biome, entry);
						}
					}
				}
			}
		}
	}

	@Override
	public void postInit()
	{
		if (entries != null)
		{
			if (entries.data != null)
			{
				logger.info("Adding %d culture entries.", entries.data.size());
				for (UserCultureEntry entry : entries.data) addYeastEntry(entry);
			}
			else
			{
				logger.error("Invalid culture entries data");
			}
		}
	}
}
