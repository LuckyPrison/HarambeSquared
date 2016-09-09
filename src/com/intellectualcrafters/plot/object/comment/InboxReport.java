package com.intellectualcrafters.plot.object.comment;

import java.util.List;

import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;

public class InboxReport extends CommentInbox {

    @Override
    public boolean canRead(Plot plot, PlotPlayer player) {
        if (Permissions.hasPermission(player, "plots.inbox.read." + toString())) {
            if (plot.isOwner(player.getUUID()) || Permissions
                    .hasPermission(player, "plots.inbox.read." + toString() + ".other")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canWrite(Plot plot, PlotPlayer player) {
        if (plot == null) {
            return Permissions.hasPermission(player, "plots.inbox.write." + toString());
        }
        return Permissions.hasPermission(player, "plots.inbox.write." + toString()) && (plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, "plots.inbox.write." + toString() + ".other"));
    }

    @Override
    public boolean canModify(Plot plot, PlotPlayer player) {
        if (Permissions.hasPermission(player, "plots.inbox.modify." + toString())) {
            if (plot.isOwner(player.getUUID()) || Permissions.hasPermission(player, "plots.inbox.modify." + toString() + ".other")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getComments(Plot plot, final RunnableVal<List<PlotComment>> whenDone) {
        DBFunc.getComments(null, toString(), new RunnableVal<List<PlotComment>>() {
            @Override
            public void run(List<PlotComment> value) {
                whenDone.value = value;
                TaskManager.runTask(whenDone);
            }
        });
        return true;
    }

    @Override
    public boolean addComment(Plot plot, PlotComment comment) {
        if (plot.owner == null) {
            return false;
        }
        DBFunc.setComment(plot, comment);
        return true;
    }

    @Override
    public String toString() {
        return "report";
    }

}
