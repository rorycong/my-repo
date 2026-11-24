package splitter.client.model;

import splitter.shared.GroupSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stores the list of groups the user belongs to. Notifies listeners on actual change.
 */
public class GroupListModel {
    private List<GroupSummary> groups = Collections.emptyList();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public GroupListModel() {}

    public void addListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (Runnable r : listeners) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }

    public synchronized void replaceGroups(List<GroupSummary> newGroups) {
        List<GroupSummary> list = newGroups != null ? new ArrayList<>(newGroups) : Collections.emptyList();
        if (!Objects.equals(this.groups, list)) {
            this.groups = Collections.unmodifiableList(list);
            notifyListeners();
        }
    }

    public synchronized List<GroupSummary> getGroups() {
        return groups;
    }
}
