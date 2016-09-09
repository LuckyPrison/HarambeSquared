package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.comment.CommentInbox;
import com.intellectualcrafters.plot.object.comment.PlotComment;
import com.intellectualcrafters.plot.util.CommentManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
		command = "inbox",
		description = "Review the comments for a plot",
		usage = "/plot inbox [inbox] [delete <index>|clear|page]",
		permission = "plots.inbox",
		category = CommandCategory.CHAT,
		requiredType = RequiredType.NONE)
public class Inbox extends SubCommand {

	public void displayComments(PlotPlayer player, List<PlotComment> oldComments, int page)
	{
		if (oldComments == null || oldComments.isEmpty())
		{
			MainUtil.sendMessage(player, C.INBOX_EMPTY);
			return;
		}
		PlotComment[] comments = oldComments.toArray(new PlotComment[oldComments.size()]);
		if (page < 0)
		{
			page = 0;
		}
		// Get the total pages
		// int totalPages = ((int) Math.ceil(12 *
		int totalPages = (int) Math.ceil(comments.length / 12);
		if (page > totalPages)
		{
			page = totalPages;
		}
		// Only display 12 per page
		int max = page * 12 + 12;
		if (max > comments.length)
		{
			max = comments.length;
		}
		StringBuilder string = new StringBuilder();
		string.append(StringMan.replaceAll(C.COMMENT_LIST_HEADER_PAGED.s(),
										   "%amount%", comments.length,
										   "%cur", page + 1,
										   "%max", totalPages + 1,
										   "%word", "all")).append('\n');

		// This might work xD
		for (int x = page * 12; x < max; x++)
		{
			PlotComment comment = comments[x];
			String color = player.getName().equals(comment.senderName) ? "&a" : "&7";
			string.append("&8[&7#").append(x + 1).append("&8][&7").append(comment.world).append(';').append(comment.id).append("&8][&6")
				  .append(comment.senderName).append("&8]").append(color).append(comment.comment).append('\n');
		}
		MainUtil.sendMessage(player, string.toString());
	}

	@Override
	public boolean onCommand(PlotPlayer player, String... args)
	{
		Plot plot = player.getCurrentPlot();
		if (plot == null)
		{
			this.sendMessage(player, C.NOT_IN_PLOT);
			return false;
		}
		if (!plot.hasOwner())
		{
			this.sendMessage(player, C.PLOT_UNOWNED);
			return false;
		}
		if (args.length == 0)
		{
			this.sendMessage(player, C.COMMAND_SYNTAX, "/plot inbox [inbox] [delete <index>|clear|page]");
			for (CommentInbox inbox : CommentManager.inboxes.values())
			{
				if (inbox.canRead(plot, player))
				{
					if (!inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
						@Override
						public void run(List<PlotComment> value)
						{
							if (value != null)
							{
								int total = 0;
								int unread = 0;
								for (PlotComment comment : value)
								{
									total++;
									if (comment.timestamp > CommentManager.getTimestamp(player, inbox.toString()))
									{
										unread++;
									}
								}
								if (total != 0)
								{
									String color = unread > 0 ? "&c" : "";
									Inbox.this.sendMessage(player, C.INBOX_ITEM, color + inbox + " (" + total + '/' + unread + ')');
									return;
								}
							}
							Inbox.this.sendMessage(player, C.INBOX_ITEM, inbox.toString());
						}
					}))
					{
						this.sendMessage(player, C.INBOX_ITEM, inbox.toString());
					}
				}
			}
			return false;
		}
		CommentInbox inbox = CommentManager.inboxes.get(args[0].toLowerCase());
		if (inbox == null)
		{
			this.sendMessage(player, C.INVALID_INBOX, StringMan.join(CommentManager.inboxes.keySet(), ", "));
			return false;
		}
		player.setMeta("inbox:" + inbox, System.currentTimeMillis());
		int page;
		if (args.length > 1)
		{
			switch (args[1].toLowerCase())
			{
				case "delete":
					if (!inbox.canModify(plot, player))
					{
						this.sendMessage(player, C.NO_PERM_INBOX_MODIFY);
						return false;
					}
					if (args.length != 3)
					{
						this.sendMessage(player, C.COMMAND_SYNTAX, "/plot inbox " + inbox + " delete <index>");
					}
					int index;
					try
					{
						index = Integer.parseInt(args[2]);
						if (index < 1)
						{
							this.sendMessage(player, C.NOT_VALID_INBOX_INDEX, String.valueOf(index));
							return false;
						}
					}
					catch (NumberFormatException ignored)
					{
						this.sendMessage(player, C.COMMAND_SYNTAX, "/plot inbox " + inbox + " delete <index>");
						return false;
					}

					if (!inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
						@Override
						public void run(List<PlotComment> value)
						{
							if (index > value.size())
							{
								Inbox.this.sendMessage(player, C.NOT_VALID_INBOX_INDEX, String.valueOf(index));
								return;
							}
							PlotComment comment = value.get(index - 1);
							inbox.removeComment(plot, comment);
							plot.getSettings().removeComment(comment);
							MainUtil.sendMessage(player, C.COMMENT_REMOVED, comment.comment);
						}
					}))
					{
						this.sendMessage(player, C.NOT_IN_PLOT);
						return false;
					}
					return true;
				case "clear":
					if (!inbox.canModify(plot, player))
					{
						this.sendMessage(player, C.NO_PERM_INBOX_MODIFY);
					}
					inbox.clearInbox(plot);
					Optional<ArrayList<PlotComment>> comments = plot.getSettings().getComments(inbox.toString());
					if (comments.isPresent())
					{
						plot.getSettings().removeComments(comments.get());
					}
					MainUtil.sendMessage(player, C.COMMENT_REMOVED, "*");
					return true;
				default:
					try
					{
						page = Integer.parseInt(args[1]);
					}
					catch (NumberFormatException ignored)
					{
						this.sendMessage(player, C.COMMAND_SYNTAX, "/plot inbox [inbox] [delete <index>|clear|page]");
						return false;
					}
			}
		}
		else
		{
			page = 1;
		}
		if (!inbox.canRead(plot, player))
		{
			this.sendMessage(player, C.NO_PERM_INBOX);
			return false;
		}
		if (!inbox.getComments(plot, new RunnableVal<List<PlotComment>>() {
			@Override
			public void run(List<PlotComment> value)
			{
				Inbox.this.displayComments(player, value, page);
			}
		}))
		{
			this.sendMessage(player, C.PLOT_UNOWNED);
			return false;
		}
		return true;
	}
}
