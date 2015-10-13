package growthcraft.cellar.container;

import growthcraft.core.Utils;
import growthcraft.api.cellar.CellarRegistry;
import growthcraft.cellar.tileentity.TileEntityBrewKettle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBrewKettle extends Container
{
	private TileEntityBrewKettle te;

	public static class SlotId
	{
		public static final int RAW = 0;
		public static final int RESIDUE = 1;
		public static final int BUCKET_INPUT = 2;
		public static final int BUCKET_OUTPUT = 3;
		public static final int PLAYER_BACKPACK_START = 4;
		public static final int PLAYER_BACKPACK_END = PLAYER_BACKPACK_START + 27;
		public static final int PLAYER_INVENTORY_START = PLAYER_BACKPACK_END;
		public static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 9;

		private SlotId() {}
	}

	public ContainerBrewKettle(InventoryPlayer player, TileEntityBrewKettle brewKettle)
	{
		this.te = brewKettle;
		this.addSlotToContainer(new Slot(te, SlotId.RAW, 80, 35));
		this.addSlotToContainer(new SlotBrewKettleResidue(this, te, SlotId.RESIDUE, 141, 17));
		this.addSlotToContainer(new Slot(te, SlotId.BUCKET_INPUT, 8, 17));
		this.addSlotToContainer(new Slot(te, SlotId.BUCKET_OUTPUT, 8, 53));
		int i;

		for (i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (i = 0; i < 9; ++i)
		{
			this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return this.te.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index)
	{
		ItemStack itemstack = null;
		final Slot slot = (Slot)this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			final ItemStack stack = slot.getStack();
			itemstack = stack.copy();

			if (index == SlotId.RESIDUE)
			{
				if (!this.mergeItemStack(stack, SlotId.PLAYER_BACKPACK_START, SlotId.PLAYER_INVENTORY_END, true))
				{
					return null;
				}

				slot.onSlotChange(stack, itemstack);
			}
			else if (index == SlotId.RAW)
			{
				if (CellarRegistry.instance().brew().isItemBrewingIngredient(stack))
				{
					if (!this.mergeItemStack(stack, SlotId.RAW, SlotId.RAW + 1, false))
					{
						return null;
					}
				}
			}
			else if (index == SlotId.BUCKET_INPUT)
			{
				if (!this.mergeItemStack(stack, SlotId.BUCKET_INPUT, SlotId.BUCKET_INPUT + 1, false))
				{
					return null;
				}
			}
			else if (Utils.between(index, SlotId.PLAYER_BACKPACK_START, SlotId.PLAYER_BACKPACK_END))
			{
				if (!this.mergeItemStack(stack, SlotId.PLAYER_INVENTORY_START, SlotId.PLAYER_INVENTORY_END, false))
				{
					return null;
				}
			}
			else if (Utils.between(index, SlotId.PLAYER_INVENTORY_START, SlotId.PLAYER_INVENTORY_END) && !this.mergeItemStack(stack, SlotId.PLAYER_BACKPACK_START, SlotId.PLAYER_BACKPACK_END, false))
			{
				return null;
			}
			else if (!this.mergeItemStack(stack, SlotId.PLAYER_BACKPACK_START, SlotId.PLAYER_INVENTORY_END, false))
			{
				return null;
			}

			if (stack.stackSize == 0)
			{
				slot.putStack((ItemStack)null);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (stack.stackSize == itemstack.stackSize)
			{
				return null;
			}

			slot.onPickupFromSlot(player, stack);
		}

		return itemstack;
	}

	// crafters
	@Override
	public void addCraftingToCrafters(ICrafting iCrafting)
	{
		super.addCraftingToCrafters(iCrafting);
		te.sendGUINetworkData(this, iCrafting);
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		for (int i = 0; i < crafters.size(); i++)
		{
			te.sendGUINetworkData(this, (ICrafting)crafters.get(i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int v)
	{
		te.getGUINetworkData(id, v);
	}
}
