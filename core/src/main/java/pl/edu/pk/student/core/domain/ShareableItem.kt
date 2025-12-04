package pl.edu.pk.student.core.domain

interface ShareableItem {
    val id: String
    val title: String
    val content: String?
    val imageUri: String?
    val timestamp: Long

    fun getShareableType(): ShareableType
    fun toPlainText(): String
    fun toHtml(): String
}

enum class ShareableType(val displayName: String) {
    PRESCRIPTION("Prescription"),
    LAB_RESULT("Lab Result"),
    DOCTOR_NOTE("Doctor's Recommendation"),
    MULTIPLE("Multiple Records")
}

fun formatTimestamp(timestamp: Long): String {
    val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(timestamp))
}