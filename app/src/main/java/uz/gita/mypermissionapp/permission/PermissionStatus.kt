package uz.gita.mypermissionapp.permission

/** The four states a runtime permission can be in, from the app's point of view. */
enum class PermissionStatus(val label: String) {
    /** Never asked yet: calling launch() shows the system dialog. */
    NotRequested("Not requested"),

    /** The user allowed it (for a group like location: at least one of the permissions). */
    Granted("Granted"),

    /** Denied once: explain why you need it (rationale) before asking again. */
    Denied("Denied"),

    /** Denied twice or "Don't ask again": the dialog won't appear, only Settings can change it. */
    PermanentlyDenied("Permanently denied")
}
