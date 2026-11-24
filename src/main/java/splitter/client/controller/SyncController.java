package splitter.client.controller;

import splitter.client.model.GroupListModel;
import splitter.client.model.GroupModel;
import splitter.client.model.SessionModel;
import splitter.shared.FailureReasons;
import splitter.shared.GroupSnapshot;
import splitter.shared.GroupSummary;
import splitter.shared.Result;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Controller managing background polling for home and group views.
 */
public class SyncController {
    private final ServerConnection server;
    private final SessionModel session;
    private final GroupModel group;
    private final GroupListModel groups;
    private final AppController app;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "SyncController-Poll");
        t.setDaemon(true);
        return t;
    });

    private ScheduledFuture<?> pollingTask;
    private enum PollMode { NONE, HOME, GROUP }
    private PollMode currentMode = PollMode.NONE;
    private long currentGroupId = 0;

    public SyncController(ServerConnection server, SessionModel session, GroupModel group,
                          GroupListModel groups, AppController app) {
        this.server = Objects.requireNonNull(server, "ServerConnection cannot be null");
        this.session = Objects.requireNonNull(session, "SessionModel cannot be null");
        this.group = Objects.requireNonNull(group, "GroupModel cannot be null");
        this.groups = Objects.requireNonNull(groups, "GroupListModel cannot be null");
        this.app = Objects.requireNonNull(app, "AppController cannot be null");
    }

    public synchronized void startHomePolling() {
        stop();
        currentMode = PollMode.HOME;
        refreshNow();
        pollingTask = scheduler.scheduleWithFixedDelay(this::pollHome, 5, 5, TimeUnit.SECONDS);
    }

    public synchronized void startGroupPolling(long groupId) {
        stop();
        currentMode = PollMode.GROUP;
        currentGroupId = groupId;
        refreshNow();
        pollingTask = scheduler.scheduleWithFixedDelay(this::pollGroup, 5, 5, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        if (pollingTask != null) {
            pollingTask.cancel(true);
            pollingTask = null;
        }
        currentMode = PollMode.NONE;
        currentGroupId = 0;
    }

    public synchronized void refreshNow() {
        if (currentMode == PollMode.HOME) {
            pollHome();
        } else if (currentMode == PollMode.GROUP) {
            pollGroup();
        }
    }

    private void pollHome() {
        Result<List<GroupSummary>> res = server.listGroups();
        if (res.isSuccess()) {
            groups.replaceGroups(res.getData());
        }
    }

    private void pollGroup() {
        long gid = currentGroupId;
        if (gid <= 0) return;

        Optional<String> savedCode = session.getSavedGroupCode(gid);
        Result<GroupSnapshot> res = server.getGroup(gid, savedCode.orElse(""));
        if (res.isSuccess()) {
            group.replaceSnapshot(res.getData());
        } else {
            String reason = res.getFailureReason();
            if (FailureReasons.CODE_OUTDATED.equals(reason)) {
                session.forgetGroupCode(gid);
                app.groupUnavailable("The group code changed. Please enter the new code.");
            } else if (FailureReasons.GROUP_NOT_FOUND.equals(reason)) {
                app.groupUnavailable("The group no longer exists.");
            } else if (FailureReasons.NOT_A_MEMBER.equals(reason)) {
                app.groupUnavailable("You are no longer a member of this group.");
            }
        }
    }
}
