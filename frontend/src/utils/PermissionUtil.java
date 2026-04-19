package utils;

public class PermissionUtil {

	public static boolean isAdmin() {
	    return "Admin".equalsIgnoreCase(SessionManager.getRole());
	}

	public static boolean isStaff() {
	    return "Staff".equalsIgnoreCase(SessionManager.getRole());
	}

	public static boolean isGuest() {
	    return "Guest".equalsIgnoreCase(SessionManager.getRole());
	}	
	
	// USER MANAGEMENT
	
    public static boolean canManageUsers() {
        return SessionManager.isAdmin();
    }

    // COURSE PERMISSIONS

    public static boolean canCreateCourse() {
        return SessionManager.isAdmin();
    }

    public static boolean canAddIteration() {
        return SessionManager.isAdmin() || SessionManager.isStaff();
    }

    // ATTENDANCE / CLIENTS

    public static boolean canModifyAttendance() {
        return SessionManager.isAdmin() || SessionManager.isStaff();
    }

    public static boolean canAddClients() {
        return SessionManager.isAdmin() || SessionManager.isStaff();
    }

    // STATISTICS

    public static boolean canViewStatistics() {
        return true; 
    }

    public static boolean canGenerateReports() {
        return SessionManager.isAdmin() || SessionManager.isStaff();
    }
    
    public static boolean canSubmitClientEntry() {
        return SessionManager.isAdmin() || SessionManager.isStaff();
    }
    
    public static boolean canModifyCourseData() {
        return isAdmin() || isStaff(); 
    }

    public static boolean canViewClients() {
        return true; 
    }
}