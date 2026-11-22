package com.streamline.tv.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.focus.FocusRequester

/**
 * Requests focus for [focusRequester] once the screen has actually been laid out.
 *
 * A bare `LaunchedEffect { requestFocus() }` runs before the first layout pass, so
 * the target node does not exist yet and the request is silently dropped. That is
 * why the focus ring used to disappear after leaving Settings or after a cold
 * start. Retrying across frames fixes both without an arbitrary `delay`.
 */
@Composable
fun FocusOnEnter(
    focusRequester: FocusRequester,
    key: Any? = Unit,
    maxAttempts: Int = 12,
) {
    LaunchedEffect(key) {
        repeat(maxAttempts) {
            if (runCatching { focusRequester.requestFocus() }.isSuccess) return@LaunchedEffect
            withFrameNanos { }
        }
    }
}
