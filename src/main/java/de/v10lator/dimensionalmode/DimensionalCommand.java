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

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.server.permission.PermissionAPI;

public class DimensionalCommand extends CommandBase {
	private final DimensionalMode mod;
	
	DimensionalCommand(DimensionalMode mod)
	{
		this.mod = mod;
	}
	
	@Override
	public String getName() {
		return "dimmode";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/dimmode <mode> <world>";
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender instanceof EntityPlayer ? PermissionAPI.hasPermission((EntityPlayer) sender, mod.permNode) : true;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length != 2)
		{
			sender.sendMessage(makeMessage(TextFormatting.RED, getUsage(sender)));
			return;
		}
		args[1] = args[1].toUpperCase();
		if(!args[1].startsWith("DIM"))
		{
			try
			{
				args[1] = "DIM" + Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e)
			{
				sender.sendMessage(makeMessage(TextFormatting.RED, "Invalid dimension: " + args[1]));
				return;
			}
		}
		WorldServer dimension;
		try
		{
			dimension = server.getWorld(Integer.parseInt(args[1].substring(3)));
		}
		catch(NumberFormatException e)
		{
			sender.sendMessage(makeMessage(TextFormatting.RED, "Invalid dimension: " + args[1]));
			return;
		}
		if(dimension == null)
		{
			sender.sendMessage(makeMessage(TextFormatting.RED, "Invalid dimension: " + args[1]));
			return;
		}
		
		GameType type;
		args[0] = args[0].toUpperCase();
		switch(args[0])
		{
			case "0":
				args[0] = "SURVIVAL";
			case "SURVIVAL":
				type = GameType.SURVIVAL;
				break;
			case "1":
				args[0] = "CREATIVE";
			case "CREATIVE":
				type = GameType.CREATIVE;
				break;
			case "2":
				args[0] = "ADVENTURE";
			case "ADVENTURE":
				type = GameType.ADVENTURE;
				break;
			case "3":
				args[0] = "SPECTATOR";
			case "SPECTATOR":
				type = GameType.SPECTATOR;
				break;
			default:
				sender.sendMessage(makeMessage(TextFormatting.RED, "Invalid gamemode: " + args[0]));
				sender.sendMessage(makeMessage(TextFormatting.RED, "Take one of: CREATIVE, SURVIVAL, ADVENTURE, SPECTATOR"));
				return;
		}
		mod.config.get(mod.configCategory, args[1], args[0]).set(args[0]);
		
		for(EntityPlayer p: dimension.playerEntities)
			p.setGameType(type);
		
		if(mod.config.hasChanged())
			mod.config.save();
		sender.sendMessage(makeMessage(TextFormatting.GREEN, "Changed gamemode for dimension " + args[1] + " to " + args[0]));
	}
	
	private TextComponentString makeMessage(TextFormatting color, String message) {
		color.getColorIndex();
		TextComponentString ret = new TextComponentString(String.format("\u00A7%x", color.getColorIndex()));
		ret.appendText(message);
		return ret;
	}
}
