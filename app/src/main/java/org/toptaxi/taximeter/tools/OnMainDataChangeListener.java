package org.toptaxi.taximeter.tools;

import org.toptaxi.taximeter.data.Order;

public interface OnMainDataChangeListener {
    void OnMainCurViewChange(int curViewType);
    void OnCurOrderDataChange(Order curOrder);
}
