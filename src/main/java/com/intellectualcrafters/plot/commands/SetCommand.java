package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;

public abstract class SetCommand extends SubCommand {

	@Override
	public boolean onCommand(PlotPlayer player, String... args)
	{
		Location loc = player.getLocation();
		Plot plot = loc.getPlotAbs();
		if (plot == null)
		{
			return !this.sendMessage(player, C.NOT_IN_PLOT);
		}
		if (!plot.hasOwner())
		{
			if (!Permissions.hasPermission(player, "plots.admin.command." + this.getFullId()))
			{
				MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.admin.command." + this.getFullId());
				MainUtil.sendMessage(player, C.PLOT_NOT_CLAIMED);
				return false;
			}
		}
		if (!plot.isOwner(player.getUUID()))
		{
			if (!Permissions.hasPermission(player, "plots.admin.command." + this.getFullId()))
			{
				MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.admin.command." + this.getFullId());
				MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
				return false;
			}
		}
		if (args.length == 0)
		{
			return this.set(player, plot, "");
		}
		return this.set(player, plot, StringMan.join(args, " "));
	}

	public abstract boolean set(PlotPlayer player, Plot plot, String value);
}
