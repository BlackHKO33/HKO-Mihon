
package eu.kanade.presentation.browse.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import eu.kanade.presentation.library.components.CommonMangaItemDefaults
import eu.kanade.presentation.library.components.MangaComfortableGridItem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.presentation.core.util.plus

@Composable
fun BrowseSourceComfortableGrid(
    mangaList: LazyPagingItems<StateFlow<Manga>>,
    columns: GridCells,
    contentPadding: PaddingValues,
    onMangaClick: (Manga) -> Unit,
    onMangaLongClick: (Manga) -> Unit,
    isSelectionMode: Boolean = false,
    selectedMangaIds: Set<Long> = emptySet(),
    onMangaSelectionToggle: (Manga) -> Unit = {},
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val showScrollToStart by remember {
        derivedStateOf { gridState.canScrollBackward }
    }
    val showScrollToEnd by remember {
        derivedStateOf { gridState.canScrollForward }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = columns,
            contentPadding = contentPadding + PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(CommonMangaItemDefaults.GridVerticalSpacer),
            horizontalArrangement = Arrangement.spacedBy(CommonMangaItemDefaults.GridHorizontalSpacer),
        ) {
            if (mangaList.loadState.prepend is LoadState.Loading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BrowseSourceLoadingItem()
                }
            }

            items(count = mangaList.itemCount) { index ->
                val manga by mangaList[index]?.collectAsState() ?: return@items
                BrowseSourceComfortableGridItem(
                    manga = manga,
                    isSelected = manga.id in selectedMangaIds,
                    onClick = {
                        if (isSelectionMode) {
                            onMangaSelectionToggle(manga)
                        } else {
                            onMangaClick(manga)
                        }
                    },
                    onLongClick = {
                        if (!isSelectionMode) onMangaLongClick(manga)
                    },
                )
            }

            if (mangaList.loadState.refresh is LoadState.Loading || mangaList.loadState.append is LoadState.Loading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BrowseSourceLoadingItem()
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showScrollToStart) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            gridState.animateScrollToItem(0)
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Ir al principio",
                    )
                }
            }

            if (showScrollToEnd) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            while (true) {
                                val previousCount = mangaList.itemCount
                                if (previousCount == 0) break

                                gridState.animateScrollToItem(previousCount - 1)

                                val appendState = mangaList.loadState.append
                                if (appendState is LoadState.NotLoading && appendState.endOfPaginationReached) {
                                    break
                                }
                                if (appendState is LoadState.Error) break

                                val result = snapshotFlow {
                                    mangaList.itemCount to mangaList.loadState.append
                                }.first { (count, state) ->
                                    count > previousCount ||
                                        (state is LoadState.NotLoading && state.endOfPaginationReached) ||
                                        state is LoadState.Error
                                }

                                val newAppendState = result.second
                                if (newAppendState is LoadState.Error) break

                                if (newAppendState is LoadState.NotLoading && newAppendState.endOfPaginationReached) {
                                    val lastIndex = mangaList.itemCount - 1
                                    if (lastIndex >= 0) {
                                        gridState.animateScrollToItem(lastIndex)
                                    }
                                    break
                                }

                                if (mangaList.itemCount <= previousCount) break
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Ir al final",
                    )
                }
            }
        }
    }
}

@Composable
private fun BrowseSourceComfortableGridItem(
    manga: Manga,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = onClick,
) {
    MangaComfortableGridItem(
        title = manga.title,
        coverData = MangaCover(
            mangaId = manga.id,
            sourceId = manga.source,
            isMangaFavorite = manga.favorite,
            url = manga.thumbnailUrl,
            lastModified = manga.coverLastModified,
        ),
        coverAlpha = if (manga.favorite) CommonMangaItemDefaults.BrowseFavoriteCoverAlpha else 1f,
        coverBadgeStart = {
            InLibraryBadge(enabled = manga.favorite)
        },
        isSelected = isSelected,
        onLongClick = onLongClick,
        onClick = onClick,
    )
}
