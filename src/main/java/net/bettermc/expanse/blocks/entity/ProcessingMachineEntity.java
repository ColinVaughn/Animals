package net.bettermc.expanse.blocks.entity;

import net.bettermc.expanse.recipe.CookingRecipe;
import net.bettermc.expanse.recipe.ModRecipeType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public abstract class ProcessingMachineEntity extends BlockEntitySetup {
    protected short cookTime;
    protected short cookTimeTotal;

    @Nullable
    protected Item inputItem;
    @Nullable
    protected ItemStack outputStack;

    public ProcessingMachineEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public abstract int getInventorySize();

    @Override
    public abstract long getMaxGeneration();

    @Override
    public abstract long getEnergyPerTick();

    @Override
    public boolean usesEnergy() {
        return true;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.cookTime = nbt.getShort("CookTime");
        this.cookTimeTotal = nbt.getShort("CookTimeTotal");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putShort("CookTime", this.cookTime);
        nbt.putShort("CookTimeTotal", this.cookTimeTotal);
    }

    public short getCookTime() {
        return this.cookTime;
    }

    public short getCookTimeTotal() {
        return this.cookTimeTotal;
    }

    public void finishCooking() {
        if (this.outputStack != null) {
            int size = this.outputStack.getCount() + this.getStack(1).getCount();
            this.setStack(1, new ItemStack(this.outputStack.getItem(), size));
        }
        this.stopCooking();
    }

    public void stopCooking() {
        this.cookTime = 0;
        this.cookTimeTotal = 0;
        this.outputStack = null;
        this.inputItem = null;
        this.markDirty();
    }

    public <T extends CookingRecipe> CookingRecipe createRecipe(
        ModRecipeType<T> type, ItemStack testStack,
        boolean checkOutput
    ) {
        this.stopCooking();

        CookingRecipe recipe = type.findFirst(this.world, p -> p.test(testStack));

        if (recipe != null) {

            // Stop if something is already in the output.
            if (checkOutput) {
                ItemStack outputSlot = this.getStack(1);
                ItemStack output = recipe.getOutput();

                if (!outputSlot.isEmpty()
                        && !outputSlot.getItem()
                                      .equals(recipe.getOutput()
                                                    .getItem()) || outputSlot.getCount() + output.getCount() > outputSlot.getMaxCount()) {
                    return null;
                }
            }

            this.outputStack = recipe.getOutput();
            this.inputItem = testStack.getItem();
        }
        return recipe;
    }
}
