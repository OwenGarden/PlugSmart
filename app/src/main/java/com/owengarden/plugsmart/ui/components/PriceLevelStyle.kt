package com.owengarden.plugsmart.ui.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.owengarden.plugsmart.R
import com.owengarden.plugsmart.data.PriceLevel
import com.owengarden.plugsmart.ui.theme.CheapGreen
import com.owengarden.plugsmart.ui.theme.ExpensiveRed
import com.owengarden.plugsmart.ui.theme.NormalAmber

/** The colour, text colour and label used to show a price level. Kept in one place so every screen matches. */
data class PriceLevelStyle(
    val colour: Color,
    val onColour: Color,       // readable text colour on top of [colour]
    @param:StringRes val label: Int
)

fun PriceLevel.style(): PriceLevelStyle = when (this) {
    PriceLevel.CHEAP -> PriceLevelStyle(CheapGreen, Color.Black, R.string.level_cheap)
    PriceLevel.NORMAL -> PriceLevelStyle(NormalAmber, Color.Black, R.string.level_normal)
    PriceLevel.EXPENSIVE -> PriceLevelStyle(ExpensiveRed, Color.White, R.string.level_expensive)
}
