package com.elmfer.parkourhelper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.elmfer.parkourhelper.ParkourFrame.Flags;
import com.elmfer.parkourhelper.gui.GUIAlphaMask;
import com.elmfer.parkourhelper.gui.GuiSaveSelector;
import com.elmfer.parkourhelper.render.GraphicsHelper;
import com.elmfer.parkourhelper.render.ModelManager;
import com.elmfer.parkourhelper.render.ParticleArrow;
import com.elmfer.parkourhelper.render.ParticleFinish;
import com.elmfer.parkourhelper.render.ShaderManager;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class EventHandler {
	
	public static Recording recording = new Recording();
	static RecorderHUD hud = new RecorderHUD();
	static ParticleArrow arrow = new ParticleArrow(null, 0, 0, 0);
	static ParticleFinish finish = new ParticleFinish(null, 0, 0, 0);
	static boolean onOverride = false;
	static boolean isRecording = false;
	static boolean isPlaying = false;
	static Minecraft mc = Minecraft.getMinecraft();
	static int frameNumber = 0;
	static float playerYaw = 0;
	static float prevPlayerYaw = 0;
	static float playerPitch = 0;
	static float prevPlayerPitch = 0;
	static double posX = 0;
	static double posY = 0;
	static double posZ = 0;
	static int rightClickDelayTimer = 0;
	static int leftClickCounter = 0;
	static int savedFlagCounter = 0;
	static boolean waiting = false;
	static int prevDisplayWidth = mc.displayWidth;
	static int prevDisplayHeight = mc.displayHeight;
	
	@SubscribeEvent
	public static void onInitGUI(InitGuiEvent event)
	{
		if(GraphicsHelper.guiMask == null)
			GraphicsHelper.guiMask = new GUIAlphaMask(mc.displayWidth, mc.displayHeight);
		else if(prevDisplayWidth != mc.displayWidth || prevDisplayHeight != mc.displayHeight)
		{
			GraphicsHelper.guiMask.deleteFramebuffer();
			GraphicsHelper.guiMask = new GUIAlphaMask(mc.displayWidth, mc.displayHeight);
		}
		GraphicsHelper.guiMask.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);
		GraphicsHelper.guiMask.framebufferClear();
		prevDisplayWidth = mc.displayWidth;
		prevDisplayHeight = mc.displayHeight;
	}
	
	@SubscribeEvent
	public static void onOverlayRender(RenderGameOverlayEvent event)
	{
		if(event.getType() == ElementType.CHAT)
		{
			hud.render();
		}
	}
	@SubscribeEvent
	public static void onRenderTick(TickEvent.RenderTickEvent event)
	{
		if(isPlaying)
		{
			mc.player.rotationYaw = playerYaw;// + (playerYaw - prevPlayerYaw) * mc.getRenderPartialTicks();
			mc.player.rotationPitch = playerPitch;// + (playerPitch - prevPlayerPitch) * mc.getRenderPartialTicks();
			mc.player.setPosition(posX, posY, posZ);
		}
	}
	@SubscribeEvent
	public static void onTick(TickEvent.ClientTickEvent event)
	{	
		if (rightClickDelayTimer > 0)
			--rightClickDelayTimer;
		
		--leftClickCounter;
		
		if(mc.player == null) return;
		if(event.phase == Phase.START)
		{	
			Settings settings = Settings.getSettings();
			
			if(settings.keybindReloadShaders.isPressed())
				reloadResources();
			if(settings.keybindSave.isPressed())
				recording.save();
			if(settings.keybindLoad.isPressed())
				mc.displayGuiScreen(new GuiSaveSelector());
			if(settings.keybindRecord.isPressed() && !waiting)
			{
				if(onOverride)
				{
					onOverride = false;
					mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
				}else {
					isPlaying = false;
					isRecording = !isRecording;
					if(isRecording) 
					{
						mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
						recording.clear();
						recording = new Recording(mc.player.getPositionVector(), 
								new Vec3d(mc.player.motionX, mc.player.motionY, mc.player.motionZ));
					}
					else
					{
						recording.lastPos = mc.player.getPositionVector();
						recording.lastVel = new Vec3d(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
					}
				}
			}
			if(settings.keybindPlay.isPressed() && !isRecording && !onOverride)
			{
				if(recording.initPos.distanceTo(mc.player.getPositionVector()) > 1.0)
					spawnParticles();
				waiting = true;
			}
			if(waiting && recording.initPos.distanceTo(mc.player.getPositionVector()) < 1.0)
			{
				isPlaying = !isPlaying;
				if(isPlaying) mc.player.movementInput = new ControlledMovementInput();
				else mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
				frameNumber = 0;
				waiting = false;
			}
			
			if(isRecording || onOverride)
			{				
				recording.add(new ParkourFrame(mc.gameSettings, mc.player));
			}
			
			if(isPlaying)
			{
				if(frameNumber < recording.size())
				{
					if(frameNumber == 0)
					{
						mc.player.setPositionAndUpdate(recording.initPos.x, recording.initPos.y, recording.initPos.z);
						mc.player.setVelocity(recording.initVel.x, recording.initVel.y, recording.initVel.z);
						if(arrow.isAlive()) arrow.setExpired();
					}
					ParkourFrame frame = recording.get(frameNumber);
					ParkourFrame nextFrame = frameNumber + 1 == recording.size() ? frame : recording.get(frameNumber + 1);
					
					setPlayerState(frame, nextFrame);
					
					if(settings.keybindOverride.isPressed())
					{
						int size = recording.size();
						for(int i = frameNumber ; i < size ; i++)
						{
							recording.remove(frameNumber);
						}
						onOverride = true;
						isPlaying = false;
						mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
						hud.lastRecordingSize = recording.size();
						finish.setExpired();
					}
					
					frameNumber++;
				}else {
					finish.setExpired();
					isPlaying = false;
					mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
				}
			}
			
			if(settings.keybindOverride.isPressed() && onOverride && !waiting)
			{
				onOverride = false;
				mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
				recording.lastPos = mc.player.getPositionVector();
				recording.lastVel = new Vec3d(mc.player.motionX, mc.player.motionY, mc.player.motionZ);
			}
			
			
		}
	}
	
	private static	 void setPlayerState(ParkourFrame frame, ParkourFrame nextFrame)
	{
		MovementInput input = mc.player.movementInput;
		
		frame.setInput(input, mc.player);
		
		//if (frame.getFlag(Flags.USING) && rightClickDelayTimer == 0 && !mc.player.isHandActive())
			//onRightClick();
		//if(frame.getFlag(Flags.HITTING)) 
			//onLeftClick();
		
		prevPlayerYaw = playerYaw;
		prevPlayerPitch = playerPitch;
		
		playerYaw = frame.headYaw;
		playerPitch = frame.headPitch;
		
		posX = frame.posX;
		posY = frame.posY;
		posZ = frame.posZ;
	}
	
	private static void spawnParticles()
	{
		arrow.setExpired();
		arrow = new ParticleArrow(mc.world, recording.initPos.x, recording.initPos.y, recording.initPos.z);
		mc.effectRenderer.addEffect(arrow);
		
		finish.setExpired();
		finish = new ParticleFinish(mc.world, recording.lastPos.x, recording.lastPos.y, recording.lastPos.z);
		mc.effectRenderer.addEffect(finish);
	}
	
	private static void reloadResources()
	{
		ShaderManager.reloadShaders();
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourHelperMod.MOD_ID, "models/arrow.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourHelperMod.MOD_ID, "models/box.ply"));
		ModelManager.loadModelFromResource(new ResourceLocation(ParkourHelperMod.MOD_ID, "models/finish.ply"));
	}
	
	private static void onLeftClick()
	{
		if (leftClickCounter <= 0)
        {
            if (mc.objectMouseOver == null)
            {
                if (mc.playerController.isNotCreative())
                {
                    leftClickCounter = 10;
                }
            }
            else if (!mc.player.isRowingBoat())
            {
                switch (mc.objectMouseOver.typeOfHit)
                {
                    case ENTITY:
                        mc.playerController.attackEntity(mc.player, mc.objectMouseOver.entityHit);
                        break;
                    case BLOCK:
                        BlockPos blockpos = mc.objectMouseOver.getBlockPos();

                        if (!mc.world.isAirBlock(blockpos))
                        {
                            mc.playerController.clickBlock(blockpos, mc.objectMouseOver.sideHit);
                            break;
                        }

                    case MISS:

                        if (mc.playerController.isNotCreative())
                        {
                            leftClickCounter = 10;
                        }

                        mc.player.resetCooldown();
                        net.minecraftforge.common.ForgeHooks.onEmptyLeftClick(mc.player);
                }

                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
	}
	
	@SuppressWarnings("incomplete-switch")
	private static void onRightClick()
	{
		if (!mc.playerController.getIsHittingBlock())
        {
			rightClickDelayTimer = 4;
			
            if (!mc.player.isRowingBoat())
            {

                for (EnumHand enumhand : EnumHand.values())
                {
                    ItemStack itemstack = mc.player.getHeldItem(enumhand);

                    if (mc.objectMouseOver != null)
                    {
                        switch (mc.objectMouseOver.typeOfHit)
                        {
                            case ENTITY:

                                if (mc.playerController.interactWithEntity(mc.player, mc.objectMouseOver.entityHit, mc.objectMouseOver, enumhand) == EnumActionResult.SUCCESS)
                                {
                                    return;
                                }

                                if (mc.playerController.interactWithEntity(mc.player, mc.objectMouseOver.entityHit, enumhand) == EnumActionResult.SUCCESS)
                                {
                                    return;
                                }

                                break;
                            case BLOCK:
                                BlockPos blockpos = mc.objectMouseOver.getBlockPos();

                                if (mc.world.getBlockState(blockpos).getMaterial() != Material.AIR)
                                {
                                    int i = itemstack.getCount();
                                    EnumActionResult enumactionresult = mc.playerController.processRightClickBlock(mc.player, mc.world, blockpos, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec, enumhand);

                                    if (enumactionresult == EnumActionResult.SUCCESS)
                                    {
                                        mc.player.swingArm(enumhand);

                                        if (!itemstack.isEmpty() && (itemstack.getCount() != i || mc.playerController.isInCreativeMode()))
                                        {
                                            mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                                        }

                                        return;
                                    }
                                }
                        }
                    }

                    if (itemstack.isEmpty() && (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit == RayTraceResult.Type.MISS)) net.minecraftforge.common.ForgeHooks.onEmptyClick(mc.player, enumhand);
                    if (!itemstack.isEmpty() && mc.playerController.processRightClick(mc.player, mc.world, enumhand) == EnumActionResult.SUCCESS)
                    {
                        mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                        return;
                    }
                }
            }
        }
	}
}
