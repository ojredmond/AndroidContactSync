package uk.me.redmonds.contactsync;

import java.util.Comparator;
import java.util.Map;

/**
 * Sort HashMap
 * Created by oli on 05/02/15.
 */
class ListSortMap implements Comparator<Map<String, String>> {
    public int compare(Map left, Map right) {
        String leftName = (String) left.values().toArray()[0];
        String rightName = (String) right.values().toArray()[0];
        return leftName.compareTo(rightName);
    }
}
