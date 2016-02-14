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
package growthcraft.api.cellar.culture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import growthcraft.api.core.log.ILogger;
import growthcraft.api.core.log.NullLogger;
import growthcraft.api.core.util.ItemKey;
import growthcraft.api.core.util.FluidTest;

import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.FluidStack;

public class CultureRegistry implements ICultureRegistry
{
	private static class FluidToCultureRecipeMap extends HashMap<Fluid, List<ICultureRecipe>>
	{
		public static final long serialVersionUID = 1L;
	}

	private static class FluidCultureRecipeTree<T> extends HashMap<T, FluidToCultureRecipeMap>
	{
		public static final long serialVersionUID = 1L;

		public void putEntry(T type, Fluid fluid, ICultureRecipe recipe)
		{
			if (!containsKey(type))
			{
				put(type, new FluidToCultureRecipeMap());
			}
			final FluidToCultureRecipeMap m = get(type);
			if (!m.containsKey(fluid))
			{
				m.put(fluid, new ArrayList<ICultureRecipe>());
			}
			m.get(fluid).add(recipe);
		}

		public List<ICultureRecipe> getEntry(T type, Fluid fluid)
		{
			final FluidToCultureRecipeMap m = get(type);
			if (m != null) return m.get(fluid);
			return null;
		}
	}

	private static class BiomeTypeTree extends FluidCultureRecipeTree<BiomeDictionary.Type>
	{
		public static final long serialVersionUID = 1L;
	}

	private static class BiomeNameTree extends FluidCultureRecipeTree<String>
	{
		public static final long serialVersionUID = 1L;
	}

	private BiomeTypeTree biomeTypeRecipeTree = new BiomeTypeTree();
	private BiomeNameTree biomeNameRecipeTree = new BiomeNameTree();
	private Set<ItemKey> cultureList = new HashSet<ItemKey>();
	private Map<BiomeDictionary.Type, Set<ItemStack>> biomeTypeToCulture = new HashMap<BiomeDictionary.Type, Set<ItemStack>>();
	private Map<String, Set<ItemStack>> biomeNameToCulture = new HashMap<String, Set<ItemStack>>();
	private Map<ItemKey, Set<BiomeDictionary.Type>> cultureToBiomeType = new HashMap<ItemKey, Set<BiomeDictionary.Type>>();
	private Map<ItemKey, Set<String>> cultureToBiomeName = new HashMap<ItemKey, Set<String>>();
	private ILogger logger = NullLogger.INSTANCE;

	@Override
	public void setLogger(@Nonnull ILogger l)
	{
		this.logger = l;
	}

	private ItemKey stackToKey(@Nonnull ItemStack stack)
	{
		return new ItemKey(stack);
	}

	@Override
	public void addCulture(@Nonnull ItemStack culture)
	{
		cultureList.add(stackToKey(culture));
	}

	@Override
	public boolean isCulture(@Nullable ItemStack culture)
	{
		if (culture == null) return false;
		if (culture.getItem() == null) return false;
		return cultureList.contains(stackToKey(culture));
	}

	@Override
	public List<ICultureRecipe> getRecipesForBiomeType(@Nullable FluidStack fluid, @Nullable BiomeDictionary.Type type)
	{
		if (fluid == null || type == null) return null;
		final Map<Fluid, List<ICultureRecipe>> m = biomeTypeRecipeTree.get(type)
		if (m == null) return null;
		return m.get(fluid);
	}

	@Override
	public List<ICultureRecipe> getRecipesForBiomeName(@Nullable FluidStack fluid, @Nullable String name)
	{
		if (fluid == null || name == null) return null;
		final Map<Fluid, List<ICultureRecipe>> m = biomeNameRecipeTree.get(name)
		if (m == null) return null;
		return m.get(fluid);
	}

	@Override
	public void addCultureToBiomeType(@Nonnull ItemStack culture, @Nonnull BiomeDictionary.Type type)
	{
		addCulture(culture);
		if (!biomeTypeToCulture.containsKey(type))
		{
			logger.debug("Initializing biome type to culture set for %s", type);
			biomeTypeToCulture.put(type, new HashSet<ItemStack>());
		}
		final ItemKey cultureKey = stackToKey(culture);
		if (!cultureToBiomeType.containsKey(cultureKey))
		{
			logger.debug("Initializing culture to biome type set for %s", culture);
			cultureToBiomeType.put(cultureKey, new HashSet<BiomeDictionary.Type>());
		}
		biomeTypeToCulture.get(type).add(culture);
		cultureToBiomeType.get(cultureKey).add(type);
	}

	@Override
	public void addCultureToBiomeByName(@Nonnull ItemStack culture, @Nonnull String name)
	{
		addCulture(culture);
		final ItemKey cultureKey = stackToKey(culture);
		if (!cultureToBiomeName.containsKey(cultureKey))
		{
			logger.debug("Initializing culture to biome name set for %s", culture);
			cultureToBiomeName.put(cultureKey, new HashSet<String>());
		}
		cultureToBiomeName.get(cultureKey).add(name);
		if (!biomeNameToCulture.containsKey(name))
		{
			logger.debug("Initializing biome name to culture set for %s", name);
			biomeNameToCulture.put(name, new HashSet<ItemStack>());
		}
		biomeNameToCulture.get(name).add(culture);
	}

	@Override
	public void addCultureRecipeToBiomeType(@Nonnull ICultureRecipe recipe, @Nonnull BiomeDictionary.Type type)
	{
		biomeTypeRecipeTree.putEntry(type, recipe.getFluid(), recipe);
		addCultureToBiomeByType(recipe.getItemStack(), type);
	}

	@Override
	public void addCultureRecipeToBiomeByName(@Nonnull ICultureRecipe recipe, @Nonnull String name)
	{
		biomeNameRecipeTree.putEntry(type, recipe.getFluid(), recipe);
		addCultureToBiomeByName(recipe.getItemStack(), name);
	}

	private ICultureRecipe testRecipe(ICultureRecipe recipe, @Nonnull FluidStack stack)
	{
		if (FluidTest.hasEnough(recipe.getFluidStack(), stack))
		{
			return recipe;
		}
		return null;
	}

	@Override
	public List<ICultureRecipe> getCultureRecipesForBiomeType(@Nonnull BiomeDictionary.Type type, @Nullable FluidStack stack)
	{
		if (stack == null) return null;
		return testRecipe(biomeTypeRecipeTree.getEntry(type, stack.getFluid()), stack);
	}

	@Override
	public List<ICultureRecipe> getCultureRecipesForBiomeByName(@Nonnull String name, @Nullable FluidStack stack)
	{
		if (stack == null) return null;
		return testRecipe(biomeNameRecipeTree.getEntry(name, stack.getFluid()), stack);
	}

	@Override
	public Set<ItemStack> getCultureListForBiomeType(@Nonnull BiomeDictionary.Type type)
	{
		return biomeTypeToCulture.get(type);
	}

	@Override
	public Set<ItemStack> getCultureListForBiomeName(@Nonnull String type)
	{
		return biomeNameToCulture.get(type);
	}

	@Override
	public Set<String> getBiomeNamesForCulture(@Nullable ItemStack culture)
	{
		if (culture == null) return null;
		return cultureToBiomeName.get(stackToKey(culture));
	}

	@Override
	public Set<BiomeDictionary.Type> getBiomeTypesForCulture(@Nullable ItemStack culture)
	{
		if (culture == null) return null;
		return cultureToBiomeType.get(stackToKey(culture));
	}

	@Override
	public boolean canCultureFormInBiome(@Nullable ItemStack culture, @Nullable FluidStack flustack, @Nullable BiomeGenBase biome)
	{
		if (culture == null || biome == null || fluid == null) return false;

		final Set<String> biomeNames = getBiomeNamesForCulture(culture);
		if (biomeNames != null)
		{
			if (biomeNames.contains(biome.biomeName)) return true;
		}

		final Set<BiomeDictionary.Type> cultureBiomeList = getBiomeTypesForCulture(culture);
		if (cultureBiomeList != null)
		{
			for (BiomeDictionary.Type t : BiomeDictionary.getTypesForBiome(biome))
			{
				if (cultureBiomeList.contains(t)) return true;
			}
		}

		return false;
	}
}
