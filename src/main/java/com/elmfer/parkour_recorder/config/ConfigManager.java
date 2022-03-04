package com.elmfer.parkour_recorder.config;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.elmfer.parkour_recorder.gui.NumberLineView.TimeStampFormat;

import net.minecraftforge.common.ForgeConfigSpec;

/**The mod's configuration manager.**/
public class ConfigManager
{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	private static final TimelineSettings TIMELINE_CONFIG = new TimelineSettings(BUILDER);
	private static final RecordingSettings RECORDING_CONFIG = new RecordingSettings(BUILDER);
	private static final MenuSettings MENU_CONFIG = new MenuSettings(BUILDER);
	private static final PlaybackSettings PLAYBACK_CONFIG = new PlaybackSettings(BUILDER);
	public static final ForgeConfigSpec CONFIG_SPEC = BUILDER.build();
	
	public static final String CONFIG_EXTENSION = ".toml";
	
	private static class TimelineSettings
	{
		public final ForgeConfigSpec.ConfigValue<String> TIME_FORMAT;
		
		public TimelineSettings(ForgeConfigSpec.Builder builder)
		{
			builder.push("Timeline Settings");
			{
				TIME_FORMAT = 
						builder.comment("The format of how time is displayed in Timeline Gui.")
						.translation("gui.timeline.time_format")
						.define("timeStampFormat", TimeStampFormat.DEFAULT.NAME);
			}
			builder.pop();
		}
	}

	private static class RecordingSettings
	{
		public final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_LOOP;

		public RecordingSettings(ForgeConfigSpec.Builder builder)
		{
			builder.push("Recording Settings");
			{
				ENABLE_LOOP =
						builder.comment("It will be played endlessly. If enabled, the end and start points must be the same when recording.")
								.translation("gui.recording.enable_loop")
								.define("enableLoop", false);
			}
			builder.pop();
		}
	}
	
	private static class MenuSettings
	{
		public final ForgeConfigSpec.ConfigValue<Boolean> HIDDEN_IP;

		public MenuSettings(ForgeConfigSpec.Builder builder)
		{
			builder.push("Menu Settings");
			{
				HIDDEN_IP =
						builder.comment("If enabled, the server ip will be hidden.")
								.translation("gui.menu.hidden_ip")
								.define("hiddenIp", false);
			}
			builder.pop();
		}
	}
	
	private static class PlaybackSettings
	{
		public final ForgeConfigSpec.ConfigValue<Boolean> SHOW_INPUTS;
		
		public PlaybackSettings(ForgeConfigSpec.Builder builder)
		{
			builder.push("Playback Settings");
			{
				SHOW_INPUTS =
						builder.comment("If enabled, a HUD displaying input states will be shown.")
						.translation("gui.playback.show_inputs")
						.define("showInputs", false);
			}
			builder.pop();
		}
	}

	public static void init(Path file)
	{
		final CommentedFileConfig configData = CommentedFileConfig.builder(file)
			.sync()
			.autosave()
			.writingMode(WritingMode.REPLACE)
			.build();

		configData.load();
		CONFIG_SPEC.setConfig(configData);
	}
	
	public static void saveTimeFormat(TimeStampFormat format)
	{
		TIMELINE_CONFIG.TIME_FORMAT.set(format.NAME);
		CONFIG_SPEC.save();
	}
	
	public static TimeStampFormat loadTimeFormat()
	{
		return TimeStampFormat.getFormatFromName(TIMELINE_CONFIG.TIME_FORMAT.get());
	}

	public static void saveLoopMode(boolean isLoopEnabled)
	{
		RECORDING_CONFIG.ENABLE_LOOP.set(isLoopEnabled);
		CONFIG_SPEC.save();
	}

	public static boolean isLoopMode()
	{
		return RECORDING_CONFIG.ENABLE_LOOP.get();
	}
	
	public static void saveHiddenIp(boolean isHiddenIp)
	{
		MENU_CONFIG.HIDDEN_IP.set(isHiddenIp);
		CONFIG_SPEC.save();
	}

	public static boolean isHiddenIp()
	{
		return MENU_CONFIG.HIDDEN_IP.get();
	}
	
	public static void saveShowInputs(boolean showInputs)
	{
		PLAYBACK_CONFIG.SHOW_INPUTS.set(showInputs);
		CONFIG_SPEC.save();
	}
	
	public static boolean showInputs()
	{
		return PLAYBACK_CONFIG.SHOW_INPUTS.get();
	}
}
