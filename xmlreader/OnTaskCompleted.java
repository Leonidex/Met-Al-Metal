package com.leonidex.xmlreader;

import java.util.ArrayList;

import com.leonidex.db.Item;

public interface OnTaskCompleted{
    void onTaskCompleted(int result, ArrayList<Item> newItems);
    void onSectionStart(int messageId);
}
