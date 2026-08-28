package com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BabySize(val assetName: String) {
    @SerialName("lentil") Lentil("img_lentil"),
    @SerialName("blueberry") Blueberry("img_blueberry"),
    @SerialName("raspberry") Raspberry("img_raspberry"),
    @SerialName("cherry") Cherry("img_cherry"),
    @SerialName("strawberry") Strawberry("img_strawberry"),
    @SerialName("fig") Fig("img_fig"),
    @SerialName("plum") Plum("img_plum"),
    @SerialName("peach") Peach("img_peach"),
    @SerialName("lemon") Lemon("img_lemon"),
    @SerialName("apple") Apple("img_apple"),
    @SerialName("avocado") Avocado("img_avocado"),
    @SerialName("pear") Pear("img_pear"),
    @SerialName("bellPepper") BellPepper("img_bellpepper"),
    @SerialName("mango") Mango("img_mango"),
    @SerialName("sweetPotato") SweetPotato("img_sweetpotato"),
    @SerialName("carrot") Carrot("img_carrot"),
    @SerialName("banana") Banana("img_banana"),
    @SerialName("eggplant") Eggplant("img_eggplant"),
    @SerialName("corn") Corn("img_corn"),
    @SerialName("cauliflower") Cauliflower("img_cauliflower"),
    @SerialName("zucchini") Zucchini("img_zucchini"),
    @SerialName("cucumber") Cucumber("img_cucumber"),
    @SerialName("coconut") Coconut("img_coconut"),
    @SerialName("butternutSquash") ButternutSquash("img_butternutsquash"),
    @SerialName("cabbage") Cabbage("img_cabbage"),
    @SerialName("bunchOfGrapes") BunchOfGrapes("img_bunchofgrapes"),
    @SerialName("pineapple") Pineapple("img_pineapple"),
    @SerialName("cantaloupe") Cantaloupe("img_cantaloupe"),
    @SerialName("honeydew") Honeydew("img_honeydew"),
    @SerialName("papaya") Papaya("img_papaya"),
    @SerialName("winterSquash") WinterSquash("img_wintersquash"),
    @SerialName("bunchOfBananas") BunchOfBananas("img_bunchofbananas"),
    @SerialName("smallWatermelon") SmallWatermelon("img_smallwatermelon"),
    @SerialName("watermelon") Watermelon("img_watermelon"),
    @SerialName("pumpkin") Pumpkin("img_pumpkin"),
}
