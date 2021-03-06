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
package growthcraft.api.cellar.booze;

import java.util.Collection;
import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import growthcraft.api.core.fluids.FluidTag;
import growthcraft.api.core.log.ILoggable;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public interface IBoozeRegistry extends ILoggable
{
	IModifierFunction getModifierFunction(@Nullable FluidTag fluid);
	void setModifierFunction(@Nonnull FluidTag fluid, IModifierFunction func);

	Collection<BoozeEntry> getBoozeEntries();
	void registerBooze(@Nonnull Fluid fluid);
	BoozeEntry getBoozeEntry(@Nullable Fluid fluid);
	BoozeEntry fetchBoozeEntry(@Nullable Fluid fluid);
	BoozeEffect getEffect(@Nullable Fluid fluid);
	boolean isFluidBooze(@Nullable Fluid f);
	boolean isFluidBooze(@Nullable FluidStack fluidStack);

	/**
	 * addBoozeAlternative()
	 *
	 * Adds an alternative fluid to the mod that will act as an alternative for the booze.
	 * You will almost always want to use this if you dont want to go into the trouble of creating boozes.
	 *
	 * Example Usage:
	 *   addBoozeAlternative("short.mead", "grc.honeymead0");
	 *
	 * @param altfluid - The alternate fluid.
	 * @param fluid    - The main fluid/booze.
	 **/
	void addBoozeAlternative(@Nonnull Fluid altfluid, @Nonnull Fluid fluid);
	void addBoozeAlternative(@Nonnull Fluid altfluid, @Nonnull String fluid);
	void addBoozeAlternative(@Nonnull String altfluid, @Nonnull String fluid);

	boolean isAlternateBooze(@Nullable Fluid f);
	Fluid getAlternateBooze(@Nullable Fluid f);

	/**
	 * @param f - source fluid
	 * @return if an alternate booze exists, that will be returned, else returns the fluid passed in
	 */
	Fluid maybeAlternateBooze(@Nullable Fluid f);
	FluidStack maybeAlternateBoozeStack(@Nullable FluidStack stack);
}
