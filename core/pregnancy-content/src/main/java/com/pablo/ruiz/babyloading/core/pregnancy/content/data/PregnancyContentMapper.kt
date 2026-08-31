package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.PregnancyContentDocument
import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.WeekContent
import javax.inject.Inject

class PregnancyContentMapper @Inject constructor() {
    internal fun toDomain(dto: PregnancyContentDocumentDto): PregnancyContentDocument {
        return PregnancyContentDocument(
            schemaVersion = dto.schemaVersion,
            locale = dto.locale,
            revision = dto.revision,
            weeks = dto.weeks.map(::toDomain),
        )
    }

    private fun toDomain(dto: WeekContentDto): WeekContent {
        return WeekContent(
            week = dto.week,
            babySize = toDomain(dto.babySize),
            babySizeLabel = dto.babySizeLabel,
            milestoneTitle = dto.milestoneTitle,
            keyEvents = dto.keyEvents,
            physiologicalImpact = dto.physiologicalImpact,
        )
    }

    internal fun toDomain(dto: BabySizeDto): BabySize = when (dto) {
        BabySizeDto.Lentil -> BabySize.Lentil
        BabySizeDto.Blueberry -> BabySize.Blueberry
        BabySizeDto.Raspberry -> BabySize.Raspberry
        BabySizeDto.Cherry -> BabySize.Cherry
        BabySizeDto.Strawberry -> BabySize.Strawberry
        BabySizeDto.Fig -> BabySize.Fig
        BabySizeDto.Plum -> BabySize.Plum
        BabySizeDto.Peach -> BabySize.Peach
        BabySizeDto.Lemon -> BabySize.Lemon
        BabySizeDto.Apple -> BabySize.Apple
        BabySizeDto.Avocado -> BabySize.Avocado
        BabySizeDto.Pear -> BabySize.Pear
        BabySizeDto.BellPepper -> BabySize.BellPepper
        BabySizeDto.Mango -> BabySize.Mango
        BabySizeDto.SweetPotato -> BabySize.SweetPotato
        BabySizeDto.Carrot -> BabySize.Carrot
        BabySizeDto.Banana -> BabySize.Banana
        BabySizeDto.Eggplant -> BabySize.Eggplant
        BabySizeDto.Corn -> BabySize.Corn
        BabySizeDto.Cauliflower -> BabySize.Cauliflower
        BabySizeDto.Zucchini -> BabySize.Zucchini
        BabySizeDto.Cucumber -> BabySize.Cucumber
        BabySizeDto.Coconut -> BabySize.Coconut
        BabySizeDto.ButternutSquash -> BabySize.ButternutSquash
        BabySizeDto.Cabbage -> BabySize.Cabbage
        BabySizeDto.BunchOfGrapes -> BabySize.BunchOfGrapes
        BabySizeDto.Pineapple -> BabySize.Pineapple
        BabySizeDto.Cantaloupe -> BabySize.Cantaloupe
        BabySizeDto.Honeydew -> BabySize.Honeydew
        BabySizeDto.Papaya -> BabySize.Papaya
        BabySizeDto.WinterSquash -> BabySize.WinterSquash
        BabySizeDto.BunchOfBananas -> BabySize.BunchOfBananas
        BabySizeDto.SmallWatermelon -> BabySize.SmallWatermelon
        BabySizeDto.Watermelon -> BabySize.Watermelon
        BabySizeDto.Pumpkin -> BabySize.Pumpkin
    }
}
