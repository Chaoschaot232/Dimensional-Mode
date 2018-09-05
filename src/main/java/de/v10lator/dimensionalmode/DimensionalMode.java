/*
 * This file is part of Dimensional Mode.
 *
 * Dimensional Mode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Dimensional Mode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with Dimensional Mode.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.v10lator.dimensionalmode;

import java.io.File;

import org.apache.logging.log4j.LogManager;

import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

@Mod(modid = "##MODID##", name = "##NAME##", version = "##VERSION##", acceptedMinecraftVersions = "1.12.2", serverSideOnly = true, acceptableRemoteVersions = "*", updateJSON="http://forge.home.v10lator.de/update.json?id=##MODID##&v=##VERSION##")
public class DimensionalMode {
	Configuration config;
	final String configCategory = "gamemodes";
	final String permNode = "##MODID##.command";
	
	@Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
		config = new Configuration(new File(event.getModConfigurationDirectory(), "##NAME##.cfg"));
		config.load();
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Mod.EventHandler
	public void start(FMLServerStartingEvent event) {
		PermissionAPI.registerNode(permNode, DefaultPermissionLevel.OP, "Use the /dimmode command");
		event.registerServerCommand(new DimensionalCommand(this));
	}
	
	@SubscribeEvent
	public void onDimensionChange(PlayerEvent.PlayerLoggedInEvent event) {
		dimensionChange(event, event.player.world.provider.getDimension());
	}
	
	@SubscribeEvent
	public void onDimensionChange(PlayerEvent.PlayerRespawnEvent event) {
		dimensionChange(event, event.player.world.provider.getDimension());
	}
	
	@SubscribeEvent
	public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		dimensionChange(event, event.toDim);
	}
	
	private void dimensionChange(PlayerEvent event, int toDim)
	{
		if(event.isCanceled())
			return;
		
		String name = "DIM" + Integer.toString(toDim);
		if(!config.hasKey(configCategory, name))
		{
			event.player.setGameType(event.player.getServer().getGameType());
			return;
		}
		String mode = config.get(configCategory, name, "CREATIVE").getString().toUpperCase();
		GameType type;
		switch(mode)
		{
			case "CREATIVE":
				type = GameType.CREATIVE;
				break;
			case "SURVIVAL":
				type = GameType.SURVIVAL;
				break;
			case "ADVENTURE":
				type = GameType.ADVENTURE;
				break;
			case "SPECTATOR":
				type = GameType.SPECTATOR;
				break;
			default:
				LogManager.getLogger("##NAME##").info("Invalid game-mode while teleporting to " + name + "; " + mode);
				return;
		}
		event.player.setGameType(type);
	}
}
