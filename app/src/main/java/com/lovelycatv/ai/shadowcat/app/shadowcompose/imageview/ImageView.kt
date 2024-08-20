package com.lovelycatv.ai.shadowcat.app.shadowcompose.imageview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.lovelycatv.ai.shadowcat.app.R

@Composable
fun AsyncImageView(
    modifier: Modifier = Modifier,
    imageUrl: String = "",
    description: String? = null,
    placeholder: Painter? = null,
    onImageClick: (() -> Unit)? = null,
) {
    SubcomposeAsyncImage(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onImageClick?.invoke() },
        model = imageUrl,
        contentDescription = description,
        contentScale = ContentScale.Crop
    ) {
        val state = painter.state
        when (state) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator()
            }

            is AsyncImagePainter.State.Error -> {
                Image(
                    painter = placeholder ?: painterResource(id = R.drawable.akarin),
                    contentDescription = description
                )
            }

            else -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}


@Composable
fun FullScreenImageView(
    modifier: Modifier = Modifier,
    imageUrl: String = "",
    description: String? = null,
    placeholder: Painter? = null,
    onImageClick: (() -> Unit)? = null,
    belowContent: (@Composable () -> Unit)? = null
) {
    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.hsv(0f, 0f, 0f, .5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImageView(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                imageUrl = imageUrl,
                description = description,
                placeholder = placeholder,
                onImageClick = onImageClick
            )

            belowContent?.invoke()
        }
    }
}