
package eu.kanade.presentation.browse.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun BrowseSourceScrollIcon(
    pointUp: Boolean,
) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val stroke = 2.5.dp.toPx()
        val arrowSize = size.width * 0.25f

        val direction = if (pointUp) -1f else 1f
        val tipY = centerY + direction * arrowSize
        val bottomY = centerY - direction * arrowSize

        drawLine(
            color = Color.White,
            start = Offset(centerX, bottomY),
            end = Offset(centerX, tipY),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )

        drawLine(
            color = Color.White,
            start = Offset(centerX, tipY),
            end = Offset(centerX - arrowSize, tipY - direction * arrowSize),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )

        drawLine(
            color = Color.White,
            start = Offset(centerX, tipY),
            end = Offset(centerX + arrowSize, tipY - direction * arrowSize),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
