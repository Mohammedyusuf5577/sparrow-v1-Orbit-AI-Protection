package com.orbit.dto.request;

import com.orbit.util.AppUtils;
import java.time.LocalDateTime;

public class AttendanceRequest {
    private AppUtils.AttendanceStatus status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public AttendanceRequest() {
    }

    public AttendanceRequest(AppUtils.AttendanceStatus status, LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    public AppUtils.AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AppUtils.AttendanceStatus status) {
        this.status = status;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public static AttendanceRequestBuilder builder() {
        return new AttendanceRequestBuilder();
    }

    public static class AttendanceRequestBuilder {
        private AppUtils.AttendanceStatus status;
        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;

        public AttendanceRequestBuilder status(AppUtils.AttendanceStatus status) {
            this.status = status;
            return this;
        }

        public AttendanceRequestBuilder checkInTime(LocalDateTime checkInTime) {
            this.checkInTime = checkInTime;
            return this;
        }

        public AttendanceRequestBuilder checkOutTime(LocalDateTime checkOutTime) {
            this.checkOutTime = checkOutTime;
            return this;
        }

        public AttendanceRequest build() {
            return new AttendanceRequest(status, checkInTime, checkOutTime);
        }
    }
}
