package co.riiid.foodchooser.model

/**
 * Created by sangcomz on 6/3/16.
 */
data class RestaurantEntry(val section: Int = -1,
                           val imageUrl: String = "",
                           val userName: String = "",
                           val content: String = "",
                           val createAt: Long = -1)
