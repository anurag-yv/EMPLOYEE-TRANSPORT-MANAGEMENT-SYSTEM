package com.example.employee_transport_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_configs")
public class SystemConfig {

    @Id
    private String id = "global";

    // Hours before departure when bookings close
    private int bookingWindow = 2;

    // Dashboard auto-refresh interval in seconds
    private int autoRefresh = 30;

    // Enable SMS notifications for route delays
    private boolean sysNotifications = true;

    // Maximum active bookings per user
    private int maxBookings = 1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBookingWindow() {
        return bookingWindow;
    }

    public void setBookingWindow(int bookingWindow) {
        this.bookingWindow = bookingWindow;
    }

    public int getAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(int autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public boolean isSysNotifications() {
        return sysNotifications;
    }

    public void setSysNotifications(boolean sysNotifications) {
        this.sysNotifications = sysNotifications;
    }

    public int getMaxBookings() {
        return maxBookings;
    }

    public void setMaxBookings(int maxBookings) {
        this.maxBookings = maxBookings;
    }
}
