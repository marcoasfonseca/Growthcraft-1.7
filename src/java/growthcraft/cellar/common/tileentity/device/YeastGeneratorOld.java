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
package growthcraft.cellar.common.tileentity.device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import growthcraft.api.cellar.booze.BoozeTag;
import growthcraft.api.cellar.CellarRegistry;
import growthcraft.api.cellar.culture.ICultureRegistry;
import growthcraft.api.cellar.culture.ICultureRecipe;
import growthcraft.cellar.common.tileentity.TileEntityCellarDevice;
import growthcraft.core.common.tileentity.device.DeviceFluidSlot;
import growthcraft.core.common.tileentity.device.DeviceInventorySlot;
import growthcraft.core.common.tileentity.device.DeviceProgressive;
import growthcraft.core.util.ItemUtils;
import growthcraft.api.core.util.PulseStepper;

import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeDictionary;

public class YeastGenerator extends DeviceProgressive
{
	protected int consumption = 1200 / 16;
	protected DeviceFluidSlot fluidSlot;
	protected DeviceInventorySlot invSlot;
	protected List<ItemStack> tempItemList = new ArrayList<ItemStack>();
	protected List<ICultureRecipe> tempRecipeList = new ArrayList<ICultureRecipe>();
	protected PulseStepper stepper = new PulseStepper(20, 1);

	/**
	 * @param te - parent tile entity
	 * @param fluidSlotIndex - fluid slot id to use in parent
	 *             Fluid will be used from this slot
	 * @param invSlotIndex - inventory slot id to use in parent
	 *             Yeast will be generated into this slot
	 */
	public YeastGenerator(TileEntityCellarDevice te, int fluidSlotIndex, int invSlotIndex)
	{
		super(te);
		this.fluidSlot = new DeviceFluidSlot(te, fluidSlotIndex);
		this.invSlot = new DeviceInventorySlot(te, invSlotIndex);
		setTimeMax(1200);
	}

	/**
	 * How many fluid units are consumed per yeast gen?
	 *
	 * @param c - fluid consumption in milli-buckets
	 */
	public YeastGenerator setConsumption(int c)
	{
		this.consumption = c;
		return this;
	}

	/**
	 * Returns the current biome of the Yeast Generator's parent TileEntity.
	 *
	 * @return biome
	 */
	public BiomeGenBase getCurrentBiome()
	{
		return getWorld().getBiomeGenForCoords(parent.xCoord, parent.zCoord);
	}

	/**
	 * Determines if the given item stack can be replicated as a yeast item
	 *
	 * @param stack - item stack to test
	 * @return true, it can be replicated, false otherwise
	 */
	public boolean canReplicateYeast(ItemStack stack)
	{
		// prevent production if the stack size is currently maxed
		if (stack.stackSize >= stack.getMaxStackSize()) return false;
		// prevent pointless ticking with invalid items
		if (!CellarRegistry.instance().culture().isCulture(stack)) return false;
		return true;
	}

	/**
	 * Determines if the jar can produce any yeast
	 *
	 * @return recipe, the generator can produce yeast, null otherwise
	 */
	private boolean canWork()
	{
		final ItemStack yeastItem = invSlot.get();
		// we can ignore null items, this will fallback to the initProduceYeast
		if (yeastItem != null)
		{
			if (!canReplicateYeast(yeastItem)) return true;
		}
		return true;
	}

	private void consumeFluid()
	{
		fluidSlot.consume(consumption, true);
		markForBlockUpdate();
	}

	/**
	 * This is called to initialize the yeast slot, a random yeast type is
	 * chosen from the various biome types and set in the slot,
	 * any further yeast production will be of the same type.
	 */
	private void initProduceYeast(List<ICultureRecipe> recipes)
	{
		tempItemList.clear();
		final BiomeGenBase biome = getCurrentBiome();
		if (biome != null)
		{
			final ICultureRegistry reg = CellarRegistry.instance().yeast();

			{
				final Collection<ItemStack> yl = reg.getYeastListForBiomeName(biome.biomeName);
				if (yl != null)
				{
					tempItemList.addAll(yl);
				}
			}

			for (Type t : BiomeDictionary.getTypesForBiome(biome))
			{
				final Collection<ItemStack> yeastList = reg.getYeastListForBiomeType(t);
				if (yeastList != null)
				{
					tempItemList.addAll(yeastList);
				}
			}

			if (tempItemList.size() > 0)
			{
				final ItemStack result = tempItemList.get(random.nextInt(tempItemList.size())).copy();
				invSlot.set(result);
				consumeFluid();
			}
		}
	}

	private void produceYeast(List<ICultureRecipe> recipes)
	{
		if (invSlot.isEmpty())
		{
			initProduceYeast(recipes);
		}
		else
		{
			final ItemStack contents = invSlot.get();
			// ensure that the item is indeed some form of yeast, prevents item duplication
			// while canProduceYeast will prevent invalid items from popping up
			// produceYeast is public, and can be called outside the update
			// logic to force yeast generation, as such, this must ensure
			// item correctness
			if (canReplicateYeast(contents))
			{
				invSlot.set(ItemUtils.increaseStack(contents));
				consumeFluid();
			}
		}
	}

	public void update()
	{
		boolean failed = false;
		if (stepper.update() == PulseStepper.State.NONE)
		{
			final List<ICultureRecipe> recipes = getWorkingRecipe();
			if (recipes != null && !recipes.isEmpty())
			{
				increaseTime();
				if (time >= timeMax)
				{
					resetTime();
					produceYeast(recipes);
					markForInventoryUpdate();
				}
			}
			else
			{
				failed = true;
				stepper.reset();
			}
		}

		if (failed)
		{
			if (resetTime()) markForInventoryUpdate();
		}
	}
}
