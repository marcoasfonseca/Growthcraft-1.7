package growthcraft.cellar.container;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;

/* Generic container for placing fluid containers in the gui */
public class SlotItemFluidContainer extends Slot
{
	final Container con;

	public SlotItemFluidContainer(Container cont, IInventory inv, int x, int y, int z)
	{
		super(inv, x, y, z);
		this.con = cont;
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return FluidContainerRegistry.isContainer(stack);
	}
}
