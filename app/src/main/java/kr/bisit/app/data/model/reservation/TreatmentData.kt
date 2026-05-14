package kr.bisit.app.data.model.reservation

data class TreatmentData(
    @com.google.gson.annotations.SerializedName("treatmentId", alternate = ["treatment_id"])
    val treatmentId: Long,
    
    @com.google.gson.annotations.SerializedName("treatmentName", alternate = ["treatment_name"])
    val treatmentName: String,
    
    @com.google.gson.annotations.SerializedName("durationMin", alternate = ["duration_min"])
    val durationMin: Int,
    
    @com.google.gson.annotations.SerializedName("price", alternate = ["fee", "cost", "amount", "listed_price", "listedPrice"])
    val price: Int,
    
    @com.google.gson.annotations.SerializedName("photoUrl", alternate = ["photo_url"])
    val photoUrl: String?,
    
    @com.google.gson.annotations.SerializedName("availableTimes", alternate = ["available_times"])
    val availableTimes: List<String>
)
