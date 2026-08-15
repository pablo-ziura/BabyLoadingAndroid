package com.pablo.ruiz.babyloading.core.pregnancy.content.presentation

import androidx.annotation.DrawableRes
import com.pablo.ruiz.babyloading.R
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize

@DrawableRes
fun BabySize.drawableResource(): Int = when (this) {
    BabySize.Lentil -> R.drawable.img_lentil
    BabySize.Blueberry -> R.drawable.img_blueberry
    BabySize.Raspberry -> R.drawable.img_raspberry
    BabySize.Cherry -> R.drawable.img_cherry
    BabySize.Strawberry -> R.drawable.img_strawberry
    BabySize.Fig -> R.drawable.img_fig
    BabySize.Plum -> R.drawable.img_plum
    BabySize.Peach -> R.drawable.img_peach
    BabySize.Lemon -> R.drawable.img_lemon
    BabySize.Apple -> R.drawable.img_apple
    BabySize.Avocado -> R.drawable.img_avocado
    BabySize.Pear -> R.drawable.img_pear
    BabySize.BellPepper -> R.drawable.img_bellpepper
    BabySize.Mango -> R.drawable.img_mango
    BabySize.SweetPotato -> R.drawable.img_sweetpotato
    BabySize.Carrot -> R.drawable.img_carrot
    BabySize.Banana -> R.drawable.img_banana
    BabySize.Eggplant -> R.drawable.img_eggplant
    BabySize.Corn -> R.drawable.img_corn
    BabySize.Cauliflower -> R.drawable.img_cauliflower
    BabySize.Zucchini -> R.drawable.img_zucchini
    BabySize.Cucumber -> R.drawable.img_cucumber
    BabySize.Coconut -> R.drawable.img_coconut
    BabySize.ButternutSquash -> R.drawable.img_butternutsquash
    BabySize.Cabbage -> R.drawable.img_cabbage
    BabySize.BunchOfGrapes -> R.drawable.img_bunchofgrapes
    BabySize.Pineapple -> R.drawable.img_pineapple
    BabySize.Cantaloupe -> R.drawable.img_cantaloupe
    BabySize.Honeydew -> R.drawable.img_honeydew
    BabySize.Papaya -> R.drawable.img_papaya
    BabySize.WinterSquash -> R.drawable.img_wintersquash
    BabySize.BunchOfBananas -> R.drawable.img_bunchofbananas
    BabySize.SmallWatermelon -> R.drawable.img_smallwatermelon
    BabySize.Watermelon -> R.drawable.img_watermelon
    BabySize.Pumpkin -> R.drawable.img_pumpkin
}
