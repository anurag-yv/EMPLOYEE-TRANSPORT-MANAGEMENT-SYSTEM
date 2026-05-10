package com.example.employee_transport_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity for storing system-wide configurations.
 */
@Entity
@Table(name = "system_configs")
public class SystemConfig {

    /** The unique key for the config (e.g., 'global'). */
    @Id
    private String id = "global";

    private int bookingWindow = 2;
    private int autoRefresh = 30;
    private boolean sysNotifications = true;
    private int maxBookings = 1;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getBookingWindow() { return bookingWindow; }
    public void setBookingWindow(int bookingWindow) { this.bookingWindow = bookingWindow; }

    public int getAutoRefresh() { return autoRefresh; }
    public void setAutoRefresh(int autoRefresh) { this.autoRefresh = autoRefresh; }

    public boolean isSysNotifications() { return sysNotifications; }
    public void setSysNotifications(boolean sysNotifications) { this.sysNotifications = sysNotifications; }

    public int getMaxBookings() { return maxBookings; }
    public void setMaxBookings(int maxBookings) { this.maxBookings = maxBookings; }
}
