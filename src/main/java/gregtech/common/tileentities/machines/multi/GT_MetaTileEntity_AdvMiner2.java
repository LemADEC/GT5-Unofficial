package gregtech.common.tileentities.machines.multi;

import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.Textures;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.objects.GT_RenderedTexture;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gregtech.common.blocks.GT_TileEntity_Ores;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class GT_MetaTileEntity_AdvMiner2 extends GT_MetaTileEntity_MultiBlockBase {

    public ArrayList<ChunkPosition> mMineList = new ArrayList();
    private boolean completedCycle = false;

    public GT_MetaTileEntity_AdvMiner2(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_AdvMiner2(String aName) {
        super(aName);
    }

    public String[] getDescription() {
        return new String[]{
                "Controller Block for the Advanced Miner II",
                "Size: 3x3x7", "Controller (front middle at bottom)",
                "3x3 Base of Solid Steel Casings",
                "Also part of Base: MV+ Energy Input Hatch",
                "Fluid Input Hatch, Output Bus and Maintainance Hatch",
                "3x Solid Steel Casings on top the Center of the base",
                "Steel Frame Boxes on each side and 3 more on top"};
    }

    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            return new ITexture[]{Textures.BlockIcons.CASING_BLOCKS[16], new GT_RenderedTexture(aActive ? Textures.BlockIcons.OVERLAY_FRONT_LARGE_BOILER_ACTIVE : Textures.BlockIcons.OVERLAY_FRONT_LARGE_BOILER)};
        }
        return new ITexture[]{Textures.BlockIcons.CASING_BLOCKS[16]};
    }
    
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_MultiMachine(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "DrillingRig.png");
    }

    @Override
    public boolean checkRecipe(ItemStack aStack) {
        if (mInventory[1] == null || (mInventory[1].isItemEqual(GT_ModHandler.getIC2Item("miningPipe", 1L)) && mInventory[1].stackSize < mInventory[1].getMaxStackSize())) {
            ArrayList<ItemStack> tItems = getStoredInputs();
            for (ItemStack tStack : tItems) {
                if (tStack.isItemEqual(GT_ModHandler.getIC2Item("miningPipe", 1L))) {
                    if (tStack.stackSize < 2) {
                        tStack = null;
                    } else {
                        tStack.stackSize--;
                    }

                }
                if (mInventory[1] == null) {
                    mInventory[1] = GT_ModHandler.getIC2Item("miningPipe", 1L);
                } else {
                    mInventory[1].stackSize++;
                }
            }
        }
        if (mInputHatches == null || mInputHatches.get(0).mFluid == null || mInputHatches.get(0).mFluid.getFluid().getID() != ItemList.sDrillingFluid.getID()) {
            return false;
        }
        FluidStack tFluid = mInputHatches.get(0).mFluid.copy();
        if (tFluid == null) {
            return false;
        }
        if (tFluid.amount < 100) {
            return false;
        }
        tFluid.amount = 100;
        depleteInput(tFluid);
        long tVoltage = getMaxInputVoltage();
        if (getBaseMetaTileEntity().getRandomNumber(20) == 0) {
            if (mMineList.isEmpty()) {
                int yLevel = getYOfPumpHead();
                for (int i = -48; i < 49; i++) {
                    for (int f = -48; f < 49; f++) {
                        Block tBlock = getBaseMetaTileEntity().getBlockOffset(i, yLevel - getBaseMetaTileEntity().getYCoord(), f);
                        if (tBlock == GregTech_API.sBlockOres1) {
                            TileEntity tTileEntity = getBaseMetaTileEntity().getTileEntityOffset(i, yLevel - getBaseMetaTileEntity().getYCoord(), f);
                            if ((tTileEntity instanceof GT_TileEntity_Ores) && ((GT_TileEntity_Ores) tTileEntity).mNatural == true && !mMineList.contains(new ChunkPosition(i, yLevel - getBaseMetaTileEntity().getYCoord(), f))) {
                                mMineList.add(new ChunkPosition(i, yLevel - getBaseMetaTileEntity().getYCoord(), f));
                            }
                        }
                    }
                }
            }
            if (mMineList.isEmpty() && getBaseMetaTileEntity().getBlockOffset(ForgeDirection.getOrientation(getBaseMetaTileEntity().getBackFacing()).offsetX, getYOfPumpHead() - 1 - getBaseMetaTileEntity().getYCoord(), ForgeDirection.getOrientation(getBaseMetaTileEntity().getBackFacing()).offsetZ) != Blocks.bedrock) {
                if (mEnergyHatches.size() > 0 && mEnergyHatches.get(0).getEUVar() > (512 + getMaxInputVoltage() * 4)) {
                    moveOneDown();
                }
            }
            ArrayList<ItemStack> tDrops = new ArrayList();
            if (!mMineList.isEmpty()) {
                Block tMineBlock = getBaseMetaTileEntity().getBlockOffset(mMineList.get(0).chunkPosX, mMineList.get(0).chunkPosY, mMineList.get(0).chunkPosZ);
                tDrops = tMineBlock.getDrops(getBaseMetaTileEntity().getWorld(), mMineList.get(0).chunkPosX, mMineList.get(0).chunkPosY, mMineList.get(0).chunkPosZ, getBaseMetaTileEntity().getMetaIDOffset(mMineList.get(0).chunkPosX, mMineList.get(0).chunkPosY, mMineList.get(0).chunkPosZ), 1);
                if (!tDrops.isEmpty()) {
                    if (GT_OreDictUnificator.getItemData(tDrops.get(0)).mPrefix != OrePrefixes.crushed) {
                        GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sMaceratorRecipes.findRecipe(getBaseMetaTileEntity(), false, tVoltage, null, tDrops.get(0));
                        if (tRecipe != null) {
                            this.mOutputItems = new ItemStack[tRecipe.mOutputs.length];
                            for (int g = 0; g < mOutputItems.length; g++) {
                                mOutputItems[g] = tRecipe.mOutputs[g].copy();
                                mOutputItems[g].stackSize = mOutputItems[g].stackSize * (getBaseMetaTileEntity().getRandomNumber(4) + 1);
                            }
                        }
                    } else {
                        this.mOutputItems = null;
                        ItemStack[] tStack = new ItemStack[tDrops.size()];
                        for (int j = 0; j < tDrops.size(); j++) {
                            tStack[j] = tDrops.get(j).copy();
                            tStack[j].stackSize = tStack[j].stackSize * (getBaseMetaTileEntity().getRandomNumber(4) + 1);
                        }
                        mOutputItems = tStack;
                    }
                }
                getBaseMetaTileEntity().getWorld().setBlockToAir(mMineList.get(0).chunkPosX + getBaseMetaTileEntity().getXCoord(), mMineList.get(0).chunkPosY + getBaseMetaTileEntity().getYCoord(), mMineList.get(0).chunkPosZ + getBaseMetaTileEntity().getZCoord());
                mMineList.remove(0);
            }
        }

        byte tTier = (byte) Math.max(1, GT_Utility.getTier(tVoltage));
        this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
        this.mEfficiencyIncrease = 10000;
        int tEU = 48;
        int tDuration = 24;
        if (tEU <= 16) {
            this.mEUt = (tEU * (1 << tTier - 1) * (1 << tTier - 1));
            this.mMaxProgresstime = (tDuration / (1 << tTier - 1));
        } else {
            this.mEUt = tEU;
            this.mMaxProgresstime = tDuration;
            while (this.mEUt <= gregtech.api.enums.GT_Values.V[(tTier - 1)]) {
                this.mEUt *= 4;
                this.mMaxProgresstime /= 2;
            }
        }
        if (this.mEUt > 0) {
            this.mEUt = (-this.mEUt);
        }
        this.mMaxProgresstime = Math.max(1, this.mMaxProgresstime);
        return true;
    }

    private boolean moveOneDown() {
        if ((this.mInventory[1] == null) || (this.mInventory[1].stackSize < 1)
                || (!GT_Utility.areStacksEqual(this.mInventory[1], GT_ModHandler.getIC2Item("miningPipe", 1L)))) {
            return false;
        }
        int xDir = ForgeDirection.getOrientation(getBaseMetaTileEntity().getBackFacing()).offsetX;
        int zDir = ForgeDirection.getOrientation(getBaseMetaTileEntity().getBackFacing()).offsetZ;
        int yHead = getYOfPumpHead();
        if (yHead <= 0) {
            return false;
        }
        if (getBaseMetaTileEntity().getBlock(getBaseMetaTileEntity().getXCoord() + xDir, yHead - 1, getBaseMetaTileEntity().getZCoord() + zDir) == Blocks.bedrock) {
            return false;
        }
        if (!(getBaseMetaTileEntity().getWorld().setBlock(getBaseMetaTileEntity().getXCoord() + xDir, yHead - 1, getBaseMetaTileEntity().getZCoord() + zDir, GT_Utility.getBlockFromStack(GT_ModHandler.getIC2Item("miningPipeTip", 1L))))) {
            return false;
        }
        if (yHead != getBaseMetaTileEntity().getYCoord()) {
            getBaseMetaTileEntity().getWorld().setBlock(getBaseMetaTileEntity().getXCoord() + xDir, yHead, getBaseMetaTileEntity().getZCoord() + zDir, GT_Utility.getBlockFromStack(GT_ModHandler.getIC2Item("miningPipe", 1L)));
        }
        getBaseMetaTileEntity().decrStackSize(1, 1);
        return true;
    }

    private int getYOfPumpHead() {
        int xDir = ForgeDirection.getOrientation(getBaseMetaTileEntity().getBackFacing()).offsetX;
        int zDir = ForgeDirection.getOrientation(getBaseMetaTileEntity().getBackFacing()).offsetZ;
        int y = getBaseMetaTileEntity().getYCoord() - 1;
        while (getBaseMetaTileEntity().getBlock(getBaseMetaTileEntity().getXCoord() + xDir, y, getBaseMetaTileEntity().getZCoord() + zDir) == GT_Utility.getBlockFromStack(GT_ModHandler.getIC2Item("miningPipe", 1L))) {
            y--;
        }
        if (y == getBaseMetaTileEntity().getYCoord() - 1) {
            if (getBaseMetaTileEntity().getBlock(getBaseMetaTileEntity().getXCoord() + xDir, y, getBaseMetaTileEntity().getZCoord() + zDir) != GT_Utility.getBlockFromStack(GT_ModHandler.getIC2Item("miningPipeTip", 1L))) {
                return y + 1;
            }
        } else if (getBaseMetaTileEntity().getBlock(getBaseMetaTileEntity().getXCoord() + xDir, y, getBaseMetaTileEntity().getZCoord() + zDir) != GT_Utility
                .getBlockFromStack(GT_ModHandler.getIC2Item("miningPipeTip", 1L)) && this.mInventory[1] != null && this.mInventory[1].stackSize > 0 && GT_Utility.areStacksEqual(this.mInventory[1], GT_ModHandler.getIC2Item("miningPipe", 1L))) {
            getBaseMetaTileEntity().getWorld().setBlock(getBaseMetaTileEntity().getXCoord() + xDir, y, getBaseMetaTileEntity().getZCoord() + zDir,
                    GT_Utility.getBlockFromStack(GT_ModHandler.getIC2Item("miningPipeTip", 1L)));
            getBaseMetaTileEntity().decrStackSize(0, 1);
        }
        return y;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX;
        int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if ((xDir + i != 0) || (zDir + j != 0)) {
                    IGregTechTileEntity tTileEntity = aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + i, 0, zDir + j);
                    if ((!addMaintenanceToMachineList(tTileEntity, 16)) && (!addInputToMachineList(tTileEntity, 16)) && (!addOutputToMachineList(tTileEntity, 16)) && (!addEnergyInputToMachineList(tTileEntity, 16))) {
                        if (aBaseMetaTileEntity.getBlockOffset(xDir + i, 0, zDir + j) != GregTech_API.sBlockCasings2) {
                            return false;
                        }
                        if (aBaseMetaTileEntity.getMetaIDOffset(xDir + i, 0, zDir + j) != 0) {
                            return false;
                        }
                    }
                }
            }
        }
        for (int y = 1; y < 4; y++) {
            if (aBaseMetaTileEntity.getBlockOffset(xDir, y, zDir) != GregTech_API.sBlockCasings2) {
                return false;
            }
            if (aBaseMetaTileEntity.getBlockOffset(xDir + 1, y, zDir) != GregTech_API.sBlockMachines) {
                return false;
            }
            if (aBaseMetaTileEntity.getBlockOffset(xDir - 1, y, zDir) != GregTech_API.sBlockMachines) {
                return false;
            }
            if (aBaseMetaTileEntity.getBlockOffset(xDir, y, zDir + 1) != GregTech_API.sBlockMachines) {
                return false;
            }
            if (aBaseMetaTileEntity.getBlockOffset(xDir, y, zDir - 1) != GregTech_API.sBlockMachines) {
                return false;
            }
            if (aBaseMetaTileEntity.getBlockOffset(xDir, y + 3, zDir) != GregTech_API.sBlockMachines) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getPollutionPerTick(ItemStack aStack) {
        return 0;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public int getAmountOfOutputs() {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_AdvMiner2(this.mName);
    }

}
